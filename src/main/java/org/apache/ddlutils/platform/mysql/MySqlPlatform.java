package org.apache.ddlutils.platform.mysql;

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
import java.sql.Types;

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
 * The platform implementation for MySQL.
 * 
 * @version $Revision: 231306 $
 */
public class MySqlPlatform extends PlatformImplBase
{
    /** Database name of this platform. */
    public static final String DATABASENAME     = "MySQL";
    /** The standard MySQL jdbc driver. */
    public static final String JDBC_DRIVER      = "com.mysql.jdbc.Driver";
    /** The old MySQL jdbc driver. */
    public static final String JDBC_DRIVER_OLD  = "org.gjt.mm.mysql.Driver";
    /** The subprotocol used by the standard MySQL driver. */
    public static final String JDBC_SUBPROTOCOL = "mysql";

    /**
     * Creates a new platform instance.
     */
    public MySqlPlatform()
    {
        PlatformInfo info = getPlatformInfo();

        info.setMaxIdentifierLength(64);
        info.setNullAsDefaultValueRequired(true);
        info.setDefaultValuesForLongTypesSupported(false);
        // see http://dev.mysql.com/doc/refman/4.1/en/example-auto-increment.html
        info.setNonPrimaryKeyIdentityColumnsSupported(false);
        info.setMultipleIdentityColumnsSupported(false);
        info.setMixingIdentityAndNormalPrimaryKeyColumnsSupported(false);
        // MySql returns synthetic default values for pk columns
        info.setSyntheticDefaultValueForRequiredReturned(true);
        info.setPrimaryKeyColumnAutomaticallyRequired(true);
        info.setCommentPrefix("#");
        // Double quotes are only allowed for delimiting identifiers if the server SQL mode includes ANSI_QUOTES 
        info.setDelimiterToken("`");
        info.setSupportedOnUpdateActions(new CascadeActionEnum[] { CascadeActionEnum.NONE, CascadeActionEnum.RESTRICT,
                                                                   CascadeActionEnum.CASCADE, CascadeActionEnum.SET_NULL });
        info.setSupportedOnDeleteActions(new CascadeActionEnum[] { CascadeActionEnum.NONE, CascadeActionEnum.RESTRICT,
                                                                   CascadeActionEnum.CASCADE, CascadeActionEnum.SET_NULL });

        info.addNativeTypeMapping(Types.ARRAY,         "LONGBLOB",   Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.BIT,           "TINYINT(1)");
        info.addNativeTypeMapping(Types.BLOB,          "LONGBLOB",   Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.BOOLEAN,       "TINYINT(1)", Types.BIT);
        info.addNativeTypeMapping(Types.CLOB,          "LONGTEXT",   Types.LONGVARCHAR);
        info.addNativeTypeMapping(Types.DATALINK,      "MEDIUMBLOB", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.DISTINCT,      "LONGBLOB",   Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.FLOAT,         "DOUBLE",     Types.DOUBLE);
        info.addNativeTypeMapping(Types.JAVA_OBJECT,   "LONGBLOB",   Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.LONGVARBINARY, "MEDIUMBLOB");
        info.addNativeTypeMapping(Types.LONGVARCHAR,   "MEDIUMTEXT");
        info.addNativeTypeMapping(Types.NULL,          "MEDIUMBLOB", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.NUMERIC,       "DECIMAL",    Types.DECIMAL);
        info.addNativeTypeMapping(Types.OTHER,         "LONGBLOB",   Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.REAL,          "FLOAT");
        info.addNativeTypeMapping(Types.REF,           "MEDIUMBLOB", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.STRUCT,        "LONGBLOB",   Types.LONGVARBINARY);
        // Since TIMESTAMP is not a stable datatype yet, and does not support a higher precision
        // than DATETIME (year to seconds) as of MySQL 5, we map the JDBC type here to DATETIME
        // TODO: Make this configurable
        info.addNativeTypeMapping(Types.TIMESTAMP,     "DATETIME");
        // In MySql, TINYINT has only a range of -128 to 127
        info.addNativeTypeMapping(Types.TINYINT,       "SMALLINT",          Types.SMALLINT);

        info.setDefaultSize(Types.CHAR,      254);
        info.setDefaultSize(Types.VARCHAR,   254);
        info.setDefaultSize(Types.BINARY,    254);
        info.setDefaultSize(Types.VARBINARY, 254);
        
        setSqlBuilder(new MySqlBuilder(this));
        setModelReader(new MySqlModelReader(this));
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
    protected ModelComparator getModelComparator()
    {
        return new MySqlModelComparator(getPlatformInfo(), getTableDefinitionChangesPredicate(), isDelimitedIdentifierModeOn());
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
                if (change instanceof AddColumnChange)
                {
                    AddColumnChange addColumnChange = (AddColumnChange)change;

                    return !addColumnChange.getNewColumn().isAutoIncrement() &&
                           (!addColumnChange.getNewColumn().isRequired() || (addColumnChange.getNewColumn().getDefaultValue() != null));
                }
                else if (change instanceof ColumnDefinitionChange)
                {
                    ColumnDefinitionChange colDefChange = (ColumnDefinitionChange)change;
                    Column                 sourceColumn = intermediateTable.findColumn(colDefChange.getChangedColumn(), isDelimitedIdentifierModeOn());

                    return !ColumnDefinitionChange.isTypeChanged(getPlatformInfo(), sourceColumn, colDefChange.getNewColumn()) &&
                           !ColumnDefinitionChange.isSizeChanged(getPlatformInfo(), sourceColumn, colDefChange.getNewColumn());
                }
                else
                {
                    return (change instanceof RemoveColumnChange) ||
                           (change instanceof AddPrimaryKeyChange) ||
                           (change instanceof PrimaryKeyChange) ||
                           (change instanceof RemovePrimaryKeyChange);
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
        ((MySqlBuilder)getSqlBuilder()).insertColumn(changedTable, change.getNewColumn(), prevColumn);
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
        Table changedTable = findChangedTable(currentModel, change);

        ((MySqlBuilder)getSqlBuilder()).recreateColumn(changedTable, change.getNewColumn());
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

        ((MySqlBuilder)getSqlBuilder()).dropColumn(changedTable, removedColumn);
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

        ((MySqlBuilder)getSqlBuilder()).dropPrimaryKey(changedTable);
        change.apply(currentModel, isDelimitedIdentifierModeOn());
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
        
        ((MySqlBuilder)getSqlBuilder()).dropPrimaryKey(changedTable);
        getSqlBuilder().createPrimaryKey(changedTable, newPKColumns);
        change.apply(currentModel, isDelimitedIdentifierModeOn());
    }
}
