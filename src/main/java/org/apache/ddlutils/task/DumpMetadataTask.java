package org.apache.ddlutils.task;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.io.PrettyPrintingXmlWriter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * A simple helper task that dumps information about a database using JDBC.
 * 
 * @version $Revision: 289996 $
 * @ant.task name="dumpMetadata"
 */
public class DumpMetadataTask extends Task
{
    /** Methods that are filtered when enumerating the properties. */
    private static final String[] IGNORED_PROPERTY_METHODS = { "getConnection", "getCatalogs", "getSchemas" };

    /** The data source to use for accessing the database. */
    private BasicDataSource _dataSource;
    /** The file to write the dump to. */
    private File _outputFile = null;
    /** The encoding of the XML output file. */
    private String _outputEncoding = "UTF-8";
    /** The database catalog(s) to read. */
    private String _catalogPattern = "%";
    /** The database schema(s) to read. */
    private String _schemaPattern = "%";
    /** The pattern for reading all tables. */
    private String _tablePattern = "%";
    /** The pattern for reading all procedures. */
    private String _procedurePattern = "%";
    /** The pattern for reading all columns. */
    private String _columnPattern = "%";
    /** The tables types to read; <code>null</code> or an empty list means that we shall read every type. */
    private String[] _tableTypes = null;
    /** Whether to read tables. */
    private boolean _dumpTables = true;
    /** Whether to read procedures. */
    private boolean _dumpProcedures = true;

    /**
     * Adds the data source to use for accessing the database.
     * 
     * @param dataSource The data source
     */
    public void addConfiguredDatabase(BasicDataSource dataSource)
    {
        _dataSource = dataSource;
    }

    /**
     * Specifies the output file to which the database metadata is written to.
     *
     * @param outputFile The output file
     * @ant.required
     */
    public void setOutputFile(File outputFile)
    {
        _outputFile = outputFile;
    }

    /**
     * Specifies the encoding of the output file.
     *
     * @param encoding The encoding
     * @ant.not-required Per default, <code>UTF-8</code> is used.
     */
    public void setOutputEncoding(String encoding)
    {
        _outputEncoding = encoding;
    }

    /**
     * Sets the catalog pattern used when accessing the database.
     *
     * @param catalogPattern The catalog pattern
     * @ant.not-required Per default, no specific catalog is used (value <code>%</code>).
     */
    public void setCatalogPattern(String catalogPattern)
    {
        _catalogPattern = ((catalogPattern == null) || (catalogPattern.length() == 0) ? null : catalogPattern);
    }

    /**
     * Sets the schema pattern used when accessing the database.
     *
     * @param schemaPattern The schema pattern
     * @ant.not-required Per default, no specific schema is used (value <code>%</code>).
     */
    public void setSchemaPattern(String schemaPattern)
    {
        _schemaPattern = ((schemaPattern == null) || (schemaPattern.length() == 0) ? null : schemaPattern);
    }

    /**
     * Specifies the table to be processed. For details see {@link DatabaseMetaData#getTables(String, String, String, String[])}.
     *
     * @param tablePattern The table pattern
     * @ant.not-required By default, all tables are read (value <code>%</code>).
     */
    public void setTablePattern(String tablePattern)
    {
        _tablePattern = ((tablePattern == null) || (tablePattern.length() == 0) ? null : tablePattern);
    }

    /**
     * Specifies the procedures to be processed. For details and typical table types see {@link DatabaseMetaData#getProcedures(String, String, String)}.
     *
     * @param procedurePattern The procedure pattern
     * @ant.not-required By default, all procedures are read (value <code>%</code>).
     */
    public void setProcedurePattern(String procedurePattern)
    {
        _procedurePattern = ((procedurePattern == null) || (procedurePattern.length() == 0) ? null : procedurePattern);
    }

    /**
     * Specifies the columns to be processed. For details and typical table types see {@link DatabaseMetaData#getColumns(String, String, String, String)}.
     *
     * @param columnPattern The column pattern
     * @ant.not-required By default, all columns are read (value <code>%</code>).
     */
    public void setColumnPattern(String columnPattern)
    {
        _columnPattern = ((columnPattern == null) || (columnPattern.length() == 0) ? null : columnPattern);
    }

    /**
     * Specifies the table types to be processed. For details and typical table types see {@link DatabaseMetaData#getTables(String, String, String, String[])}.
     *
     * @param tableTypes The table types to read
     * @ant.not-required By default, all types of tables are read.
     */
    public void setTableTypes(String tableTypes)
    {
        ArrayList types = new ArrayList();

        if (tableTypes != null)
        {
            StringTokenizer tokenizer = new StringTokenizer(tableTypes, ",");

            while (tokenizer.hasMoreTokens())
            {
                String token = tokenizer.nextToken().trim();

                if (token.length() > 0)
                {
                    types.add(token);
                }
            }
        }
        _tableTypes = (String[])types.toArray(new String[types.size()]);
    }

    /**
     * Specifies whether procedures shall be read from the database.
     *
     * @param readProcedures <code>true</code> if procedures shall be read
     * @ant.not-required By default, procedures are read.
     */
    public void setDumpProcedures(boolean readProcedures)
    {
        _dumpProcedures = readProcedures;
    }

    /**
     * Specifies whether tables shall be read from the database.
     *
     * @param readTables <code>true</code> if tables shall be read
     * @ant.not-required By default, tables are read.
     */
    public void setDumpTables(boolean readTables)
    {
        _dumpTables = readTables;
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws BuildException
    {
        if (_dataSource == null)
        {
            log("No data source specified, so there is nothing to do.", Project.MSG_INFO);
            return;
        }

        Connection   connection = null;
        OutputStream output     = null;

        try
        {
            connection = _dataSource.getConnection();

            if (_outputFile == null)
            {
                output = System.out;
            }
            else
            {
                output = new FileOutputStream(_outputFile);
            }

            PrettyPrintingXmlWriter xmlWriter = new PrettyPrintingXmlWriter(output, _outputEncoding);

            xmlWriter.writeDocumentStart();
            xmlWriter.writeElementStart(null, "metadata");
            xmlWriter.writeAttribute(null, "driverClassName", _dataSource.getDriverClassName());
            
            dumpMetaData(xmlWriter, connection.getMetaData());

            xmlWriter.writeDocumentEnd();
        }
        catch (Exception ex)
        {
            throw new BuildException(ex);
        }
        finally
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException ex)
                {}
            }
            if ((_outputFile != null) && (output != null))
            {
                try
                {
                    output.close();
                }
                catch (IOException ex)
                {}
            }
        }
    }

    /**
     * Dumps the database meta data into XML elements under the given element.
     * 
     * @param element  The XML element
     * @param metaData The meta data
     */
    private void dumpMetaData(PrettyPrintingXmlWriter xmlWriter, DatabaseMetaData metaData) throws NoSuchMethodException,
                                                                                                   IllegalAccessException,
                                                                                                   InvocationTargetException,
                                                                                                   SQLException
    {
        // We rather iterate over the methods because most metadata properties
        // do not follow the bean naming standard
        Method[] methods  = metaData.getClass().getMethods();
        Set      filtered = new HashSet(Arrays.asList(IGNORED_PROPERTY_METHODS));

        for (int idx = 0; idx < methods.length; idx++)
        {
            // only no-arg methods that return something and that are not defined in Object
            // we also filter certain methods
            if ((methods[idx].getParameterTypes().length == 0) && 
                (methods[idx].getReturnType() != null) &&
                (Object.class != methods[idx].getDeclaringClass()) &&
                !filtered.contains(methods[idx].getName()))
            {
                dumpProperty(xmlWriter, metaData, methods[idx]);
            }
        }
        dumpCatalogsAndSchemas(xmlWriter, metaData);
        if (_dumpTables)
        {
            dumpTables(xmlWriter, metaData);
        }
        if (_dumpProcedures)
        {
            dumpProcedures(xmlWriter, metaData);
        }
    }

    /**
     * Dumps the property represented by the given method.
     * 
     * @param parent     The parent XML element
     * @param obj        The instance we're working on
     * @param propGetter The method for accessing the property
     */
    private void dumpProperty(PrettyPrintingXmlWriter xmlWriter, Object obj, Method propGetter)
    {
        try
        {
            addProperty(xmlWriter, getPropertyName(propGetter.getName()), propGetter.invoke(obj, null));
        }
        catch (Throwable ex)
        {
            log("Could not dump property "+propGetter.getName()+": "+ex.getStackTrace(), Project.MSG_ERR);
        }
    }

    /**
     * Adds a property to the given element, either as an attribute (primitive value or
     * string) or as a sub element.
     * 
     * @param element The XML element
     * @param name    The name of the property
     * @param value   The value of the property
     */
    private void addProperty(PrettyPrintingXmlWriter xmlWriter, String name, Object value)
    {
        if (value != null)
        {
            if (value.getClass().isArray())
            {
                addArrayProperty(xmlWriter, name, (Object[])value);
            }
            else if (value.getClass().isPrimitive() || (value instanceof String))
            {
                xmlWriter.writeAttribute(null, name, value.toString());
            }
            else if (value instanceof ResultSet)
            {
                addResultSetProperty(xmlWriter, name, (ResultSet)value);
            }
        }
    }

    /**
     * Adds a property to the given XML element that is represented as an array.
     * 
     * @param element The XML element
     * @param name    The name of the property
     * @param values  The values of the property
     */
    private void addArrayProperty(PrettyPrintingXmlWriter xmlWriter, String name, Object[] values)
    {
        String propName = name;

        if (propName.endsWith("s"))
        {
            propName = propName.substring(0, propName.length() - 1);
        }

        xmlWriter.writeElementStart(null, propName + "s");
        for (int idx = 0; idx < values.length; idx++)
        {
            addProperty(xmlWriter, "value", values[idx]);
        }
        xmlWriter.writeElementEnd();
    }
    
    /**
     * Adds a property to the given XML element that is represented as a result set.
     * 
     * @param element The XML element
     * @param name    The name of the property
     * @param result  The values of the property as a result set
     */
    private void addResultSetProperty(PrettyPrintingXmlWriter xmlWriter, String name, ResultSet result)
    {
        String propName = name;

        if (propName.endsWith("s"))
        {
            propName = propName.substring(0, propName.length() - 1);
        }

        try
        {
            ResultSetMetaData metaData = result.getMetaData();
    
            xmlWriter.writeElementStart(null, propName + "s");
            try
            {
                while (result.next())
                {
                    xmlWriter.writeElementStart(null, propName);
        
                    try
                    {
                        for (int idx = 1; idx <= metaData.getColumnCount(); idx++)
                        {
                            Object value = result.getObject(idx);
            
                            addProperty(xmlWriter, metaData.getColumnLabel(idx), value);
                        }
                    }
                    finally
                    {
                        xmlWriter.writeElementEnd();
                    }
                }
            }
            finally
            {
                xmlWriter.writeElementEnd();
            }
        }
        catch (SQLException ex)
        {
            log("Could not read the result set metadata: "+ex.getStackTrace(), Project.MSG_ERR);
        }
    }

    /**
     * Derives the property name from the given method name.
     * 
     * @param methodName The method name
     * @return The property name
     */
    private String getPropertyName(String methodName)
    {
        if (methodName.startsWith("get"))
        {
            if (Character.isLowerCase(methodName.charAt(4)))
            {
                return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
            }
            else
            {
                return methodName.substring(3);
            }
        }
        else if (methodName.startsWith("is"))
        {
            if (Character.isLowerCase(methodName.charAt(3)))
            {
                return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
            }
            else
            {
                return methodName.substring(2);
            }
        }
        else
        {
            return methodName;
        }
    }

    private static interface ResultSetXmlOperation
    {
        public ResultSet getResultSet() throws SQLException;
        public void handleRow(PrettyPrintingXmlWriter xmlWriter, ResultSet result) throws SQLException;
        public void handleError(SQLException ex);
    }

    private void performResultSetXmlOperation(PrettyPrintingXmlWriter xmlWriter, String name, ResultSetXmlOperation op)
    {
        ResultSet result = null;

        try
        {
            result = op.getResultSet();

            if (name != null)
            {
                xmlWriter.writeElementStart(null, name);
            }
            try
            {
                while (result.next())
                {
                    op.handleRow(xmlWriter, result);
                }
            }
            finally
            {
                if (name != null)
                {
                    xmlWriter.writeElementEnd();
                }
            }
        }
        catch (SQLException ex)
        {
            op.handleError(ex);
        }
        finally
        {
            if (result != null)
            {
                try
                {
                    result.close();
                }
                catch (SQLException ex)
                {
                    log("Could not close a result set: " + ex.getStackTrace(), Project.MSG_ERR);
                }
            }
        }
    }

    /**
     * Dumps the catalogs and schemas of the database.
     * 
     * @param parent   The parent element
     * @param metaData The database meta data
     */
    private void dumpCatalogsAndSchemas(PrettyPrintingXmlWriter xmlWriter, final DatabaseMetaData metaData)
    {
        performResultSetXmlOperation(xmlWriter, "catalogs", new ResultSetXmlOperation()
        {
            public ResultSet getResultSet() throws SQLException
            {
                return metaData.getCatalogs();
            }

            public void handleRow(PrettyPrintingXmlWriter xmlWriter, ResultSet result) throws SQLException
            {
                String catalogName = result.getString("TABLE_CAT");
                
                if ((catalogName != null) && (catalogName.length() > 0))
                {
                    xmlWriter.writeElementStart(null, "catalog");
                    xmlWriter.writeAttribute(null, "name", catalogName);
                    xmlWriter.writeElementEnd();
                }
            }

            public void handleError(SQLException ex)
            {
                log("Could not read the catalogs from the result set: " + ex.getStackTrace(), Project.MSG_ERR);
            }
        });
        performResultSetXmlOperation(xmlWriter, "schemas", new ResultSetXmlOperation()
        {
            public ResultSet getResultSet() throws SQLException
            {
                return metaData.getSchemas();
            }

            public void handleRow(PrettyPrintingXmlWriter xmlWriter, ResultSet result) throws SQLException
            {
                String schemaName = result.getString("TABLE_SCHEM");
                
                if ((schemaName != null) && (schemaName.length() > 0))
                {
                    xmlWriter.writeElementStart(null, "schema");
                    xmlWriter.writeAttribute(null, "name", schemaName);
                    xmlWriter.writeElementEnd();
                }
            }

            public void handleError(SQLException ex)
            {
                log("Could not read the schemas from the result set: " + ex.getStackTrace(), Project.MSG_ERR);
            }
        });
    }

    /**
     * Dumps all tables.
     * 
     * @param parent   The parent element
     * @param metaData The database metadata
     */
    private void dumpTables(PrettyPrintingXmlWriter xmlWriter, final DatabaseMetaData metaData)
    {
        // First we need the list of supported table types
        final ArrayList tableTypeList = new ArrayList();

        performResultSetXmlOperation(xmlWriter, "tableTypes", new ResultSetXmlOperation()
        {
            public ResultSet getResultSet() throws SQLException
            {
                return metaData.getTableTypes();
            }

            public void handleRow(PrettyPrintingXmlWriter xmlWriter, ResultSet result) throws SQLException
            {
                String tableType = result.getString("TABLE_TYPE");

                tableTypeList.add(tableType);
                xmlWriter.writeElementStart(null, "tableType");
                xmlWriter.writeAttribute(null, "name", tableType);
                xmlWriter.writeElementEnd();
            }

            public void handleError(SQLException ex)
            {
                log("Could not read the table types from the result set: " + ex.getStackTrace(), Project.MSG_ERR);
            }
        });

        final String[] tableTypesToRead;

        if ((_tableTypes == null) || (_tableTypes.length == 0))
        {
            tableTypesToRead = (String[])tableTypeList.toArray(new String[tableTypeList.size()]);
        }
        else
        {
            tableTypesToRead = _tableTypes;
        }

        performResultSetXmlOperation(xmlWriter, "tables", new ResultSetXmlOperation()
        {
            public ResultSet getResultSet() throws SQLException
            {
                return metaData.getTables(_catalogPattern, _schemaPattern, _tablePattern, tableTypesToRead);
            }

            public void handleRow(PrettyPrintingXmlWriter xmlWriter, ResultSet result) throws SQLException
            {
                Set    columns   = getColumnsInResultSet(result);
                String tableName = result.getString("TABLE_NAME");

                if ((tableName != null) && (tableName.length() > 0))
                {
                    String catalog = result.getString("TABLE_CAT");
                    String schema  = result.getString("TABLE_SCHEM");

                    log("Reading table " + ((schema != null) && (schema.length() > 0) ? schema + "." : "") + tableName, Project.MSG_INFO);

                    xmlWriter.writeElementStart(null, "table");
                    xmlWriter.writeAttribute(null, "name", tableName);
                    if (catalog != null)
                    {
                        xmlWriter.writeAttribute(null, "catalog", catalog);
                    }
                    if (schema != null)
                    {
                        xmlWriter.writeAttribute(null, "schema", schema);
                    }
                    addStringAttribute(xmlWriter, "type", result, columns, "TABLE_TYPE");
                    addStringAttribute(xmlWriter, "remarks", result, columns, "REMARKS");
                    addStringAttribute(xmlWriter, "typeName", result, columns, "TYPE_NAME");
                    addStringAttribute(xmlWriter, "typeCatalog", result, columns, "TYPE_CAT");
                    addStringAttribute(xmlWriter, "typeSchema", result, columns, "TYPE_SCHEM");
                    addStringAttribute(xmlWriter, "identifierColumn", result, columns, "SELF_REFERENCING_COL_NAME");
                    addStringAttribute(xmlWriter, "identifierGeneration", result, columns, "REF_GENERATION");
        
                    dumpColumns(xmlWriter, metaData, catalog, schema, tableName);
                    dumpPKs(xmlWriter, metaData, catalog, schema, tableName);
                    dumpVersionColumns(xmlWriter, metaData, catalog, schema, tableName);
                    dumpFKs(xmlWriter, metaData, catalog, schema, tableName);
                    dumpIndexes(xmlWriter, metaData, catalog, schema, tableName);

                    xmlWriter.writeElementEnd();
                }
            }

            public void handleError(SQLException ex)
            {
                log("Could not read the tables from the result set: " + ex.getStackTrace(), Project.MSG_ERR);
            }
        });
    }

    /**
     * Dumps the columns of the indicated table.
     * 
     * @param tableElem   The XML element for the table
     * @param metaData    The database metadata
     * @param catalogName The catalog name
     * @param schemaName  The schema name
     * @param tableName   The table name
     */
    private void dumpColumns(PrettyPrintingXmlWriter xmlWriter,
                             final DatabaseMetaData  metaData,
                             final String            catalogName,
                             final String            schemaName,
                             final String            tableName) throws SQLException
    {
        performResultSetXmlOperation(xmlWriter, null, new ResultSetXmlOperation()
        {
            public ResultSet getResultSet() throws SQLException
            {
                return metaData.getColumns(catalogName, schemaName, tableName, _columnPattern);
            }

            public void handleRow(PrettyPrintingXmlWriter xmlWriter, ResultSet result) throws SQLException
            {
                Set    columns    = getColumnsInResultSet(result);
                String columnName = result.getString("COLUMN_NAME");
                
                if ((columnName != null) && (columnName.length() > 0))
                {
                    xmlWriter.writeElementStart(null, "column");
                    xmlWriter.writeAttribute(null, "name", columnName);

                    addIntAttribute(xmlWriter, "typeCode", result, columns, "DATA_TYPE");
                    addStringAttribute(xmlWriter, "type", result, columns, "TYPE_NAME");
                    addIntAttribute(xmlWriter, "size", result, columns, "COLUMN_SIZE");
                    addIntAttribute(xmlWriter, "digits", result, columns, "DECIMAL_DIGITS");
                    addIntAttribute(xmlWriter, "precision", result, columns, "NUM_PREC_RADIX");
                    if (columns.contains("NULLABLE"))
                    {
                        try
                        {
                            switch (result.getInt("NULLABLE"))
                            {
                                case DatabaseMetaData.columnNoNulls:
                                    xmlWriter.writeAttribute(null, "nullable", "false");
                                    break;
                                case DatabaseMetaData.columnNullable:
                                    xmlWriter.writeAttribute(null, "nullable", "true");
                                    break;
                                default:
                                    xmlWriter.writeAttribute(null, "nullable", "unknown");
                                    break;
                            }
                        }
                        catch (SQLException ex)
                        {
                            log("Could not read the NULLABLE value for colum '" + columnName + "' of table '" + tableName + "' from the result set: " + ex.getStackTrace(), Project.MSG_ERR);
                        }
                    }
                    addStringAttribute(xmlWriter, "remarks", result, columns, "REMARKS");
                    addStringAttribute(xmlWriter, "defaultValue", result, columns, "COLUMN_DEF");
                    addIntAttribute(xmlWriter, "maxByteLength", result, columns, "CHAR_OCTET_LENGTH");
                    addIntAttribute(xmlWriter, "index", result, columns, "ORDINAL_POSITION");
                    if (columns.contains("IS_NULLABLE"))
                    {
                        try
                        {
                            String value = result.getString("IS_NULLABLE");

                            if ("no".equalsIgnoreCase(value))
                            {
                                xmlWriter.writeAttribute(null, "isNullable", "false");
                            }
                            else if ("yes".equalsIgnoreCase(value))
                            {
                                xmlWriter.writeAttribute(null, "isNullable", "true");
                            }
                            else
                            {
                                xmlWriter.writeAttribute(null, "isNullable", "unknown");
                            }
                        }
                        catch (SQLException ex)
                        {
                            log("Could not read the IS_NULLABLE value for colum '" + columnName + "' of table '" + tableName + "' from the result set: " + ex.getStackTrace(), Project.MSG_ERR);
                        }
                    }
                    addStringAttribute(xmlWriter, "refCatalog", result, columns, "SCOPE_CATLOG");
                    addStringAttribute(xmlWriter, "refSchema", result, columns, "SCOPE_SCHEMA");
                    addStringAttribute(xmlWriter, "refTable", result, columns, "SCOPE_TABLE");
                    addShortAttribute(xmlWriter, "sourceTypeCode", result, columns, "SOURCE_DATA_TYPE");

                    xmlWriter.writeElementEnd();
                }
            }

            public void handleError(SQLException ex)
            {
                log("Could not read the colums for table '" + tableName + "' from the result set: "+ex.getStackTrace(), Project.MSG_ERR);
            }
        });
    }

    /**
     * Dumps the primary key columns of the indicated table.
     * 
     * @param tableElem   The XML element for the table
     * @param metaData    The database metadata
     * @param catalogName The catalog name
     * @param schemaName  The schema name
     * @param tableName   The table name
     */
    private void dumpPKs(PrettyPrintingXmlWriter xmlWriter,
                         final DatabaseMetaData  metaData,
                         final String            catalogName,
                         final String            schemaName,
                         final String            tableName) throws SQLException
    {
        performResultSetXmlOperation(xmlWriter, null, new ResultSetXmlOperation()
        {
            public ResultSet getResultSet() throws SQLException
            {
                return metaData.getPrimaryKeys(catalogName, schemaName, tableName);
            }

            public void handleRow(PrettyPrintingXmlWriter xmlWriter, ResultSet result) throws SQLException
            {
                Set    columns    = getColumnsInResultSet(result);
                String columnName = result.getString("COLUMN_NAME");
                
                if ((columnName != null) && (columnName.length() > 0))
                {
                    xmlWriter.writeElementStart(null, "primaryKey");
                    xmlWriter.writeAttribute(null, "column", columnName);

                    addStringAttribute(xmlWriter, "name", result, columns, "PK_NAME");
                    addShortAttribute(xmlWriter, "sequenceNumberInPK", result, columns, "KEY_SEQ");

                    xmlWriter.writeElementEnd();
                }
            }

            public void handleError(SQLException ex)
            {
                log("Could not read the primary keys for table '" + tableName + "' from the result set: " + ex.getStackTrace(), Project.MSG_ERR);
            }
        });
    }

    /**
     * Dumps the versioned (auto-updating) columns of the indicated table.
     * 
     * @param tableElem   The XML element for the table
     * @param metaData    The database metadata
     * @param catalogName The catalog name
     * @param schemaName  The schema name
     * @param tableName   The table name
     */
    private void dumpVersionColumns(PrettyPrintingXmlWriter xmlWriter,
                                    final DatabaseMetaData  metaData,
                                    final String            catalogName,
                                    final String            schemaName,
                                    final String            tableName) throws SQLException
    {
        performResultSetXmlOperation(xmlWriter, null, new ResultSetXmlOperation()
        {
            public ResultSet getResultSet() throws SQLException
            {
                return metaData.getVersionColumns(catalogName, schemaName, tableName);
            }

            public void handleRow(PrettyPrintingXmlWriter xmlWriter, ResultSet result) throws SQLException
            {
                Set    columns    = getColumnsInResultSet(result);
                String columnName = result.getString("COLUMN_NAME");
                
                if ((columnName != null) && (columnName.length() > 0))
                {
                    xmlWriter.writeElementStart(null, "versionedColumn");
                    xmlWriter.writeAttribute(null, "column", columnName);

                    addIntAttribute(xmlWriter, "typeCode", result, columns, "DATA_TYPE");
                    addStringAttribute(xmlWriter, "type", result, columns, "TYPE_NAME");
                    addIntAttribute(xmlWriter, "size", result, columns, "BUFFER_LENGTH");
                    addIntAttribute(xmlWriter, "precision", result, columns, "COLUMN_SIZE");
                    addShortAttribute(xmlWriter, "scale", result, columns, "DECIMAL_DIGITS");
                    if (columns.contains("PSEUDO_COLUMN"))
                    {
                        try
                        {
                            switch (result.getShort("PSEUDO_COLUMN"))
                            {
                                case DatabaseMetaData.versionColumnPseudo:
                                    xmlWriter.writeAttribute(null, "columnType", "pseudo column");
                                    break;
                                case DatabaseMetaData.versionColumnNotPseudo:
                                    xmlWriter.writeAttribute(null, "columnType", "real column");
                                    break;
                                default:
                                    xmlWriter.writeAttribute(null, "columnType", "unknown");
                                    break;
                            }
                        }
                        catch (SQLException ex)
                        {
                            log("Could not read the PSEUDO_COLUMN value for versioned colum '" + columnName + "' of table '" + tableName + "' from the result set: " + ex.getStackTrace(), Project.MSG_ERR);
                        }
                    }
                    xmlWriter.writeElementEnd();
                }
            }

            public void handleError(SQLException ex)
            {
                log("Could not read the versioned columns for table '" + tableName + "' from the result set: " + ex.getStackTrace(), Project.MSG_ERR);
            }
        });
    }

    /**
     * Dumps the foreign key columns of the indicated table to other tables.
     * 
     * @param tableElem   The XML element for the table
     * @param metaData    The database metadata
     * @param catalogName The catalog name
     * @param schemaName  The schema name
     * @param tableName   The table name
     */
    private void dumpFKs(PrettyPrintingXmlWriter xmlWriter,
                         final DatabaseMetaData  metaData,
                         final String            catalogName,
                         final String            schemaName,
                         final String            tableName) throws SQLException
    {
        performResultSetXmlOperation(xmlWriter, null, new ResultSetXmlOperation()
        {
            public ResultSet getResultSet() throws SQLException
            {
                return metaData.getImportedKeys(catalogName, schemaName, tableName);
            }

            public void handleRow(PrettyPrintingXmlWriter xmlWriter, ResultSet result) throws SQLException
            {
                Set columns = getColumnsInResultSet(result);

                xmlWriter.writeElementStart(null, "foreignKey");

                addStringAttribute(xmlWriter, "name", result, columns, "FK_NAME");
                addStringAttribute(xmlWriter, "primaryKeyName", result, columns, "PK_NAME");
                addStringAttribute(xmlWriter, "column", result, columns, "PKCOLUMN_NAME");
                addStringAttribute(xmlWriter, "foreignCatalog", result, columns, "FKTABLE_CAT");
                addStringAttribute(xmlWriter, "foreignSchema", result, columns, "FKTABLE_SCHEM");
                addStringAttribute(xmlWriter, "foreignTable", result, columns, "FKTABLE_NAME");
                addStringAttribute(xmlWriter, "foreignColumn", result, columns, "FKCOLUMN_NAME");
                addShortAttribute(xmlWriter, "sequenceNumberInFK", result, columns, "KEY_SEQ");
                if (columns.contains("UPDATE_RULE"))
                {
                    try
                    {
                        switch (result.getShort("UPDATE_RULE"))
                        {
                            case DatabaseMetaData.importedKeyNoAction:
                                xmlWriter.writeAttribute(null, "updateRule", "no action");
                                break;
                            case DatabaseMetaData.importedKeyCascade:
                                xmlWriter.writeAttribute(null, "updateRule", "cascade PK change");
                                break;
                            case DatabaseMetaData.importedKeySetNull:
                                xmlWriter.writeAttribute(null, "updateRule", "set FK to NULL");
                                break;
                            case DatabaseMetaData.importedKeySetDefault:
                                xmlWriter.writeAttribute(null, "updateRule", "set FK to default");
                                break;
                            default:
                                xmlWriter.writeAttribute(null, "updateRule", "unknown");
                                break;
                        }
                    }
                    catch (SQLException ex)
                    {
                        log("Could not read the UPDATE_RULE value for a foreign key of table '" + tableName + "' from the result set: " + ex.getStackTrace(), Project.MSG_ERR);
                    }
                }
                if (columns.contains("DELETE_RULE"))
                {
                    try
                    {
                        switch (result.getShort("DELETE_RULE"))
                        {
                            case DatabaseMetaData.importedKeyNoAction:
                            case DatabaseMetaData.importedKeyRestrict:
                                xmlWriter.writeAttribute(null, "deleteRule", "no action");
                                break;
                            case DatabaseMetaData.importedKeyCascade:
                                xmlWriter.writeAttribute(null, "deleteRule", "cascade PK change");
                                break;
                            case DatabaseMetaData.importedKeySetNull:
                                xmlWriter.writeAttribute(null, "deleteRule", "set FK to NULL");
                                break;
                            case DatabaseMetaData.importedKeySetDefault:
                                xmlWriter.writeAttribute(null, "deleteRule", "set FK to default");
                                break;
                            default:
                                xmlWriter.writeAttribute(null, "deleteRule", "unknown");
                                break;
                        }
                    }
                    catch (SQLException ex)
                    {
                        log("Could not read the DELETE_RULE value for a foreign key of table '" + tableName + "' from the result set: " + ex.getStackTrace(), Project.MSG_ERR);
                    }
                }
                if (columns.contains("DEFERRABILITY"))
                {
                    try
                    {
                        switch (result.getShort("DEFERRABILITY"))
                        {
                            case DatabaseMetaData.importedKeyInitiallyDeferred:
                                xmlWriter.writeAttribute(null, "deferrability", "initially deferred");
                                break;
                            case DatabaseMetaData.importedKeyInitiallyImmediate:
                                xmlWriter.writeAttribute(null, "deferrability", "immediately deferred");
                                break;
                            case DatabaseMetaData.importedKeyNotDeferrable:
                                xmlWriter.writeAttribute(null, "deferrability", "not deferred");
                                break;
                            default:
                                xmlWriter.writeAttribute(null, "deferrability", "unknown");
                                break;
                        }
                    }
                    catch (SQLException ex)
                    {
                        log("Could not read the DEFERRABILITY value for a foreign key of table '" + tableName + "' from the result set: " + ex.getStackTrace(), Project.MSG_ERR);
                    }
                }
                xmlWriter.writeElementEnd();
            }

            public void handleError(SQLException ex)
            {
                log("Could not determine the foreign keys for table '" + tableName + "': " + ex.getStackTrace(), Project.MSG_ERR);
            }
        });
    }

    /**
     * Dumps the indexes of the indicated table.
     * 
     * @param tableElem   The XML element for the table
     * @param metaData    The database metadata
     * @param catalogName The catalog name
     * @param schemaName  The schema name
     * @param tableName   The table name
     */
    private void dumpIndexes(PrettyPrintingXmlWriter xmlWriter,
                             final DatabaseMetaData  metaData,
                             final String            catalogName,
                             final String            schemaName,
                             final String            tableName) throws SQLException
    {
        performResultSetXmlOperation(xmlWriter, null, new ResultSetXmlOperation()
        {
            public ResultSet getResultSet() throws SQLException
            {
                return metaData.getIndexInfo(catalogName, schemaName, tableName, false, false);
            }

            public void handleRow(PrettyPrintingXmlWriter xmlWriter, ResultSet result) throws SQLException
            {
                Set columns = getColumnsInResultSet(result);

                xmlWriter.writeElementStart(null, "index");

                addStringAttribute(xmlWriter, "name", result, columns, "INDEX_NAME");
                addBooleanAttribute(xmlWriter, "nonUnique", result, columns, "NON_UNIQUE");
                addStringAttribute(xmlWriter, "indexCatalog", result, columns, "INDEX_QUALIFIER");
                if (columns.contains("TYPE"))
                {
                    try
                    {
                        switch (result.getShort("TYPE"))
                        {
                            case DatabaseMetaData.tableIndexStatistic:
                                xmlWriter.writeAttribute(null, "type", "table statistics");
                                break;
                            case DatabaseMetaData.tableIndexClustered:
                                xmlWriter.writeAttribute(null, "type", "clustered");
                                break;
                            case DatabaseMetaData.tableIndexHashed:
                                xmlWriter.writeAttribute(null, "type", "hashed");
                                break;
                            case DatabaseMetaData.tableIndexOther:
                                xmlWriter.writeAttribute(null, "type", "other");
                                break;
                            default:
                                xmlWriter.writeAttribute(null, "type", "unknown");
                                break;
                        }
                    }
                    catch (SQLException ex)
                    {
                        log("Could not read the TYPE value for an index of table '" + tableName + "' from the result set: " + ex.getStackTrace(), Project.MSG_ERR);
                    }
                }
                addStringAttribute(xmlWriter, "column", result, columns, "COLUMN_NAME");
                addShortAttribute(xmlWriter, "sequenceNumberInIndex", result, columns, "ORDINAL_POSITION");
                if (columns.contains("ASC_OR_DESC"))
                {
                    try
                    {
                        String value = result.getString("ASC_OR_DESC");
                        
                        if ("A".equalsIgnoreCase(value))
                        {
                            xmlWriter.writeAttribute(null, "sortOrder", "ascending");
                        }
                        else if ("D".equalsIgnoreCase(value))
                        {
                            xmlWriter.writeAttribute(null, "sortOrder", "descending");
                        }
                        else
                        {
                            xmlWriter.writeAttribute(null, "sortOrder", "unknown");
                        }
                    }
                    catch (SQLException ex)
                    {
                        log("Could not read the ASC_OR_DESC value for an index of table '" + tableName + "' from the result set: " + ex.getStackTrace(), Project.MSG_ERR);
                    }
                }
                addIntAttribute(xmlWriter, "cardinality", result, columns, "CARDINALITY");
                addIntAttribute(xmlWriter, "pages", result, columns, "PAGES");
                addStringAttribute(xmlWriter, "filter", result, columns, "FILTER_CONDITION");
            }

            public void handleError(SQLException ex)
            {
                log("Could not read the indexes for table '" + tableName + "' from the result set: " + ex.getStackTrace(), Project.MSG_ERR);
            }
        });
    }

    /**
     * Dumps all procedures.
     * 
     * @param parent   The parent element
     * @param metaData The database metadata
     */
    private void dumpProcedures(PrettyPrintingXmlWriter xmlWriter, final DatabaseMetaData metaData) throws SQLException
    {
        performResultSetXmlOperation(xmlWriter, "procedures", new ResultSetXmlOperation()
        {
            public ResultSet getResultSet() throws SQLException
            {
                return metaData.getProcedures(_catalogPattern, _schemaPattern, _procedurePattern);
            }

            public void handleRow(PrettyPrintingXmlWriter xmlWriter, ResultSet result) throws SQLException
            {
                Set    columns       = getColumnsInResultSet(result);
                String procedureName = result.getString("PROCEDURE_NAME");
                
                if ((procedureName != null) && (procedureName.length() > 0))
                {
                    String catalog = result.getString("PROCEDURE_CAT");
                    String schema  = result.getString("PROCEDURE_SCHEM");
        
                    log("Reading procedure " + ((schema != null) && (schema.length() > 0) ? schema + "." : "") + procedureName, Project.MSG_INFO);
    
                    xmlWriter.writeElementStart(null, "procedure");
                    xmlWriter.writeAttribute(null, "name", procedureName);
                    if (catalog != null)
                    {
                        xmlWriter.writeAttribute(null, "catalog", catalog);
                    }
                    if (schema != null)
                    {
                        xmlWriter.writeAttribute(null, "schema", schema);
                    }
                    addStringAttribute(xmlWriter, "remarks", result, columns, "REMARKS");
                    if (columns.contains("PROCEDURE_TYPE"))
                    {
                        try
                        {
                            switch (result.getShort("PROCEDURE_TYPE"))
                            {
                                case DatabaseMetaData.procedureReturnsResult:
                                    xmlWriter.writeAttribute(null, "type", "returns result");
                                    break;
                                case DatabaseMetaData.procedureNoResult:
                                    xmlWriter.writeAttribute(null, "type", "doesn't return result");
                                    break;
                                case DatabaseMetaData.procedureResultUnknown:
                                    xmlWriter.writeAttribute(null, "type", "may return result");
                                    break;
                                default:
                                    xmlWriter.writeAttribute(null, "type", "unknown");
                                    break;
                            }
                        }
                        catch (SQLException ex)
                        {
                            log("Could not read the PROCEDURE_TYPE value for the procedure '" + procedureName + "' from the result set: " + ex.getStackTrace(), Project.MSG_ERR);
                        }
                    }
        
                    dumpProcedure(xmlWriter, metaData, "%", "%", procedureName);
                    xmlWriter.writeElementEnd();
                }
            }

            public void handleError(SQLException ex)
            {
                log("Could not read the procedures from the result set: " + ex.getStackTrace(), Project.MSG_ERR);
            }
        });
    }

    /**
     * Dumps the contents of the indicated procedure.
     * 
     * @param procedureElem The XML element for the procedure
     * @param metaData      The database metadata
     * @param catalogName   The catalog name
     * @param schemaName    The schema name
     * @param procedureName The procedure name
     */
    private void dumpProcedure(PrettyPrintingXmlWriter xmlWriter,
                               final DatabaseMetaData  metaData,
                               final String            catalogName,
                               final String            schemaName,
                               final String            procedureName) throws SQLException
    {
        performResultSetXmlOperation(xmlWriter, null, new ResultSetXmlOperation()
        {
            public ResultSet getResultSet() throws SQLException
            {
                return metaData.getProcedureColumns(catalogName, schemaName, procedureName, _columnPattern);
            }

            public void handleRow(PrettyPrintingXmlWriter xmlWriter, ResultSet result) throws SQLException
            {
                Set    columns    = getColumnsInResultSet(result);
                String columnName = result.getString("COLUMN_NAME");
                
                if ((columnName != null) && (columnName.length() > 0))
                {
                    xmlWriter.writeElementStart(null, "column");
                    xmlWriter.writeAttribute(null, "name", columnName);
                    if (columns.contains("COLUMN_TYPE"))
                    {
                        try
                        {
                            switch (result.getShort("COLUMN_TYPE"))
                            {
                                case DatabaseMetaData.procedureColumnIn:
                                    xmlWriter.writeAttribute(null, "type", "in parameter");
                                    break;
                                case DatabaseMetaData.procedureColumnInOut:
                                    xmlWriter.writeAttribute(null, "type", "in/out parameter");
                                    break;
                                case DatabaseMetaData.procedureColumnOut:
                                    xmlWriter.writeAttribute(null, "type", "out parameter");
                                    break;
                                case DatabaseMetaData.procedureColumnReturn:
                                    xmlWriter.writeAttribute(null, "type", "return value");
                                    break;
                                case DatabaseMetaData.procedureColumnResult:
                                    xmlWriter.writeAttribute(null, "type", "result column in ResultSet");
                                    break;
                                default:
                                    xmlWriter.writeAttribute(null, "type", "unknown");
                                    break;
                            }
                        }
                        catch (SQLException ex)
                        {
                            log("Could not read the COLUMN_TYPE value for the column '" + columnName + "' of procedure '" + procedureName + "' from the result set: " + ex.getStackTrace(), Project.MSG_ERR);
                        }
                    }
        
                    addIntAttribute(xmlWriter, "typeCode", result, columns, "DATA_TYPE");
                    addStringAttribute(xmlWriter, "type", result, columns, "TYPE_NAME");
                    addIntAttribute(xmlWriter, "length", result, columns, "LENGTH");
                    addIntAttribute(xmlWriter, "precision", result, columns, "PRECISION");
                    addShortAttribute(xmlWriter, "short", result, columns, "SCALE");
                    addShortAttribute(xmlWriter, "radix", result, columns, "RADIX");
                    if (columns.contains("NULLABLE"))
                    {
                        try
                        {
                            switch (result.getInt("NULLABLE"))
                            {
                                case DatabaseMetaData.procedureNoNulls:
                                    xmlWriter.writeAttribute(null, "nullable", "false");
                                    break;
                                case DatabaseMetaData.procedureNullable:
                                    xmlWriter.writeAttribute(null, "nullable", "true");
                                    break;
                                default:
                                    xmlWriter.writeAttribute(null, "nullable", "unknown");
                                    break;
                            }
                        }
                        catch (SQLException ex)
                        {
                            log("Could not read the NULLABLE value for the column '" + columnName + "' of procedure '" + procedureName + "' from the result set: " + ex.getStackTrace(), Project.MSG_ERR);
                        }
                    }
                    addStringAttribute(xmlWriter, "remarks", result, columns, "REMARKS");
                }
            }

            public void handleError(SQLException ex)
            {
                log("Could not read the columns for procedure '"+procedureName+"' from the result set: " + ex.getStackTrace(), Project.MSG_ERR);
            }
        });
    }

    /**
     * If the result set contains the indicated column, extracts its value and sets an attribute at the given element.
     * 
     * @param result     The result set
     * @param columns    The columns in the result set
     * @param columnName The name of the column in the result set
     * @param element    The element to add the attribute
     * @param attrName   The name of the attribute to set
     */
    private void addStringAttribute(PrettyPrintingXmlWriter xmlWriter, String attrName, ResultSet result, Set columns, String columnName) throws SQLException
    {
        if (columns.contains(columnName))
        {
            try
            {
                xmlWriter.writeAttribute(null, attrName, result.getString(columnName));
            }
            catch (SQLException ex)
            {
                log("Could not read the value from result set column " + columnName + ":" + ex.getStackTrace(), Project.MSG_ERR);
            }
        }
    }

    /**
     * If the result set contains the indicated column, extracts its int value and sets an attribute at the given element.
     * 
     * @param result     The result set
     * @param columns    The columns in the result set
     * @param columnName The name of the column in the result set
     * @param element    The element to add the attribute
     * @param attrName   The name of the attribute to set
     */
    private void addIntAttribute(PrettyPrintingXmlWriter xmlWriter, String attrName, ResultSet result, Set columns, String columnName) throws SQLException
    {
        if (columns.contains(columnName))
        {
        	try
        	{
        	    xmlWriter.writeAttribute(null, attrName, String.valueOf(result.getInt(columnName)));
        	}
        	catch (SQLException ex)
        	{
        		// A few databases do not comply with the jdbc spec and return a string (or null),
        		// so lets try this just in case
        		String value = result.getString(columnName);

        		if (value != null)
        		{
	        		try
	        		{
	                    xmlWriter.writeAttribute(null, attrName, new Integer(value).toString());
	        		}
	        		catch (NumberFormatException parseEx)
	        		{
	        			log("Could not parse the value from result set column " + columnName + ":" + ex.getStackTrace(), Project.MSG_ERR);
	        		}
        		}
        	}
        }
    }

    /**
     * If the result set contains the indicated column, extracts its short value and sets an attribute at the given element.
     * 
     * @param result     The result set
     * @param columns    The columns in the result set
     * @param columnName The name of the column in the result set
     * @param element    The element to add the attribute
     * @param attrName   The name of the attribute to set
     */
    private void addShortAttribute(PrettyPrintingXmlWriter xmlWriter, String attrName, ResultSet result, Set columns, String columnName) throws SQLException
    {
        if (columns.contains(columnName))
        {
        	try
        	{
                xmlWriter.writeAttribute(null, attrName, String.valueOf(result.getShort(columnName)));
        	}
        	catch (SQLException ex)
        	{
        		// A few databases do not comply with the jdbc spec and return a string (or null),
        		// so lets try strings this just in case
        		String value = result.getString(columnName);

        		if (value != null)
        		{
                    try
                    {
                        xmlWriter.writeAttribute(null, attrName, new Short(value).toString());
                    }
                    catch (NumberFormatException parseEx)
                    {
                        log("Could not parse the value from result set column " + columnName + ":" + ex.getStackTrace(), Project.MSG_ERR);
                    }
        		}
        	}
        }
    }

    /**
     * If the result set contains the indicated column, extracts its boolean value and sets an attribute at the given element.
     * 
     * @param result     The result set
     * @param columns    The columns in the result set
     * @param columnName The name of the column in the result set
     * @param element    The element to add the attribute
     * @param attrName   The name of the attribute to set
     */
    private void addBooleanAttribute(PrettyPrintingXmlWriter xmlWriter, String attrName, ResultSet result, Set columns, String columnName) throws SQLException
    {
        if (columns.contains(columnName))
        {
            try
            {
                xmlWriter.writeAttribute(null, attrName, String.valueOf(result.getBoolean(columnName)));
            }
            catch (SQLException ex)
            {
                // A few databases do not comply with the jdbc spec and return a string (or null),
                // so lets try strings this just in case
                String value = result.getString(columnName);

                if (value != null)
                {
                    try
                    {
                        xmlWriter.writeAttribute(null, attrName, new Boolean(value).toString());
                    }
                    catch (NumberFormatException parseEx)
                    {
                        log("Could not parse the value from result set column " + columnName + ":" + ex.getStackTrace(), Project.MSG_ERR);
                    }
                }
            }
        }
    }
    
    /**
     * Determines the columns that are present in the given result set.
     * 
     * @param resultSet The result set
     * @return The columns
     */
    private Set getColumnsInResultSet(ResultSet resultSet) throws SQLException
    {
        ListOrderedSet    result   = new ListOrderedSet();
        ResultSetMetaData metaData = resultSet.getMetaData();

        for (int idx = 1; idx <= metaData.getColumnCount(); idx++)
        {
            result.add(metaData.getColumnName(idx).toUpperCase());
        }
        
        return result;
    }
}
