package org.apache.ddlutils.platform.interbase;

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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.ddlutils.DdlUtilsException;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.alteration.AddColumnChange;
import org.apache.ddlutils.alteration.AddPrimaryKeyChange;
import org.apache.ddlutils.alteration.ModelComparator;
import org.apache.ddlutils.alteration.PrimaryKeyChange;
import org.apache.ddlutils.alteration.RemoveColumnChange;
import org.apache.ddlutils.alteration.TableChange;
import org.apache.ddlutils.alteration.TableDefinitionChangesPredicate;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.CreationParameters;
import org.apache.ddlutils.platform.DefaultTableDefinitionChangesPredicate;
import org.apache.ddlutils.platform.PlatformImplBase;

/**
 * The platform implementation for the Interbase database.
 * 
 * @version $Revision: 231306 $
 */
public class InterbasePlatform extends PlatformImplBase
{
    /** Database name of this platform. */
    public static final String DATABASENAME     = "Interbase";
    /** The interbase jdbc driver. */
    public static final String JDBC_DRIVER      = "interbase.interclient.Driver";
    /** The subprotocol used by the interbase driver. */
    public static final String JDBC_SUBPROTOCOL = "interbase";

    /**
     * Creates a new platform instance.
     */
    public InterbasePlatform()
    {
        PlatformInfo info = getPlatformInfo();

        info.setMaxIdentifierLength(31);
        info.setCommentPrefix("/*");
        info.setCommentSuffix("*/");
        info.setSystemForeignKeyIndicesAlwaysNonUnique(true);
        info.setPrimaryKeyColumnsHaveToBeRequired(true);

        // BINARY and VARBINARY are also handled by the InterbaseBuilder.getSqlType method
        info.addNativeTypeMapping(Types.ARRAY,         "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.BIGINT,        "NUMERIC(18,0)");
        // Theoretically we could use (VAR)CHAR CHARACTER SET OCTETS but the JDBC driver is not
        // able to handle that properly (the byte[]/BinaryStream accessors do not work)
        info.addNativeTypeMapping(Types.BINARY,        "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.BIT,           "SMALLINT",           Types.SMALLINT);
        info.addNativeTypeMapping(Types.BLOB,          "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.BOOLEAN,       "SMALLINT",           Types.SMALLINT);
        info.addNativeTypeMapping(Types.CLOB,          "BLOB SUB_TYPE TEXT");
        info.addNativeTypeMapping(Types.DATALINK,      "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.DISTINCT,      "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.DOUBLE,        "DOUBLE PRECISION");
        info.addNativeTypeMapping(Types.FLOAT,         "DOUBLE PRECISION",   Types.DOUBLE);
        info.addNativeTypeMapping(Types.JAVA_OBJECT,   "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.LONGVARBINARY, "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.LONGVARCHAR,   "BLOB SUB_TYPE TEXT", Types.CLOB);
        info.addNativeTypeMapping(Types.NULL,          "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.OTHER,         "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.REAL,          "FLOAT");
        info.addNativeTypeMapping(Types.REF,           "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.STRUCT,        "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.TINYINT,       "SMALLINT",           Types.SMALLINT);
        info.addNativeTypeMapping(Types.VARBINARY,     "BLOB",               Types.LONGVARBINARY);

        info.setDefaultSize(Types.CHAR,    254);
        info.setDefaultSize(Types.VARCHAR, 254);
        info.setHasSize(Types.BINARY,    false);
        info.setHasSize(Types.VARBINARY, false);
        
        setSqlBuilder(new InterbaseBuilder(this));
        setModelReader(new InterbaseModelReader(this));
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return DATABASENAME;
    }

    /**
     * {@inheritDoc}
     */
    protected void setStatementParameterValue(PreparedStatement statement, int sqlIndex, int typeCode, Object value) throws SQLException
    {
        if (value != null)
        {
            if ((value instanceof byte[]) &&
                ((typeCode == Types.BINARY) || (typeCode == Types.VARBINARY) || (typeCode == Types.BLOB)))
            {
                byte[]               bytes  = (byte[])value;
                ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

                statement.setBinaryStream(sqlIndex, stream, bytes.length);
                return;
            }
            else if ((value instanceof String) && ((typeCode == Types.CLOB) || (typeCode == Types.LONGVARCHAR)))
            {
                // Clob is not supported directly
                statement.setString(sqlIndex, (String)value);
                return;
            }
        }
        super.setStatementParameterValue(statement, sqlIndex, typeCode, value);
    }

    /**
     * {@inheritDoc}
     */
    protected Object extractColumnValue(ResultSet resultSet, String columnName, int columnIdx, int jdbcType) throws SQLException
    {
        boolean useIdx = (columnName == null);

        switch (jdbcType)
        {
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.BLOB:
                BufferedInputStream input = null;

                try
                {
                    input = new BufferedInputStream(useIdx ? resultSet.getBinaryStream(columnIdx) : resultSet.getBinaryStream(columnName));

                    if (resultSet.wasNull())
                    {
                        return null;
                    }
        
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024);
                    byte[]                data   = new byte[1024];
                    int                   numRead;
        
                    while ((numRead = input.read(data, 0, data.length)) != -1)
                    {
                        buffer.write(data, 0, numRead);
                    }
                    input.close();
                    return buffer.toByteArray();
                }
                catch (IOException ex)
                {
                    throw new DdlUtilsException(ex);
                }
                finally
                {
                    if (input != null)
                    {
                        try
                        {
                            input.close();
                        }
                        catch (IOException ex)
                        {
                            getLog().error("Could not close binary stream read from result set", ex);
                        }
                    }
                }
            case Types.LONGVARCHAR:
            case Types.CLOB:
                String value = useIdx ? resultSet.getString(columnIdx) : resultSet.getString(columnName);

                return resultSet.wasNull() ? null : value;
            default:
                return super.extractColumnValue(resultSet, columnName, columnIdx, jdbcType);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected ModelComparator getModelComparator()
    {
        ModelComparator comparator = super.getModelComparator();

        comparator.setCanDropPrimaryKeyColumns(false);
        comparator.setGeneratePrimaryKeyChanges(false);
        return comparator;
    }

    /**
     * {@inheritDoc}
     */
    protected TableDefinitionChangesPredicate getTableDefinitionChangesPredicate()
    {
        return new DefaultTableDefinitionChangesPredicate()
        {
            public boolean areSupported(Table intermediateTable, List changes)
            {
                // Firebird does support adding a primary key, but only if none of the primary
                // key columns have been added within the same session
                if (super.areSupported(intermediateTable, changes))
                {
                    HashSet  addedColumns = new HashSet();
                    String[] pkColNames   = null;

                    for (Iterator it = changes.iterator(); it.hasNext();)
                    {
                        TableChange change = (TableChange)it.next();

                        if (change instanceof AddColumnChange)
                        {
                            addedColumns.add(((AddColumnChange)change).getNewColumn().getName());
                        }
                        else if (change instanceof AddPrimaryKeyChange)
                        {
                            pkColNames = ((AddPrimaryKeyChange)change).getPrimaryKeyColumns();
                        }
                        else if (change instanceof PrimaryKeyChange)
                        {
                            pkColNames = ((PrimaryKeyChange)change).getNewPrimaryKeyColumns();
                        }
                    }
                    if (pkColNames != null)
                    {
                        for (int colIdx = 0; colIdx < pkColNames.length; colIdx++)
                        {
                            if (addedColumns.contains(pkColNames[colIdx]))
                            {
                                return false;
                            }
                        }
                    }
                    return true;
                }
                else
                {
                    return false;
                }
            }

            protected boolean isSupported(Table intermediateTable, TableChange change)
            {
                // Firebird cannot add columns to the primary key or drop columns from it but
                // since we add/drop the primary key with separate changes anyways, this will
                // no problem here
                if ((change instanceof RemoveColumnChange) ||
                    (change instanceof AddColumnChange))
                {
                    return true;
                }
                else
                {
                    return super.isSupported(intermediateTable, change);
                }
            }
        };
    }


    /**
     * Processes the addition of a column to a table.
     * 
     * @param currentModel The current database schema
     * @param params       The parameters used in the creation of new tables. Note that for existing
     *                     tables, the parameters won't be applied
     * @param change       The change object
     */
    public void processChange(Database           currentModel,
                              CreationParameters params,
                              AddColumnChange    change) throws IOException
    {
        Table  changedTable = findChangedTable(currentModel, change);
        Column prevColumn   = null;

        if (change.getPreviousColumn() != null)
        {
            prevColumn = changedTable.findColumn(change.getPreviousColumn(), isDelimitedIdentifierModeOn());
        }
        ((InterbaseBuilder)getSqlBuilder()).insertColumn(currentModel,
                                                         changedTable,
                                                         change.getNewColumn(),
                                                         prevColumn);
        change.apply(currentModel, isDelimitedIdentifierModeOn());
    }

    /**
     * Processes the removal of a column from a table.
     * 
     * @param currentModel The current database schema
     * @param params       The parameters used in the creation of new tables. Note that for existing
     *                     tables, the parameters won't be applied
     * @param change       The change object
     */
    public void processChange(Database           currentModel,
                              CreationParameters params,
                              RemoveColumnChange change) throws IOException
    {
        Table  changedTable  = findChangedTable(currentModel, change);
        Column droppedColumn = changedTable.findColumn(change.getChangedColumn(), isDelimitedIdentifierModeOn());

        ((InterbaseBuilder)getSqlBuilder()).dropColumn(changedTable, droppedColumn);
        change.apply(currentModel, isDelimitedIdentifierModeOn());
    }
}
