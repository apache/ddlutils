package org.apache.ddlutils.io;

/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;

/**
 * An utility class to create a Database model from a live database.
 *
 * @author <a href="mailto:drfish@cox.net">J. Russell Smyth</a>
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision$
 */
public class JdbcModelReader
{
    /** The Log to which logging calls will be made */
    private final Log log = LogFactory.getLog(JdbcModelReader.class);

    /** Contains default column sizes (minimum sizes that a JDBC-compliant db must support) */
    private HashMap defaultSizes = new HashMap();
    /** The database connection */
    private Connection connection = null;
    /** The database catalog to read */
    private String catalog = "%";
    /** The database schema to read */
    private String schema = "%";
    /** The table types to recognize */
    private String[] tableTypes = { "TABLE", "VIEW" };
    /** The patern to recognize when parsing a default value */
    private Pattern defaultPattern = Pattern.compile("\\(\\'?(.*?)\\'?\\)");

    /**
     * Creates a new model reader instance for the given connection.
     * 
     * @param conn The database connection
     */
    public JdbcModelReader(Connection conn)
    {
        connection = conn;
        defaultSizes.put(new Integer(Types.CHAR),          "254");
        defaultSizes.put(new Integer(Types.VARCHAR),       "254");
        defaultSizes.put(new Integer(Types.LONGVARCHAR),   "254");
        defaultSizes.put(new Integer(Types.BINARY),        "254");
        defaultSizes.put(new Integer(Types.VARBINARY),     "254");
        defaultSizes.put(new Integer(Types.LONGVARBINARY), "254");
        defaultSizes.put(new Integer(Types.INTEGER),       "32");
        defaultSizes.put(new Integer(Types.BIGINT),        "64");
        defaultSizes.put(new Integer(Types.REAL),          "7,0");
        defaultSizes.put(new Integer(Types.FLOAT),         "15,0");
        defaultSizes.put(new Integer(Types.DOUBLE),        "15,0");
        defaultSizes.put(new Integer(Types.DECIMAL),       "15,15");
        defaultSizes.put(new Integer(Types.NUMERIC),       "15,15");
    }

    /**
     * Sets the catalog in the database to read.
     * 
     * @param catalog The catalog
     */
    public void setCatalog(String catalog)
    {
        this.catalog = catalog;
    }

    /**
     * Sets the schema in the database to read.
     * 
     * @param schema The schema
     */
    public void setSchema(String schema)
    {
        this.schema = schema;
    }

    /**
     * Sets the table types to recognize. Typical types are "TABLE", "VIEW", "SYSTEM TABLE",
     * "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     * 
     * @param types The table types
     */
    public void setTableTypes(String[] types)
    {
        this.tableTypes = types;
    }

    /**
     * Creates the database model.
     * 
     * @return The database model
     */
    public Database getDatabase() throws SQLException
    {
        Database db = new Database();

        for (Iterator it = getTables().iterator(); it.hasNext();)
        {
            db.addTable((Table)it.next());
        }
        return db;
    }

    /**
     * Returns a list of {@link Table} instances for the tables in the database.
     * 
     * @return The list of tables
     */
    public List getTables() throws SQLException
    {
        ResultSet tableData = null;

        try
        {
            tableData = connection.getMetaData().getTables(catalog, schema, "%", tableTypes);
            
            Set  availableColumns = determineAvailableColumns(tableData);
            List tables           = new ArrayList();

            while (tableData.next())
            {
                String tableName = getValueAsString(tableData, "TABLE_NAME", availableColumns, null);
                
                if ((tableName != null) && (tableName.length() > 0))
                {
                    Table table = new Table();
    
                    table.setName(tableName);
                    table.setType(getValueAsString(tableData, "TABLE_TYPE", availableColumns, "UNKNOWN"));
                    table.setCatalog(getValueAsString(tableData, "TABLE_CAT", availableColumns, null));
                    table.setSchema(getValueAsString(tableData, "TABLE_SCHEM", availableColumns, null));
                    table.setRemarks(getValueAsString(tableData, "REMARKS", availableColumns, ""));
                    tables.add(table);
                }
            }

            for (Iterator it = tables.iterator(); it.hasNext();)
            {
                Table table = (Table)it.next();

                for (Iterator columnIt = getColumnsForTable(table.getName()).iterator(); columnIt.hasNext();)
                {
                    table.addColumn((Column)columnIt.next());
                }
                for (Iterator fkIt = getForeignKeysForTable(table.getName()).iterator(); fkIt.hasNext();)
                {
                    table.addForeignKey((ForeignKey)fkIt.next());
                }
                for (Iterator idxIt = getIndicesForTable(table.getName()).iterator(); idxIt.hasNext();)
                {
                    table.addIndex((Index)idxIt.next());
                }
            }
            return tables;
        }
        finally
        {
            if (tableData != null)
            {
                tableData.close();
            }
        }
    }

    /**
     * Returns a list of {@link Column} instances for the indicated table.
     * 
     * @param tableName The name of the table
     * @return The list of columns
     */
    private List getColumnsForTable(String tableName) throws SQLException
    {
        ResultSet columnData = null;

        try
        {
            columnData = connection.getMetaData().getColumns(catalog, schema, tableName, null);

            Set  availableColumns = determineAvailableColumns(columnData);
            List columns          = new ArrayList();
            List primaryKeys      = getPrimaryKeysForTable(tableName);

            while (columnData.next())
            {
                Column col = new Column();

                col.setName(getValueAsString(columnData, "COLUMN_NAME", availableColumns, "UNKNOWN"));
                col.setTypeCode(getValueAsInt(columnData, "DATA_TYPE", availableColumns, java.sql.Types.OTHER));
                col.setPrecisionRadix(getValueAsInt(columnData, "NUM_PREC_RADIX", availableColumns, 10));
                col.setScale(getValueAsInt(columnData, "DECIMAL_DIGITS", availableColumns, 0));
                // we're setting the size after the precision and radix in case
                // the database prefers to return them in the size value 
                col.setSize(getValueAsString(columnData, "COLUMN_SIZE", availableColumns, (String)defaultSizes.get(new Integer(col.getTypeCode()))));
                col.setRequired("NO".equalsIgnoreCase(getValueAsString(columnData, "IS_NULLABLE", availableColumns, "YES").trim()));
                if (primaryKeys.contains(col.getName()))
                {
                    col.setPrimaryKey(true);
                }
                else
                {
                    col.setPrimaryKey(false);
                }

                // sometimes the default comes back with parenthesis around it (jTDS/mssql)
                String columnDefaultValue = getValueAsString(columnData, "COLUMN_DEF", availableColumns, null);

                if (columnDefaultValue != null)
                {
                    Matcher m = defaultPattern.matcher(columnDefaultValue);

                    if (m.matches())
                    {
                        columnDefaultValue = m.group(1);
                    }
                    col.setDefaultValue(columnDefaultValue);
                }
                columns.add(col);
            }
            return columns;
        }
        finally
        {
            if (columnData != null)
            {
                columnData.close();
            }
        }
    }

    /**
     * Retrieves a list of the columns composing the primary key for a given
     * table.
     *
     * @param tableName The name of the table from which to retrieve PK information
     * @return The list of the primary key column names
     */
    public List getPrimaryKeysForTable(String tableName) throws SQLException
    {
        List      pks   = new ArrayList();
        ResultSet pkData = null;

        try
        {
            pkData = connection.getMetaData().getPrimaryKeys(catalog, schema, tableName);
            while (pkData.next())
            {
                pks.add(pkData.getString("COLUMN_NAME"));
            }
        }
        catch (SQLException ex)
        {
            log.warn("Could not determine the primary keys of table "+tableName, ex);
        }
        finally
        {
            if (pkData != null)
            {
                pkData.close();
            }
        }
        return pks;
    }

    /**
     * Retrieves a list of the foreign keys of the indicated table.
     *
     * @param tableName The name of the table from which to retrieve FK information
     * @return The list of foreign keys
     */
    public List getForeignKeysForTable(String tableName) throws SQLException
    {
        List      fks    = new ArrayList();
        ResultSet fkData = null;

        try
        {
            fkData = connection.getMetaData().getImportedKeys(catalog, schema, tableName);

            Set        availableColumns = determineAvailableColumns(fkData);
            String     prevPkTable      = null;
            ForeignKey currFk           = null;

            while (fkData.next())
            {
                String pkTable     = getValueAsString(fkData, "PKTABLE_NAME", availableColumns, null);
                short  keySequence = getValueAsShort(fkData, "KEY_SEQ", availableColumns, (short)0);

                // a new foreign key definition can only be identified by the changed referenced table
                if (!pkTable.equals(prevPkTable) || (keySequence == 1))
                {
                    if (currFk != null)
                    {
                        fks.add(currFk);
                    }
                    currFk = new ForeignKey();
                    currFk.setForeignTable(pkTable);
                    prevPkTable = pkTable;
                }
                Reference ref = new Reference();

                ref.setForeign(getValueAsString(fkData, "PKCOLUMN_NAME", availableColumns, null));
                ref.setLocal(getValueAsString(fkData, "FKCOLUMN_NAME", availableColumns, null));
                currFk.addReference(ref);
            }
            if (currFk != null)
            {
                fks.add(currFk);
                currFk = null;
            }
        }
        catch (SQLException ex)
        {
            log.warn("Could not determine the foreignkeys of table "+tableName, ex);
        }
        finally
        {
            if (fkData != null)
            {
                fkData.close();
            }
        }
        return fks;
    }

    /**
     * Determines the indices for the indicated table.
     * 
     * @param tableName The name of the table
     * @return The list of indices
     */
    private List getIndicesForTable(String tableName) throws SQLException
    {
        ResultSet indexData = null;
        List      indices   = new ArrayList();

        try 
        {
            indexData = connection.getMetaData().getIndexInfo(catalog, schema, tableName, false, false);

            Set availableColumns = determineAvailableColumns(indexData);
            Map indicesByName    = new LinkedMap();

            while (indexData.next())
            {
                String indexName = getValueAsString(indexData, "INDEX_NAME", availableColumns, null);
                Index  index     = (Index)indicesByName.get(indexName);

                if ((index == null) && (indexName != null))
                {
                    index = new Index();

                    index.setName(indexName);
                    index.setUnique(!getValueAsBoolean(indexData, "NON_UNIQUE", availableColumns, true));
                    indicesByName.put(indexName, index);
                }
                if (index != null)
                {
                    IndexColumn ic = new IndexColumn();

                    ic.setName(getValueAsString(indexData, "COLUMN_NAME", availableColumns, null));
                    index.addIndexColumn(ic);
                }
            }
            indices.addAll(indicesByName.values());
        }
        catch (SQLException ex)
        {
            log.trace("Could determine the indices for the table "+tableName, ex);
        }
        finally
        {
            if (indexData != null)
            {
                indexData.close();
            }
        }
        return indices;
    }

    /**
     * Determines the columns available in the given result set, and returns them (in upper case)
     * in a set.
     *  
     * @param data The result set
     * @return The columns present in the result set
     */
    private Set determineAvailableColumns(ResultSet data) throws SQLException
    {
        Set               result   = new HashSet();
        ResultSetMetaData metaData = data.getMetaData();

        for (int idx = 1; idx <= metaData.getColumnCount(); idx++)
        {
            result.add(metaData.getColumnName(idx).toUpperCase());
        }
        return result;
    }

    /**
     * Retrieves the value of the specified column as a string. If the column is not present, then
     * the default value is returned.
     * 
     * @param data             The data
     * @param columnName       The name of the column
     * @param availableColumns The available columns
     * @param defaultValue     The default value to use if the column is not present
     * @return The value
     */
    private String getValueAsString(ResultSet data, String columnName, Set availableColumns, String defaultValue) throws SQLException
    {
        return availableColumns.contains(columnName) ? data.getString(columnName) : defaultValue;
    }

    /**
     * Retrieves the value of the specified column as an integer. If the column is not present, then
     * the default value is returned.
     * 
     * @param data             The data
     * @param columnName       The name of the column
     * @param availableColumns The available columns
     * @param defaultValue     The default value to use if the column is not present
     * @return The value
     */
    private int getValueAsInt(ResultSet data, String columnName, Set availableColumns, int defaultValue) throws SQLException
    {
        return availableColumns.contains(columnName) ? data.getInt(columnName) : defaultValue;
    }

    /**
     * Retrieves the value of the specified column as a short integer. If the column is not present, then
     * the default value is returned.
     * 
     * @param data             The data
     * @param columnName       The name of the column
     * @param availableColumns The available columns
     * @param defaultValue     The default value to use if the column is not present
     * @return The value
     */
    private short getValueAsShort(ResultSet data, String columnName, Set availableColumns, short defaultValue) throws SQLException
    {
        return availableColumns.contains(columnName) ? data.getShort(columnName) : defaultValue;
    }

    /**
     * Retrieves the value of the specified column as a boolean value. If the column is not present, then
     * the default value is returned.
     * 
     * @param data             The data
     * @param columnName       The name of the column
     * @param availableColumns The available columns
     * @param defaultValue     The default value to use if the column is not present
     * @return The value
     */
    private boolean getValueAsBoolean(ResultSet data, String columnName, Set availableColumns, boolean defaultValue) throws SQLException
    {
        return availableColumns.contains(columnName) ? data.getBoolean(columnName) : defaultValue;
    }
}
