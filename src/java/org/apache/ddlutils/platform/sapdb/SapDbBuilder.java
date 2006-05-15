package org.apache.ddlutils.platform.sapdb;

/*
 * Copyright 1999-2006 The Apache Software Foundation.
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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.alteration.AddColumnChange;
import org.apache.ddlutils.alteration.AddPrimaryKeyChange;
import org.apache.ddlutils.alteration.ColumnDefaultValueChange;
import org.apache.ddlutils.alteration.ColumnRequiredChange;
import org.apache.ddlutils.alteration.PrimaryKeyChange;
import org.apache.ddlutils.alteration.RemoveColumnChange;
import org.apache.ddlutils.alteration.RemovePrimaryKeyChange;
import org.apache.ddlutils.alteration.TableChange;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.SqlBuilder;

/**
 * The SQL Builder for SapDB.
 * 
 * @author James Strachan
 * @author Thomas Dudziak
 * @version $Revision$
 */
public class SapDbBuilder extends SqlBuilder
{
    /**
     * Creates a new builder instance.
     * 
     * @param platform The plaftform this builder belongs to
     */
    public SapDbBuilder(Platform platform)
    {
        super(platform);
        addEscapedCharSequence("'", "''");
    }

    /**
     * {@inheritDoc}
     */
    public void dropTable(Table table) throws IOException
    { 
        print("DROP TABLE ");
        printIdentifier(getTableName(table));
        print(" CASCADE");
        printEndOfStatement();
    }

    /**
     * {@inheritDoc}
     */
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        print("DEFAULT SERIAL(1)");
    }

    /**
     * {@inheritDoc}
     */
    protected void processTableStructureChanges(Database currentModel,
                                                Database desiredModel,
                                                Table    sourceTable,
                                                Table    targetTable,
                                                Map      parameters,
                                                List     changes) throws IOException
    {
        // First we drop primary keys as necessary
        for (Iterator changeIt = changes.iterator(); changeIt.hasNext();)
        {
            TableChange change = (TableChange)changeIt.next();

            if (change instanceof RemovePrimaryKeyChange)
            {
                processChange(currentModel, desiredModel, (RemovePrimaryKeyChange)change);
                change.apply(currentModel);
                changeIt.remove();
            }
            else if (change instanceof PrimaryKeyChange)
            {
                PrimaryKeyChange       pkChange       = (PrimaryKeyChange)change;
                RemovePrimaryKeyChange removePkChange = new RemovePrimaryKeyChange(pkChange.getChangedTable(),
                                                                                   pkChange.getOldPrimaryKeyColumns());

                processChange(currentModel, desiredModel, removePkChange);
                removePkChange.apply(currentModel);
            }
        }
        // Next we add/change/remove columns
        // SapDB has a ALTER TABLE MODIFY COLUMN but it is limited regarding the type conversions
        // it can perform, so we don't use it here but rather rebuild the table
        for (Iterator changeIt = changes.iterator(); changeIt.hasNext();)
        {
            TableChange change = (TableChange)changeIt.next();

            if (change instanceof AddColumnChange)
            {
                AddColumnChange addColumnChange = (AddColumnChange)change;

                // SapDB can only add not insert columns
                if (addColumnChange.isAtEnd())
                {
                    processChange(currentModel, desiredModel, addColumnChange);
                    change.apply(currentModel);
                    changeIt.remove();
                }
            }
            else if (change instanceof ColumnDefaultValueChange)
            {
                processChange(currentModel, desiredModel, (ColumnDefaultValueChange)change);
                change.apply(currentModel);
                changeIt.remove();
            }
            else if (change instanceof ColumnRequiredChange)
            {
                processChange(currentModel, desiredModel, (ColumnRequiredChange)change);
                change.apply(currentModel);
                changeIt.remove();
            }
            else if (change instanceof RemoveColumnChange)
            {
                processChange(currentModel, desiredModel, (RemoveColumnChange)change);
                change.apply(currentModel);
                changeIt.remove();
            }
        }
        // Finally we add primary keys
        for (Iterator changeIt = changes.iterator(); changeIt.hasNext();)
        {
            TableChange change = (TableChange)changeIt.next();

            if (change instanceof AddPrimaryKeyChange)
            {
                processChange(currentModel, desiredModel, (AddPrimaryKeyChange)change);
                change.apply(currentModel);
                changeIt.remove();
            }
            else if (change instanceof PrimaryKeyChange)
            {
                PrimaryKeyChange    pkChange    = (PrimaryKeyChange)change;
                AddPrimaryKeyChange addPkChange = new AddPrimaryKeyChange(pkChange.getChangedTable(),
                                                                          pkChange.getNewPrimaryKeyColumns());

                processChange(currentModel, desiredModel, addPkChange);
                addPkChange.apply(currentModel);
                changeIt.remove();
            }
        }
    }

    /**
     * Processes the addition of a column to a table.
     * 
     * @param currentModel The current database schema
     * @param desiredModel The desired database schema
     * @param change       The change object
     */
    protected void processChange(Database        currentModel,
                                 Database        desiredModel,
                                 AddColumnChange change) throws IOException
    {
        print("ALTER TABLE ");
        printlnIdentifier(getTableName(change.getChangedTable()));
        printIndent();
        print("ADD ");
        writeColumn(change.getChangedTable(), change.getNewColumn());
        printEndOfStatement();
    }

    /**
     * Processes the removal of a column from a table.
     * 
     * @param currentModel The current database schema
     * @param desiredModel The desired database schema
     * @param change       The change object
     */
    protected void processChange(Database           currentModel,
                                 Database           desiredModel,
                                 RemoveColumnChange change) throws IOException
    {
        print("ALTER TABLE ");
        printlnIdentifier(getTableName(change.getChangedTable()));
        printIndent();
        print("DROP ");
        printIdentifier(getColumnName(change.getColumn()));
        print(" RELEASE SPACE");
        printEndOfStatement();
    }

    /**
     * Processes the removal of a primary key from a table.
     * 
     * @param currentModel The current database schema
     * @param desiredModel The desired database schema
     * @param change       The change object
     */
    protected void processChange(Database               currentModel,
                                 Database               desiredModel,
                                 RemovePrimaryKeyChange change) throws IOException
    {
        print("ALTER TABLE ");
        printlnIdentifier(getTableName(change.getChangedTable()));
        printIndent();
        print("DROP PRIMARY KEY");
        printEndOfStatement();
    }

    /**
     * Processes the change of the required constraint of a column.
     * 
     * @param currentModel The current database schema
     * @param desiredModel The desired database schema
     * @param change       The change object
     */
    protected void processChange(Database             currentModel,
                                 Database             desiredModel,
                                 ColumnRequiredChange change) throws IOException
    {
        print("ALTER TABLE ");
        printlnIdentifier(getTableName(change.getChangedTable()));
        printIndent();
        print("COLUMN ");
        printIdentifier(getColumnName(change.getChangedColumn()));
        if (change.getChangedColumn().isRequired())
        {
            print(" DEFAULT NULL");
        }
        else
        {
            print(" NOT NULL");
        }
        printEndOfStatement();
    }

    /**
     * Processes the change of the default value of a column.
     * 
     * @param currentModel The current database schema
     * @param desiredModel The desired database schema
     * @param change       The change object
     */
    protected void processChange(Database                 currentModel,
                                 Database                 desiredModel,
                                 ColumnDefaultValueChange change) throws IOException
    {
        print("ALTER TABLE ");
        printlnIdentifier(getTableName(change.getChangedTable()));
        printIndent();
        print("COLUMN ");
        printIdentifier(getColumnName(change.getChangedColumn()));

        Table   curTable   = currentModel.findTable(change.getChangedTable().getName(), getPlatform().isDelimitedIdentifierModeOn());
        Column  curColumn  = curTable.findColumn(change.getChangedColumn().getName(), getPlatform().isDelimitedIdentifierModeOn());
        boolean hasDefault = curColumn.getParsedDefaultValue() != null;

        if (isValidDefaultValue(change.getNewDefaultValue(), curColumn.getTypeCode()))
        {
            if (hasDefault)
            {
                print(" ALTER DEFAULT ");
            }
            else
            {
                print(" ADD DEFAULT ");
            }
            printDefaultValue(change.getNewDefaultValue(), curColumn.getTypeCode());
        }
        else if (hasDefault)
        {
            print(" DROP DEFAULT");
        }
        printEndOfStatement();
    }
}
