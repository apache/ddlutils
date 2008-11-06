package org.apache.ddlutils.platform.firebird;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.alteration.AddColumnChange;
import org.apache.ddlutils.alteration.AddPrimaryKeyChange;
import org.apache.ddlutils.alteration.ModelComparator;
import org.apache.ddlutils.alteration.PrimaryKeyChange;
import org.apache.ddlutils.alteration.RemoveColumnChange;
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
 * The platform implementation for the Firebird database.
 * It is assumed that the database is configured with sql dialect 3!
 * 
 * @version $Revision: 231306 $
 */
public class FirebirdPlatform extends PlatformImplBase
{
    /** Database name of this platform. */
    public static final String DATABASENAME     = "Firebird";
    /** The standard Firebird jdbc driver. */
    public static final String JDBC_DRIVER      = "org.firebirdsql.jdbc.FBDriver";
    /** The subprotocol used by the standard Firebird driver. */
    public static final String JDBC_SUBPROTOCOL = "firebirdsql";

    /**
     * Creates a new Firebird platform instance.
     */
    public FirebirdPlatform()
    {
        PlatformInfo info = getPlatformInfo();

        info.setMaxIdentifierLength(31);
        info.setSystemForeignKeyIndicesAlwaysNonUnique(true);
        info.setPrimaryKeyColumnAutomaticallyRequired(true);
        info.setCommentPrefix("/*");
        info.setCommentSuffix("*/");
        info.setSupportedOnUpdateActions(new CascadeActionEnum[] { CascadeActionEnum.CASCADE, CascadeActionEnum.SET_DEFAULT, CascadeActionEnum.SET_NULL, CascadeActionEnum.NONE });
        info.setSupportedOnDeleteActions(new CascadeActionEnum[] { CascadeActionEnum.CASCADE, CascadeActionEnum.SET_DEFAULT, CascadeActionEnum.SET_NULL, CascadeActionEnum.NONE });

        info.addNativeTypeMapping(Types.ARRAY,         "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.BINARY,        "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.BIT,           "SMALLINT",           Types.SMALLINT);
        info.addNativeTypeMapping(Types.BLOB,          "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.BOOLEAN,       "SMALLINT",           Types.SMALLINT);
        info.addNativeTypeMapping(Types.CLOB,          "BLOB SUB_TYPE TEXT", Types.LONGVARCHAR);
        info.addNativeTypeMapping(Types.DATALINK,      "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.DISTINCT,      "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.DOUBLE,        "DOUBLE PRECISION");
        info.addNativeTypeMapping(Types.FLOAT,         "DOUBLE PRECISION",   Types.DOUBLE);
        info.addNativeTypeMapping(Types.JAVA_OBJECT,   "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.LONGVARBINARY, "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.LONGVARCHAR,   "BLOB SUB_TYPE TEXT");
        info.addNativeTypeMapping(Types.NULL,          "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.OTHER,         "BLOB",               Types.LONGVARBINARY);
        // This is back-mapped to REAL in the model reader
        info.addNativeTypeMapping(Types.REAL,          "FLOAT");
        info.addNativeTypeMapping(Types.REF,           "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.STRUCT,        "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.TINYINT,       "SMALLINT",           Types.SMALLINT);
        info.addNativeTypeMapping(Types.VARBINARY,     "BLOB",               Types.LONGVARBINARY);

        info.setDefaultSize(Types.VARCHAR, 254);
        info.setDefaultSize(Types.CHAR,    254);

        setSqlBuilder(new FirebirdBuilder(this));
        setModelReader(new FirebirdModelReader(this));
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
        ModelComparator comparator = super.getModelComparator();

        comparator.setCanDropPrimaryKeyColumns(false);
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
                if (change instanceof AddColumnChange)
                {
                    AddColumnChange addColumnChange = (AddColumnChange)change;

                    // Firebird does not apply default values or identity status to existing rows when adding a column
                    return !addColumnChange.getNewColumn().isAutoIncrement() &&
                           ((addColumnChange.getNewColumn().getDefaultValue() == null) && !addColumnChange.getNewColumn().isRequired());
                }
                else
                {
                    return (change instanceof RemoveColumnChange) ||
                           super.isSupported(intermediateTable, change);
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

        if (change.getNextColumn() == null)
        {
            getSqlBuilder().addColumn(currentModel, changedTable, change.getNewColumn());
        }
        else
        {
            if (change.getPreviousColumn() != null)
            {
                prevColumn = changedTable.findColumn(change.getPreviousColumn(), isDelimitedIdentifierModeOn());
            }
            ((FirebirdBuilder)getSqlBuilder()).insertColumn(currentModel,
                                                            changedTable,
                                                            change.getNewColumn(),
                                                            prevColumn);
        }
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

        ((FirebirdBuilder)getSqlBuilder()).dropColumn(changedTable, droppedColumn);
        change.apply(currentModel, isDelimitedIdentifierModeOn());
    }
}
