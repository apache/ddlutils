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

import org.apache.ddlutils.DdlUtilsException;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

/**
 * Represents the addition of a column to a table.
 * 
 * @version $Revision: $
 */
public class AddColumnChange extends TableChangeImplBase
{
    /** The new column. */
    private Column _newColumn;

    /**
     * Creates a new change object.
     * 
     * @param table     The table to add the column to
     * @param newColumn The new column
     */
    public AddColumnChange(Table table, Column newColumn)
    {
        super(table);
        _newColumn = newColumn;
    }

    /**
     * Returns the new column.
     *
     * @return The new column
     */
    public Column getNewColumn()
    {
        return _newColumn;
    }

    /**
     * {@inheritDoc}
     */
    public void apply(Database database)
    {
        Column newColumn = null;

        try
        {
            newColumn = (Column)_newColumn.clone();
        }
        catch (CloneNotSupportedException ex)
        {
            throw new DdlUtilsException(ex);
        }
        database.findTable(getChangedTable().getName()).addColumn(newColumn);
    }
}
