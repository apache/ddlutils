package org.apache.ddlutils.platform.hsqldb;

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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.apache.ddlutils.DdlUtilsException;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.alteration.AddColumnChange;
import org.apache.ddlutils.alteration.AddPrimaryKeyChange;
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
 * The platform implementation for the HsqlDb database.
 * 
 * @version $Revision: 231306 $
 */
public class HsqlDbPlatform extends PlatformImplBase
{
    /** Database name of this platform. */
    public static final String DATABASENAME     = "HsqlDb";
    /** The standard Hsqldb jdbc driver. */
    public static final String JDBC_DRIVER      = "org.hsqldb.jdbcDriver";
    /** The subprotocol used by the standard Hsqldb driver. */
    public static final String JDBC_SUBPROTOCOL = "hsqldb";

    /**
     * Creates a new instance of the Hsqldb platform.
     */
    public HsqlDbPlatform()
    {
        PlatformInfo info = getPlatformInfo();

        info.setDefaultValueUsedForIdentitySpec(true);
        info.setNonPrimaryKeyIdentityColumnsSupported(false);
        info.setIdentityOverrideAllowed(false);
        info.setSystemForeignKeyIndicesAlwaysNonUnique(true);
        info.setPrimaryKeyColumnAutomaticallyRequired(true);
        info.setMixingIdentityAndNormalPrimaryKeyColumnsSupported(false);

        info.addNativeTypeMapping(Types.ARRAY,       "LONGVARBINARY", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.BIT,         "BOOLEAN",       Types.BOOLEAN);
        info.addNativeTypeMapping(Types.BLOB,        "LONGVARBINARY", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.CLOB,        "LONGVARCHAR",   Types.LONGVARCHAR);
        info.addNativeTypeMapping(Types.DATALINK,    "LONGVARBINARY", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.DISTINCT,    "LONGVARBINARY", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.FLOAT,       "DOUBLE",        Types.DOUBLE);
        info.addNativeTypeMapping(Types.JAVA_OBJECT, "OBJECT");
        info.addNativeTypeMapping(Types.NULL,        "LONGVARBINARY", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.REF,         "LONGVARBINARY", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.STRUCT,      "LONGVARBINARY", Types.LONGVARBINARY);
        // JDBC's TINYINT requires a value range of -255 to 255, but HsqlDb's is only -128 to 127
        info.addNativeTypeMapping(Types.TINYINT,     "SMALLINT",      Types.SMALLINT);

        info.setDefaultSize(Types.CHAR,      Integer.MAX_VALUE);
        info.setDefaultSize(Types.VARCHAR,   Integer.MAX_VALUE);
        info.setDefaultSize(Types.BINARY,    Integer.MAX_VALUE);
        info.setDefaultSize(Types.VARBINARY, Integer.MAX_VALUE);

        setSqlBuilder(new HsqlDbBuilder(this));
        setModelReader(new HsqlDbModelReader(this));
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
    public void shutdownDatabase(Connection connection)
    {
        Statement stmt = null;

        try
        {
            stmt = connection.createStatement();
            stmt.executeUpdate("SHUTDOWN");
        }
        catch (SQLException ex)
        {
            throw new DdlUtilsException(ex);
        }
        finally
        {
            closeStatement(stmt);    
        }
    }

    /**
     * {@inheritDoc}
     */
    protected TableDefinitionChangesPredicate getTableDefinitionChangesPredicate()
    {
        return new DefaultTableDefinitionChangesPredicate()
        {
            protected boolean isSupported(Table intermediateTable, TableChange change)
            {
                if (change instanceof RemoveColumnChange)
                {
                    Column column = intermediateTable.findColumn(((RemoveColumnChange)change).getChangedColumn(),
                                                                 isDelimitedIdentifierModeOn());

                    // HsqlDb can only drop columns that are not part of a primary key
                    return !column.isPrimaryKey();
                }
                else if (change instanceof AddColumnChange)
                {
                    AddColumnChange addColumnChange = (AddColumnChange)change; 

                    // adding IDENTITY columns is not supported without a table rebuild because they have to
                    // be PK columns, but we add them to the PK later
                    return addColumnChange.isAtEnd() &&
                           (!addColumnChange.getNewColumn().isRequired() ||
                            (addColumnChange.getNewColumn().getDefaultValue() != null));
                }
                else if (change instanceof AddPrimaryKeyChange)
                {
                    return true;
                }
                else
                {
                    return false;
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
        Column nextColumn   = null;

        if (change.getNextColumn() != null)
        {
            nextColumn = changedTable.findColumn(change.getNextColumn(), isDelimitedIdentifierModeOn());
        }
        ((HsqlDbBuilder)getSqlBuilder()).insertColumn(changedTable, change.getNewColumn(), nextColumn);
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
        Column removedColumn = changedTable.findColumn(change.getChangedColumn(), isDelimitedIdentifierModeOn());

        ((HsqlDbBuilder)getSqlBuilder()).dropColumn(changedTable, removedColumn);
        change.apply(currentModel, isDelimitedIdentifierModeOn());
    }
}
