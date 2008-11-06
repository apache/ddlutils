package org.apache.ddlutils.platform.mssql;

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
import java.sql.Types;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.DdlUtilsException;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.alteration.AddColumnChange;
import org.apache.ddlutils.alteration.AddPrimaryKeyChange;
import org.apache.ddlutils.alteration.ColumnDefinitionChange;
import org.apache.ddlutils.alteration.ModelComparator;
import org.apache.ddlutils.alteration.PrimaryKeyChange;
import org.apache.ddlutils.alteration.RemoveColumnChange;
import org.apache.ddlutils.alteration.RemovePrimaryKeyChange;
import org.apache.ddlutils.alteration.TableChange;
import org.apache.ddlutils.alteration.TableDefinitionChangesPredicate;
import org.apache.ddlutils.model.CascadeActionEnum;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.CreationParameters;
import org.apache.ddlutils.platform.DefaultTableDefinitionChangesPredicate;
import org.apache.ddlutils.platform.PlatformImplBase;

/**
 * The platform implementation for the Microsoft SQL Server database.
 * 
 * @version $Revision: 231306 $
 */
public class MSSqlPlatform extends PlatformImplBase
{
    /** Database name of this platform. */
    public static final String DATABASENAME         = "MsSql";
    /** The standard SQLServer jdbc driver. */
    public static final String JDBC_DRIVER          = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
    /** The new SQLServer 2005 jdbc driver which can also be used for SQL Server 2000. */
    public static final String JDBC_DRIVER_NEW      = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    /** The subprotocol used by the standard SQL Server driver. */
    public static final String JDBC_SUBPROTOCOL     = "microsoft:sqlserver";
    /** The subprotocol recommended for the newer SQL Server 2005 driver. */
    public static final String JDBC_SUBPROTOCOL_NEW = "sqlserver";
    /** The subprotocol internally returned by the newer SQL Server 2005 driver. */
    public static final String JDBC_SUBPROTOCOL_INTERNAL = "sqljdbc";

    /**
     * Creates a new platform instance.
     */
    public MSSqlPlatform()
    {
        PlatformInfo info = getPlatformInfo();

        info.setMaxIdentifierLength(128);
        info.setPrimaryKeyColumnAutomaticallyRequired(true);
        info.setIdentityColumnAutomaticallyRequired(true);
        info.setMultipleIdentityColumnsSupported(false);
        info.setSupportedOnUpdateActions(new CascadeActionEnum[] { CascadeActionEnum.CASCADE, CascadeActionEnum.NONE });
        info.setSupportedOnDeleteActions(new CascadeActionEnum[] { CascadeActionEnum.CASCADE, CascadeActionEnum.NONE });

        info.addNativeTypeMapping(Types.ARRAY,         "IMAGE",         Types.LONGVARBINARY);
        // BIGINT will be mapped back to BIGINT by the model reader 
        info.addNativeTypeMapping(Types.BIGINT,        "DECIMAL(19,0)");
        info.addNativeTypeMapping(Types.BLOB,          "IMAGE",         Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.BOOLEAN,       "BIT",           Types.BIT);
        info.addNativeTypeMapping(Types.CLOB,          "TEXT",          Types.LONGVARCHAR);
        info.addNativeTypeMapping(Types.DATALINK,      "IMAGE",         Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.DATE,          "DATETIME",      Types.TIMESTAMP);
        info.addNativeTypeMapping(Types.DISTINCT,      "IMAGE",         Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.DOUBLE,        "FLOAT",         Types.FLOAT);
        info.addNativeTypeMapping(Types.INTEGER,       "INT");
        info.addNativeTypeMapping(Types.JAVA_OBJECT,   "IMAGE",         Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.LONGVARBINARY, "IMAGE");
        info.addNativeTypeMapping(Types.LONGVARCHAR,   "TEXT");
        info.addNativeTypeMapping(Types.NULL,          "IMAGE",         Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.OTHER,         "IMAGE",         Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.REF,           "IMAGE",         Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.STRUCT,        "IMAGE",         Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.TIME,          "DATETIME",      Types.TIMESTAMP);
        info.addNativeTypeMapping(Types.TIMESTAMP,     "DATETIME");
        info.addNativeTypeMapping(Types.TINYINT,       "SMALLINT",      Types.SMALLINT);

        info.setDefaultSize(Types.CHAR,       254);
        info.setDefaultSize(Types.VARCHAR,    254);
        info.setDefaultSize(Types.BINARY,     254);
        info.setDefaultSize(Types.VARBINARY,  254);

        setSqlBuilder(new MSSqlBuilder(this));
        setModelReader(new MSSqlModelReader(this));
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return DATABASENAME;
    }

    /**
     * Determines whether we need to use identity override mode for the given table.
     * 
     * @param table The table
     * @return <code>true</code> if identity override mode is needed
     */
    private boolean useIdentityOverrideFor(Table table)
    {
        return isIdentityOverrideOn() &&
               getPlatformInfo().isIdentityOverrideAllowed() &&
               (table.getAutoIncrementColumns().length > 0);
    }

    /**
     * {@inheritDoc}
     */
    protected void beforeInsert(Connection connection, Table table) throws SQLException
    {
        if (useIdentityOverrideFor(table))
        {
            MSSqlBuilder builder = (MSSqlBuilder)getSqlBuilder();
    
            connection.createStatement().execute(builder.getEnableIdentityOverrideSql(table));
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void afterInsert(Connection connection, Table table) throws SQLException
    {
        if (useIdentityOverrideFor(table))
        {
            MSSqlBuilder builder = (MSSqlBuilder)getSqlBuilder();
    
            connection.createStatement().execute(builder.getDisableIdentityOverrideSql(table));
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void beforeUpdate(Connection connection, Table table) throws SQLException
    {
        beforeInsert(connection, table);
    }

    /**
     * {@inheritDoc}
     */
    protected void afterUpdate(Connection connection, Table table) throws SQLException
    {
        afterInsert(connection, table);
    }

    /**
     * {@inheritDoc}
     */
    protected ModelComparator getModelComparator()
    {
        return new MSSqlModelComparator(getPlatformInfo(), getTableDefinitionChangesPredicate(), isDelimitedIdentifierModeOn());
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
                if ((change instanceof RemoveColumnChange) ||
                    (change instanceof AddPrimaryKeyChange) ||
                    (change instanceof PrimaryKeyChange) ||
                    (change instanceof RemovePrimaryKeyChange))
                {
                    return true;
                }
                else if (change instanceof AddColumnChange)
                {
                    AddColumnChange addColumnChange = (AddColumnChange)change;

                    // Sql Server can only add not insert columns, and the cannot be requird unless also
                    // auto increment or with a DEFAULT value
                    return (addColumnChange.getNextColumn() == null) &&
                           (!addColumnChange.getNewColumn().isRequired() ||
                            addColumnChange.getNewColumn().isAutoIncrement() ||
                            !StringUtils.isEmpty(addColumnChange.getNewColumn().getDefaultValue()));
                }
                else if (change instanceof ColumnDefinitionChange)
                {
                    ColumnDefinitionChange colDefChange = (ColumnDefinitionChange)change;
                    Column                 curColumn    = intermediateTable.findColumn(colDefChange.getChangedColumn(), isDelimitedIdentifierModeOn());
                    Column                 newColumn    = colDefChange.getNewColumn();

                    // Sql Server has no way of adding or removing an IDENTITY constraint
                    // Also, Sql Server cannot handle reducing the size (even with the CAST in place)
                    return (curColumn.isAutoIncrement() == colDefChange.getNewColumn().isAutoIncrement()) &&
                           (curColumn.isRequired() || (curColumn.isRequired() == newColumn.isRequired())) &&
                           !ColumnDefinitionChange.isSizeReduced(getPlatformInfo(), curColumn, newColumn);
                }
                else
                {
                    return false;
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    protected Database processChanges(Database model, Collection changes, CreationParameters params) throws IOException, DdlUtilsException
    {
        if (!changes.isEmpty())
        {
            ((MSSqlBuilder)getSqlBuilder()).turnOnQuotation();
        }
        return super.processChanges(model, changes, params);
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

        ((MSSqlBuilder)getSqlBuilder()).dropColumn(changedTable, removedColumn);
        change.apply(currentModel, isDelimitedIdentifierModeOn());
    }

    /**
     * Processes the removal of a primary key from a table.
     * 
     * @param currentModel The current database schema
     * @param params       The parameters used in the creation of new tables. Note that for existing
     *                     tables, the parameters won't be applied
     * @param change       The change object
     */
    public void processChange(Database               currentModel,
                              CreationParameters     params,
                              RemovePrimaryKeyChange change) throws IOException
    {
        Table changedTable = findChangedTable(currentModel, change);

        ((MSSqlBuilder)getSqlBuilder()).dropPrimaryKey(changedTable);
        change.apply(currentModel, isDelimitedIdentifierModeOn());
    }

    /**
     * Processes the change of the column of a table.
     * 
     * @param currentModel The current database schema
     * @param params       The parameters used in the creation of new tables. Note that for existing
     *                     tables, the parameters won't be applied
     * @param change       The change object
     */
    public void processChange(Database               currentModel,
                              CreationParameters     params,
                              ColumnDefinitionChange change) throws IOException
    {
        Table  changedTable  = findChangedTable(currentModel, change);
        Column changedColumn = changedTable.findColumn(change.getChangedColumn(), isDelimitedIdentifierModeOn());

        ((MSSqlBuilder)getSqlBuilder()).recreateColumn(changedTable, changedColumn, change.getNewColumn());
    }
    
    /**
     * Processes the change of the primary key of a table.
     * 
     * @param currentModel The current database schema
     * @param params       The parameters used in the creation of new tables. Note that for existing
     *                     tables, the parameters won't be applied
     * @param change       The change object
     */
    public void processChange(Database           currentModel,
                              CreationParameters params,
                              PrimaryKeyChange   change) throws IOException
    {
        Table    changedTable     = findChangedTable(currentModel, change);
        String[] newPKColumnNames = change.getNewPrimaryKeyColumns();
        Column[] newPKColumns     = new Column[newPKColumnNames.length];

        for (int colIdx = 0; colIdx < newPKColumnNames.length; colIdx++)
        {
            newPKColumns[colIdx] = changedTable.findColumn(newPKColumnNames[colIdx], isDelimitedIdentifierModeOn());
        }
        ((MSSqlBuilder)getSqlBuilder()).dropPrimaryKey(changedTable);
        getSqlBuilder().createPrimaryKey(changedTable, newPKColumns);
        change.apply(currentModel, isDelimitedIdentifierModeOn());
    }
}
