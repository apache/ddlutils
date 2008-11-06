package org.apache.ddlutils.platform.oracle;

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
import java.util.Map;

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.alteration.AddColumnChange;
import org.apache.ddlutils.alteration.AddPrimaryKeyChange;
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
 * The platform for Oracle 8.
 * 
 * TODO: We might support the {@link org.apache.ddlutils.Platform#createDatabase(String, String, String, String, Map)}
 *       functionality via "CREATE SCHEMA"/"CREATE USER" or "CREATE TABLESPACE" ?
 *
 * @version $Revision: 231306 $
 */
public class Oracle8Platform extends PlatformImplBase
{
    /** Database name of this platform. */
    public static final String DATABASENAME              = "Oracle";
    /** The standard Oracle jdbc driver. */
    public static final String JDBC_DRIVER               = "oracle.jdbc.driver.OracleDriver";
    /** The old Oracle jdbc driver. */
    public static final String JDBC_DRIVER_OLD           = "oracle.jdbc.dnlddriver.OracleDriver";
    /** The thin subprotocol used by the standard Oracle driver. */
    public static final String JDBC_SUBPROTOCOL_THIN     = "oracle:thin";
    /** The thin subprotocol used by the standard Oracle driver. */
    public static final String JDBC_SUBPROTOCOL_OCI8     = "oracle:oci8";
    /** The old thin subprotocol used by the standard Oracle driver. */
    public static final String JDBC_SUBPROTOCOL_THIN_OLD = "oracle:dnldthin";

    /**
     * Creates a new platform instance.
     */
    public Oracle8Platform()
    {
        PlatformInfo info = getPlatformInfo();

        info.setMaxIdentifierLength(30);
        info.setIdentityStatusReadingSupported(false);
        info.setPrimaryKeyColumnAutomaticallyRequired(true);
        info.setSupportedOnUpdateActions(new CascadeActionEnum[] { CascadeActionEnum.NONE });
        info.setSupportedOnDeleteActions(new CascadeActionEnum[] { CascadeActionEnum.CASCADE, CascadeActionEnum.SET_NULL, CascadeActionEnum.NONE });

        // Note that the back-mappings are partially done by the model reader, not the driver
        info.addNativeTypeMapping(Types.ARRAY,         "BLOB",             Types.BLOB);
        info.addNativeTypeMapping(Types.BIGINT,        "NUMBER(38)");
        info.addNativeTypeMapping(Types.BINARY,        "RAW",              Types.VARBINARY);
        info.addNativeTypeMapping(Types.BIT,           "NUMBER(1)");
        info.addNativeTypeMapping(Types.BOOLEAN,       "NUMBER(1)",        Types.BIT);
        info.addNativeTypeMapping(Types.DATALINK,      "BLOB",             Types.BLOB);
        info.addNativeTypeMapping(Types.DATE,          "DATE",             Types.TIMESTAMP);
        info.addNativeTypeMapping(Types.DECIMAL,       "NUMBER");
        info.addNativeTypeMapping(Types.DISTINCT,      "BLOB",             Types.BLOB);
        info.addNativeTypeMapping(Types.DOUBLE,        "DOUBLE PRECISION");
        info.addNativeTypeMapping(Types.FLOAT,         "FLOAT",            Types.DOUBLE);
        info.addNativeTypeMapping(Types.JAVA_OBJECT,   "BLOB",             Types.BLOB);
        info.addNativeTypeMapping(Types.LONGVARBINARY, "BLOB",             Types.BLOB);
        info.addNativeTypeMapping(Types.LONGVARCHAR,   "CLOB",             Types.CLOB);
        info.addNativeTypeMapping(Types.NULL,          "BLOB",             Types.BLOB);
        info.addNativeTypeMapping(Types.NUMERIC,       "NUMBER",           Types.DECIMAL);
        info.addNativeTypeMapping(Types.OTHER,         "BLOB",             Types.BLOB);
        info.addNativeTypeMapping(Types.REF,           "BLOB",             Types.BLOB);
        info.addNativeTypeMapping(Types.SMALLINT,      "NUMBER(5)");
        info.addNativeTypeMapping(Types.STRUCT,        "BLOB",             Types.BLOB);
        info.addNativeTypeMapping(Types.TIME,          "DATE",             Types.TIMESTAMP);
        info.addNativeTypeMapping(Types.TIMESTAMP,     "DATE");
        info.addNativeTypeMapping(Types.TINYINT,       "NUMBER(3)");
        info.addNativeTypeMapping(Types.VARBINARY,     "RAW");
        info.addNativeTypeMapping(Types.VARCHAR,       "VARCHAR2");

        info.setDefaultSize(Types.CHAR,       254);
        info.setDefaultSize(Types.VARCHAR,    254);
        info.setDefaultSize(Types.BINARY,     254);
        info.setDefaultSize(Types.VARBINARY,  254);

        setSqlBuilder(new Oracle8Builder(this));
        setModelReader(new Oracle8ModelReader(this));
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
    protected TableDefinitionChangesPredicate getTableDefinitionChangesPredicate()
    {
        // While Oracle has an ALTER TABLE MODIFY statement, it is somewhat limited
        // esp. if there is data in the table, so we don't use it
        return new DefaultTableDefinitionChangesPredicate()
        {
            protected boolean isSupported(Table intermediateTable, TableChange change)
            {
                if ((change instanceof AddPrimaryKeyChange) ||
                    (change instanceof RemovePrimaryKeyChange))
                {
                    return true;
                }
                else if (change instanceof RemoveColumnChange)
                {
                    // TODO: for now we trigger recreating the table, but ideally we should simply add the necessary pk changes
                    RemoveColumnChange removeColumnChange = (RemoveColumnChange)change;
                    Column             column             = intermediateTable.findColumn(removeColumnChange.getChangedColumn(), isDelimitedIdentifierModeOn());

                    return !column.isPrimaryKey();
                }
                else if (change instanceof AddColumnChange)
                {
                    AddColumnChange addColumnChange = (AddColumnChange)change;

                    // Oracle can only add not insert columns
                    // Also, we cannot add NOT NULL columns unless they have a default value
                    return addColumnChange.isAtEnd() &&
                           (!addColumnChange.getNewColumn().isRequired() || (addColumnChange.getNewColumn().getDefaultValue() != null));
                }
                else
                {
                    return false;
                }
            }
        };
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

        ((Oracle8Builder)getSqlBuilder()).dropColumn(changedTable, removedColumn);
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

        ((Oracle8Builder)getSqlBuilder()).dropPrimaryKey(changedTable);
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
        
        ((Oracle8Builder)getSqlBuilder()).dropPrimaryKey(changedTable);
        getSqlBuilder().createPrimaryKey(changedTable, newPKColumns);
        change.apply(currentModel, isDelimitedIdentifierModeOn());
    }
}
