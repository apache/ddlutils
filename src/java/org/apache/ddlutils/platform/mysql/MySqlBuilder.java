package org.apache.ddlutils.platform.mysql;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.alteration.AddColumnChange;
import org.apache.ddlutils.alteration.AddPrimaryKeyChange;
import org.apache.ddlutils.alteration.ColumnChange;
import org.apache.ddlutils.alteration.PrimaryKeyChange;
import org.apache.ddlutils.alteration.RemoveColumnChange;
import org.apache.ddlutils.alteration.RemovePrimaryKeyChange;
import org.apache.ddlutils.alteration.TableChange;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.SqlBuilder;

/**
 * The SQL Builder for MySQL.
 * 
 * @author James Strachan
 * @author John Marshall/Connectria
 * @author Thomas Dudziak
 * @version $Revision$
 */
public class MySqlBuilder extends SqlBuilder
{
    /**
     * Creates a new builder instance.
     * 
     * @param platform The plaftform this builder belongs to
     */
    public MySqlBuilder(Platform platform)
    {
        super(platform);
        // we need to handle the backslash first otherwise the other
        // already escaped sequences would be affected
        addEscapedCharSequence("\\",     "\\\\");
        addEscapedCharSequence("\0",     "\\0");
        addEscapedCharSequence("'",      "\\'");
        addEscapedCharSequence("\"",     "\\\"");
        addEscapedCharSequence("\b",     "\\b");
        addEscapedCharSequence("\n",     "\\n");
        addEscapedCharSequence("\r",     "\\r");
        addEscapedCharSequence("\t",     "\\t");
        addEscapedCharSequence("\u001A", "\\Z");
        addEscapedCharSequence("%",      "\\%");
        addEscapedCharSequence("_",      "\\_");
    }

    /**
     * {@inheritDoc}
     */
    public void dropTable(Table table) throws IOException
    { 
        print("DROP TABLE IF EXISTS ");
        printIdentifier(getTableName(table));
        printEndOfStatement();
    }

    /**
     * {@inheritDoc}
     */
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        print("AUTO_INCREMENT");
    }

    /**
     * {@inheritDoc}
     */
    protected boolean shouldGeneratePrimaryKeys(Column[] primaryKeyColumns)
    {
        // mySQL requires primary key indication for autoincrement key columns
        // I'm not sure why the default skips the pk statement if all are identity
        return true;
    }

    /**
     * {@inheritDoc}
     * Normally mysql will return the LAST_INSERT_ID as the column name for the inserted id.
     * Since ddlutils expects the real column name of the field that is autoincrementing, the
     * column has an alias of that column name.
     */
    public String getSelectLastInsertId(Table table)
    {
        String autoIncrementKeyName = "";
        if (table.getAutoIncrementColumns().length > 0)
        {
            autoIncrementKeyName = table.getAutoIncrementColumns()[0].getName();
        }
        return "SELECT LAST_INSERT_ID() " + autoIncrementKeyName;
    }

    /**
     * {@inheritDoc}
     */
    protected void writeTableCreationStmtEnding(Table table, Map parameters) throws IOException
    {
        if (parameters != null)
        {
            print(" ");
            // MySql supports additional table creation options which are appended
            // at the end of the CREATE TABLE statement
            for (Iterator it = parameters.entrySet().iterator(); it.hasNext();)
            {
                Map.Entry entry = (Map.Entry)it.next();

                print(entry.getKey().toString());
                if (entry.getValue() != null)
                {
                    print("=");
                    print(entry.getValue().toString());
                }
                if (it.hasNext())
                {
                    print(" ");
                }
            }
        }
        super.writeTableCreationStmtEnding(table, parameters);
    }

    /**
     * {@inheritDoc}
     */
    protected void writeExternalForeignKeyDropStmt(Table table, ForeignKey foreignKey) throws IOException
    {
        writeTableAlterStmt(table);
        print("DROP FOREIGN KEY ");
        printIdentifier(getForeignKeyName(table, foreignKey));
        printEndOfStatement();
        writeTableAlterStmt(table);
        print("DROP INDEX ");
        printIdentifier(getForeignKeyName(table, foreignKey));
        printEndOfStatement();
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
        // in order to utilize the ALTER TABLE ADD COLUMN AFTER statement
        // we have to apply the add column changes in the correct order
        // thus we first gather all add column changes and then execute them
        ArrayList addColumnChanges = new ArrayList();

        for (Iterator changeIt = changes.iterator(); changeIt.hasNext();)
        {
            TableChange change = (TableChange)changeIt.next();

            if (change instanceof AddColumnChange)
            {
                addColumnChanges.add(change);
                changeIt.remove();
            }
        }
        for (Iterator changeIt = addColumnChanges.iterator(); changeIt.hasNext();)
        {
            AddColumnChange addColumnChange = (AddColumnChange)changeIt.next();

            processChange(currentModel, desiredModel, addColumnChange);
            addColumnChange.apply(currentModel);
            changeIt.remove();
        }

        ListOrderedSet changedColumns = new ListOrderedSet();
        
        for (Iterator changeIt = changes.iterator(); changeIt.hasNext();)
        {
            TableChange change = (TableChange)changeIt.next();

            if (change instanceof RemoveColumnChange)
            {
                processChange(currentModel, desiredModel, (RemoveColumnChange)change);
                change.apply(currentModel);
            }
            else if (change instanceof AddPrimaryKeyChange)
            {
                processChange(currentModel, desiredModel, (AddPrimaryKeyChange)change);
                change.apply(currentModel);
            }
            else if (change instanceof PrimaryKeyChange)
            {
                processChange(currentModel, desiredModel, (PrimaryKeyChange)change);
                change.apply(currentModel);
            }
            else if (change instanceof RemovePrimaryKeyChange)
            {
                processChange(currentModel, desiredModel, (RemovePrimaryKeyChange)change);
                change.apply(currentModel);
            }
            else
            {
                // we gather all changed columns because we can use the ALTER TABLE MODIFY COLUMN
                // statement for them
                changedColumns.add(((ColumnChange)change).getChangedColumn());
            }
            changeIt.remove();
        }
        for (Iterator columnIt = changedColumns.iterator(); columnIt.hasNext();)
        {
            Column sourceColumn = (Column)columnIt.next();
            Column targetColumn = targetTable.findColumn(sourceColumn.getName(), getPlatform().isDelimitedIdentifierModeOn());

            processColumnChange(sourceTable, targetTable, sourceColumn, targetColumn);
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
        print("ADD COLUMN ");
        writeColumn(change.getChangedTable(), change.getNewColumn());
        if (change.getPreviousColumn() != null)
        {
            print(" AFTER ");
            printIdentifier(getColumnName(change.getPreviousColumn()));
        }
        else
        {
            print(" FIRST");
        }
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
        print("DROP COLUMN ");
        printIdentifier(getColumnName(change.getColumn()));
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
     * Processes the change of the primary key of a table.
     * 
     * @param currentModel The current database schema
     * @param desiredModel The desired database schema
     * @param change       The change object
     */
    protected void processChange(Database         currentModel,
                                 Database         desiredModel,
                                 PrimaryKeyChange change) throws IOException
    {
        print("ALTER TABLE ");
        printlnIdentifier(getTableName(change.getChangedTable()));
        printIndent();
        print("DROP PRIMARY KEY");
        printEndOfStatement();
        writeExternalPrimaryKeysCreateStmt(change.getChangedTable(), change.getNewPrimaryKeyColumns());
    }

    /**
     * Processes a change to a column.
     * 
     * @param sourceTable  The current table
     * @param targetTable  The desired table
     * @param sourceColumn The current column
     * @param targetColumn The desired column
     */
    protected void processColumnChange(Table  sourceTable,
                                       Table  targetTable,
                                       Column sourceColumn,
                                       Column targetColumn) throws IOException
    {
        print("ALTER TABLE ");
        printlnIdentifier(getTableName(sourceTable));
        printIndent();
        print("MODIFY COLUMN ");
        writeColumn(targetTable, targetColumn);
        printEndOfStatement();
    }
}
