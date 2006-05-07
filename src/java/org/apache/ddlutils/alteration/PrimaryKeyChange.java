package org.apache.ddlutils.alteration;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

/**
 * Represents the change of the primary key of a table.
 * 
 * @version $Revision: $
 */
public class PrimaryKeyChange extends TableChangeImplBase
{
    /** The columns making up the original primary key. */
    private Column[] _oldPrimaryKeyColumns;
    /** The columns making up the new primary key. */
    private Column[] _newPrimaryKeyColumns;

    /**
     * Creates a new change object.
     * 
     * @param table                The table whose primary key is to be changed
     * @param oldPrimaryKeyColumns The columns making up the original primary key
     * @param newPrimaryKeyColumns The columns making up the new primary key
     */
    public PrimaryKeyChange(Table table, Column[] oldPrimaryKeyColumns, Column[] newPrimaryKeyColumns)
    {
        super(table);
        _oldPrimaryKeyColumns = oldPrimaryKeyColumns;
        _newPrimaryKeyColumns = newPrimaryKeyColumns;
    }

    /**
     * Returns the columns making up the original primary key.
     *
     * @return The columns
     */
    public Column[] getOldPrimaryKeyColumns()
    {
        return _oldPrimaryKeyColumns;
    }

    /**
     * Returns the columns making up the new primary key.
     *
     * @return The columns
     */
    public Column[] getNewPrimaryKeyColumns()
    {
        return _newPrimaryKeyColumns;
    }

    /**
     * {@inheritDoc}
     */
    public void apply(Database database)
    {
        Table table = database.findTable(getChangedTable().getName());

        for (int idx = 0; idx < _oldPrimaryKeyColumns.length; idx++)
        {
            Column column = table.findColumn(_oldPrimaryKeyColumns[idx].getName());

            column.setPrimaryKey(false);
        }
        for (int idx = 0; idx < _newPrimaryKeyColumns.length; idx++)
        {
            Column column = table.findColumn(_newPrimaryKeyColumns[idx].getName());

            column.setPrimaryKey(true);
        }
    }
}
