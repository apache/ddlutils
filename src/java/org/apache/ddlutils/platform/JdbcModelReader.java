package org.apache.ddlutils.platform;

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
import org.apache.ddlutils.model.NonUniqueIndex;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.UniqueIndex;

/**
 * An utility class to create a Database model from a live database.
 *
 * @author J. Russell Smyth
 * @author Thomas Dudziak
 * @version $Revision$
 */
public class JdbcModelReader
{
    /** The Log to which logging calls will be made. */
    private final Log _log = LogFactory.getLog(JdbcModelReader.class);

    /** Contains default column sizes (minimum sizes that a JDBC-compliant db must support). */
    private HashMap _defaultSizes = new HashMap();
    /** The default database catalog to read. */
    private String _defaultCatalog = "%";
    /** The sefault database schema(s) to read. */
    private String _defaultSchemaPattern = "%";
    /** The table types to recognize per default. */
    private String[] _defaultTableTypes = { "TABLE" };
    /** The pattern to recognize when parsing a default value. */
    private Pattern _defaultPattern = Pattern.compile("\\(\\'?(.*?)\\'?\\)");

    /**
     * Creates a new model reader instance.
     */
    public JdbcModelReader()
    {
        _defaultSizes.put(new Integer(Types.CHAR),          "254");
        _defaultSizes.put(new Integer(Types.VARCHAR),       "254");
        _defaultSizes.put(new Integer(Types.LONGVARCHAR),   "254");
        _defaultSizes.put(new Integer(Types.BINARY),        "254");
        _defaultSizes.put(new Integer(Types.VARBINARY),     "254");
        _defaultSizes.put(new Integer(Types.LONGVARBINARY), "254");
        _defaultSizes.put(new Integer(Types.INTEGER),       "32");
        _defaultSizes.put(new Integer(Types.BIGINT),        "64");
        _defaultSizes.put(new Integer(Types.REAL),          "7,0");
        _defaultSizes.put(new Integer(Types.FLOAT),         "15,0");
        _defaultSizes.put(new Integer(Types.DOUBLE),        "15,0");
        _defaultSizes.put(new Integer(Types.DECIMAL),       "15,15");
        _defaultSizes.put(new Integer(Types.NUMERIC),       "15,15");
    }

    /**
     * Returns the catalog in the database to read per default.
     *
     * @return The default catalog
     */
    public String getDefaultCatalog()
    {
        return _defaultCatalog;
    }

    /**
     * Sets the catalog in the database to read per default.
     * 
     * @param catalog The catalog
     */
    public void setDefaultCatalog(String catalog)
    {
        _defaultCatalog = catalog;
    }

    /**
     * Returns the schema in the database to read per default.
     *
     * @return The default schema
     */
    public String getDefaultSchemaPattern()
    {
        return _defaultSchemaPattern;
    }

    /**
     * Sets the schema in the database to read per default.
     * 
     * @param schemaPattern The schema
     */
    public void setDefaultSchemaPattern(String schemaPattern)
    {
        _defaultSchemaPattern = schemaPattern;
    }

    /**
     * Returns the table types to recognize per default.
     *
     * @return The default table types
     */
    public String[] getDefaultTableTypes()
    {
        return _defaultTableTypes;
    }

    /**
     * Sets the table types to recognize per default. Typical types are "TABLE", "VIEW",
     * "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     * 
     * @param types The table types
     */
    public void setDefaultTableTypes(String[] types)
    {
        _defaultTableTypes = types;
    }

    /**
     * Reads the database model from the given connection.
     * 
     * @param connection The connection
     * @return The database model
     */
    public Database getDatabase(Connection connection) throws SQLException
    {
        return getDatabase(connection, null, null, null);
    }

    /**
     * Reads the database model from the given connection.
     * 
     * @param connection The connection
     * @param catalog    The catalog to acess in the database; use <code>null</code> for the default value
     * @param schema     The schema to acess in the database; use <code>null</code> for the default value
     * @param tableTypes The table types to process; use <code>null</code> or an empty list for the default ones
     * @return The database model
     */
    public Database getDatabase(Connection connection, String catalog, String schema, String[] tableTypes) throws SQLException
    {
        Database db = new Database();

        try 
        {
            db.setName(connection.getCatalog());
            if (catalog == null)
            {
                catalog = db.getName();
            }
        } 
        catch(Exception e) 
        {
            _log.info("Cannot determine the catalog name from connection.");
            if (catalog != null)
            {
                db.setName(catalog);
            }
        }
        for (Iterator it = getTables(connection, catalog, schema, tableTypes).iterator(); it.hasNext();)
        {
            db.addTable((Table)it.next());
        }
        return db;
    }

    /**
     * Returns a list of {@link Table} instances for the tables in the database.
     * 
     * @param connection    The connection
     * @param catalog       The catalog to acess in the database; use <code>null</code> for the default value
     * @param schemaPattern The schema(s) to acess in the database; use <code>null</code> for the default value
     * @param tableTypes    The table types to process; use <code>null</code> or an empty list for the default ones
     * @return The list of tables
     */
    private List getTables(Connection connection, String catalog, String schemaPattern, String[] tableTypes) throws SQLException
    {
        ResultSet tableData = null;

        try
        {
            DatabaseMetaDataWrapper metaData = new DatabaseMetaDataWrapper();

            metaData.setMetaData(connection.getMetaData());
            metaData.setCatalog(catalog == null ? getDefaultCatalog() : catalog);
            metaData.setSchemaPattern(schemaPattern == null ? getDefaultSchemaPattern() : schemaPattern);
            metaData.setTableTypes((tableTypes == null) || (tableTypes.length == 0) ? getDefaultTableTypes() : tableTypes);
            
            tableData = metaData.getTables("%");

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
                    table.setDescription(getValueAsString(tableData, "REMARKS", availableColumns, ""));
                    tables.add(table);
                }
            }

            for (Iterator it = tables.iterator(); it.hasNext();)
            {
                Table table = (Table)it.next();

                for (Iterator columnIt = getColumnsForTable(metaData, table.getName()).iterator(); columnIt.hasNext();)
                {
                    table.addColumn((Column)columnIt.next());
                }
                for (Iterator fkIt = getForeignKeysForTable(metaData, table.getName()).iterator(); fkIt.hasNext();)
                {
                    table.addForeignKey((ForeignKey)fkIt.next());
                }
                for (Iterator idxIt = getIndicesForTable(metaData, table.getName()).iterator(); idxIt.hasNext();)
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
     * @param metaData  The database meta data
     * @param tableName The name of the table
     * @return The list of columns
     */
    private List getColumnsForTable(DatabaseMetaDataWrapper metaData, String tableName) throws SQLException
    {
        ResultSet columnData = null;

        try
        {
            columnData = metaData.getColumns(tableName, null);

            Set  availableColumns = determineAvailableColumns(columnData);
            List columns          = new ArrayList();
            List primaryKeys      = getPrimaryKeysForTable(metaData, tableName);

            while (columnData.next())
            {
                Column col = new Column();

                // As suggested by Alexandre Borgoltz, we're reading the COLUMN_DEF first because Oracle
                // has problems otherwise (it seemingly requires a LONG column to be the first to be read)
                // See also DDLUTILS-29
                String columnDefaultValue = getValueAsString(columnData, "COLUMN_DEF", availableColumns, null);

                if (columnDefaultValue != null)
                {
                    // Sometimes the default comes back with parenthesis around it (jTDS/mssql)
                    Matcher m = _defaultPattern.matcher(columnDefaultValue);

                    if (m.matches())
                    {
                        columnDefaultValue = m.group(1);
                    }
                    col.setDefaultValue(columnDefaultValue);
                }
                col.setName(getValueAsString(columnData, "COLUMN_NAME", availableColumns, "UNKNOWN"));
                col.setTypeCode(getValueAsInt(columnData, "DATA_TYPE", availableColumns, java.sql.Types.OTHER));
                col.setPrecisionRadix(getValueAsInt(columnData, "NUM_PREC_RADIX", availableColumns, 10));
                col.setScale(getValueAsInt(columnData, "DECIMAL_DIGITS", availableColumns, 0));
                // we're setting the size after the precision and radix in case
                // the database prefers to return them in the size value 
                col.setSize(getValueAsString(columnData, "COLUMN_SIZE", availableColumns, (String)_defaultSizes.get(new Integer(col.getTypeCode()))));
                col.setRequired("NO".equalsIgnoreCase(getValueAsString(columnData, "IS_NULLABLE", availableColumns, "YES").trim()));
                col.setDescription(getValueAsString(columnData, "REMARKS", availableColumns, null));                
                if (primaryKeys.contains(col.getName()))
                {
                    col.setPrimaryKey(true);
                }
                else
                {
                    col.setPrimaryKey(false);
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
     * @param metaData  The database meta data
     * @param tableName The name of the table from which to retrieve PK information
     * @return The list of the primary key column names
     */
    private List getPrimaryKeysForTable(DatabaseMetaDataWrapper metaData, String tableName) throws SQLException
    {
        List      pks   = new ArrayList();
        ResultSet pkData = null;

        try
        {
            pkData = metaData.getPrimaryKeys(tableName);
            while (pkData.next())
            {
                pks.add(pkData.getString("COLUMN_NAME"));
            }
        }
        catch (SQLException ex)
        {
            _log.warn("Could not determine the primary keys of table "+tableName, ex);
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
     * @param metaData  The database meta data
     * @param tableName The name of the table from which to retrieve FK information
     * @return The list of foreign keys
     */
    private List getForeignKeysForTable(DatabaseMetaDataWrapper metaData, String tableName) throws SQLException
    {
        List      fks    = new ArrayList();
        ResultSet fkData = null;

        try
        {
            fkData = metaData.getForeignKeys(tableName);

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
                    currFk = new ForeignKey(getValueAsString(fkData, "FK_NAME", availableColumns, null));
                    currFk.setForeignTableName(pkTable);
                    prevPkTable = pkTable;
                }
                Reference ref = new Reference();

                ref.setForeignColumnName(getValueAsString(fkData, "PKCOLUMN_NAME", availableColumns, null));
                ref.setLocalColumnName(getValueAsString(fkData, "FKCOLUMN_NAME", availableColumns, null));
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
            _log.warn("Could not determine the foreignkeys of table "+tableName, ex);
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
     * @param metaData  The database meta data
     * @param tableName The name of the table
     * @return The list of indices
     */
    private List getIndicesForTable(DatabaseMetaDataWrapper metaData, String tableName) throws SQLException
    {
        ResultSet indexData = null;
        List      indices   = new ArrayList();

        try 
        {
            indexData = metaData.getIndices(tableName, false, false);

            Set availableColumns = determineAvailableColumns(indexData);
            Map indicesByName    = new LinkedMap();

            while (indexData.next())
            {
                String  indexName = getValueAsString(indexData, "INDEX_NAME", availableColumns, null);
                boolean isUnique  = !getValueAsBoolean(indexData, "NON_UNIQUE", availableColumns, true);
                Index   index     = (Index)indicesByName.get(indexName);

                if ((index == null) && (indexName != null))
                {
                    if (isUnique)
                    {
                        index = new UniqueIndex();
                    }
                    else
                    {
                        index = new NonUniqueIndex();
                    }

                    index.setName(indexName);
                    indicesByName.put(indexName, index);
                }
                if (index != null)
                {
                    IndexColumn ic = new IndexColumn();

                    ic.setName(getValueAsString(indexData, "COLUMN_NAME", availableColumns, null));
                    index.addColumn(ic);
                }
            }
            indices.addAll(indicesByName.values());
        }
        catch (SQLException ex)
        {
            _log.trace("Could determine the indices for the table "+tableName, ex);
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
