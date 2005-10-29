package org.apache.ddlutils.dynabean;

/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.ddlutils.DynaSqlException;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

/**
 * This is an iterator that is specifically targeted at traversing result sets.
 * If the query is against a known table, then {@link org.apache.ddlutils.dynabean.SqlDynaBean} instances
 * are created from the rows, otherwise normal {@link org.apache.commons.beanutils.DynaBean} instances
 * are created.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class DynaSqlIterator implements Iterator
{
    /** The base result set. */
    private ResultSet _resultSet;
    /** The dyna class to use for creating beans. */
    private DynaClass _dynaClass;
    /** Maps column names to properties. */
    private Map _columnsToProperties = new ListOrderedMap();
    /** Whether the next call to hasNext or next needs advancement. */
    private boolean _needsAdvancing = true;
    /** Whether we're already at the end of the result set. */
    private boolean _isAtEnd = false;
    /** Whether to close the statement and connection after finishing. */
    private boolean _cleanUpAfterFinish;

    /**
     * Creates a new iterator.
     * 
     * @param platformInfo       The platform info
     * @param model              The database model
     * @param resultSet          The result set
     * @param queryHints         The tables that were queried in the query that produced the given result set
     *                           (optional)
     * @param cleanUpAfterFinish Whether to close the statement and connection after finishing
     *                           the iteration, upon on exception, or when this iterator is garbage collected
     */
    public DynaSqlIterator(PlatformInfo platformInfo, Database model, ResultSet resultSet, Table[] queryHints, boolean cleanUpAfterFinish) throws DynaSqlException
    {
        if (resultSet != null)
        {
            _resultSet          = resultSet;
            _cleanUpAfterFinish = cleanUpAfterFinish;

            try
            {
                initFromMetaData(platformInfo, model, resultSet, queryHints);
            }
            catch (SQLException ex)
            {
                cleanUp();
                throw new DynaSqlException("Could not read the metadata of the result set", ex);
            }
        }
        else
        {
            _isAtEnd = true;
        }
    }

    /**
     * Initializes this iterator from the resultset metadata.
     * 
     * @param platformInfo The platform info
     * @param model        The database model
     * @param resultSet    The result set
     * @param queryHints   The tables that were queried in the query that produced the given result set
     */
    private void initFromMetaData(PlatformInfo platformInfo, Database model, ResultSet resultSet, Table[] queryHints) throws SQLException
    {
        ResultSetMetaData metaData           = resultSet.getMetaData();
        String            tableName          = null;
        boolean           singleKnownTable   = true;
        boolean           caseSensitive      = platformInfo.isUseDelimitedIdentifiers();
        Map               preparedQueryHints = prepareQueryHints(queryHints, caseSensitive);

        for (int idx = 1; idx <= metaData.getColumnCount(); idx++)
        {
            String columnName    = metaData.getColumnName(idx);
            String tableOfColumn = metaData.getTableName(idx);
            Table  table         = null;

            if ((tableOfColumn != null) && (tableOfColumn.length() > 0))
            {
                // the JDBC driver gave us enough meta data info
                table = model.findTable(tableOfColumn, caseSensitive);
            }
            else
            {
                // not enough info in the meta data of the result set, lets try the
                // user-supplied query hints
                table         = (Table)preparedQueryHints.get(caseSensitive ? columnName : columnName.toLowerCase());
                tableOfColumn = (table == null ? null : table.getName());
            }
            if (tableName == null)
            {
                tableName = tableOfColumn;
            }
            else if (!tableName.equals(tableOfColumn))
            {
                singleKnownTable = false;
            }

            String propName = columnName;

            if (table != null)
            {
                Column column = table.findColumn(columnName, caseSensitive);

                if (column != null)
                {
                    propName = column.getName();
                }
            }
            _columnsToProperties.put(columnName, propName);
        }
        if (singleKnownTable && (tableName != null))
        {
            _dynaClass = model.getDynaClassFor(tableName);
        }
        else
        {
            DynaProperty[] props = new DynaProperty[_columnsToProperties.size()];
            int            idx   = 0;

            for (Iterator it = _columnsToProperties.values().iterator(); it.hasNext(); idx++)
            {
                props[idx] = new DynaProperty((String)it.next());
            }
            _dynaClass = new BasicDynaClass("result", BasicDynaBean.class, props);
        }
    }

    /**
     * Prepares the query hints by extracting the column names and using them as keys
     * into the resulting map pointing to the corresponding table.
     *  
     * @param queryHints    The query hints
     * @param caseSensitive Whether the case of the column names is important
     * @return The column name -> table map
     */
    private Map prepareQueryHints(Table[] queryHints, boolean caseSensitive)
    {
        Map result = new HashMap();

        for (int tableIdx = 0; (queryHints != null) && (tableIdx < queryHints.length); tableIdx++)
        {
            for (int columnIdx = 0; columnIdx < queryHints[tableIdx].getColumnCount(); columnIdx++)
            {
                String columnName = queryHints[tableIdx].getColumn(columnIdx).getName();

                if (!caseSensitive)
                {
                    columnName = columnName.toLowerCase();
                }
                if (!result.containsKey(columnName))
                {
                    result.put(columnName, queryHints[tableIdx]);
                }
            }
        }
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean hasNext() throws DynaSqlException
    {
        advanceIfNecessary();
        return !_isAtEnd;
    }

    /**
     * {@inheritDoc}
     */
    public Object next() throws DynaSqlException
    {
        advanceIfNecessary();
        if (_isAtEnd)
        {
            throw new NoSuchElementException("No more elements in the resultset");
        }
        else
        {
            try
            {
                DynaBean bean = _dynaClass.newInstance();

                for (Iterator it = _columnsToProperties.entrySet().iterator(); it.hasNext();)
                {
                    Map.Entry entry = (Map.Entry)it.next();

                    bean.set((String)entry.getValue(), _resultSet.getObject((String)entry.getKey()));
                }
                _needsAdvancing = true;
                return bean;
            }
            catch (Exception ex)
            {
                cleanUp();
                throw new DynaSqlException("Exception while reading the row from the resultset", ex);
            }
        }
    }

    /**
     * Advances the result set if necessary.
     */
    private void advanceIfNecessary() throws DynaSqlException
    {
        if (_needsAdvancing && !_isAtEnd)
        {
            try
            {
                _isAtEnd        = !_resultSet.next();
                _needsAdvancing = false;
            }
            catch (SQLException ex)
            {
                cleanUp();
                throw new DynaSqlException("Could not retrieve next row from result set", ex);
            }
            if (_isAtEnd)
            {
                cleanUp();
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void remove() throws DynaSqlException
    {
        try
        {
            _resultSet.deleteRow();
        }
        catch (SQLException ex)
        {
            cleanUp();
            throw new DynaSqlException("Failed to delete current row", ex);
        }
    }

    /**
     * Closes the resources (connection, statement, resultset).
     */
    public void cleanUp()
    {
        if (_cleanUpAfterFinish && (_resultSet != null))
        {
            try
            {
                Statement  stmt = _resultSet.getStatement();
                Connection conn = stmt.getConnection();

                // also closes the resultset
                stmt.close();
                conn.close();
            }
            catch (SQLException ex)
            {
                // we ignore it
            }
            _resultSet = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void finalize() throws Throwable
    {
        cleanUp();
    }

    /**
     * Determines whether the connection is still open.
     * 
     * @return <code>true</code> if the connection is still open
     */
    public boolean isConnectionOpen()
    {
        if (_resultSet == null)
        {
            return false;
        }
        try
        {
            Statement  stmt = _resultSet.getStatement();
            Connection conn = stmt.getConnection();

            return !conn.isClosed();
        }
        catch (SQLException ex)
        {
            return false;
        }
    }
}
