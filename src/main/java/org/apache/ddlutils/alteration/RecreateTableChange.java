package org.apache.ddlutils.alteration;

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

import java.util.List;

import org.apache.ddlutils.model.CloneHelper;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

/**
 * Represents the recreation of a table, i.e. creating a temporary table using the target table definition,
 * copying the data from the original table into this temporary table, dropping the original table and
 * finally renaming the temporary table to the final table. This change is only created by the model
 * comparator if the table definition change predicate flags a table change as unsupported.
 *  
 * @version $Revision: $
 */
public class RecreateTableChange extends TableChangeImplBase
{
    /** The target table definition. */
    private Table _targetTable;
    /** The original table changes, one of which is unsupported by the current platform. */
    private List  _originalChanges;

    /**
     * Creates a new change object for recreating a table. This change is used to specify that a table needs
     * to be dropped and then re-created (with changes). In the standard model comparison algorithm, it will
     * replace all direct changes to the table's columns (i.e. foreign key and index changes are unaffected).  
     * 
     * @param tableName       The name of the table
     * @param targetTable     The table as it should be; note that the change object will keep a reference
     *                        to this table which means that it should not be changed after creating this
     *                        change object
     * @param originalChanges The original changes that this change object replaces
     */
    public RecreateTableChange(String tableName, Table targetTable, List originalChanges)
    {
        super(tableName);
        _targetTable     = targetTable;
        _originalChanges = originalChanges;
    }

    /**
     * Returns the original table changes, one of which is unsupported by the current platform.
     * 
     * @return The table changes
     */
    public List getOriginalChanges()
    {
        return _originalChanges;
    }

    /**
     * Returns the target table definition. Due to when an object of this kind is created in the comparison
     * process, this table object will not have any foreign keys pointing to or from it, i.e. it is
     * independent of any model.
     * 
     * @return The table definition
     */
    public Table getTargetTable()
    {
        return _targetTable;
    }

    /**
     * {@inheritDoc}
     */
    public void apply(Database database, boolean caseSensitive)
    {
        // we only need to replace the table in the model, as there can't be a
        // foreign key from or to it when these kind of changes are created
        for (int tableIdx = 0; tableIdx < database.getTableCount(); tableIdx++)
        {
            Table curTable = database.getTable(tableIdx);

            if ((caseSensitive  && curTable.getName().equals(getChangedTable())) ||
                (!caseSensitive && curTable.getName().equalsIgnoreCase(getChangedTable())))
            {
                database.removeTable(tableIdx);
                database.addTable(tableIdx, new CloneHelper().clone(_targetTable, true, false, database, caseSensitive));
                break;
            }
        }
    }
}
