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

import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Table;

/**
 * Represents the addition of a foreign key to a table.
 * 
 * @version $Revision: $
 */
public class AddForeignKeyChange
{
    /** The table to add the foreign key to. */
    private Table _table;
    /** The new foreign key. */
    private ForeignKey _newForeignKey;

    /**
     * Creates a new change object.
     * 
     * @param table         The table to add the foreign key to
     * @param newForeignKey The new foreign key
     */
    public AddForeignKeyChange(Table table, ForeignKey newForeignKey)
    {
        _table         = table;
        _newForeignKey = newForeignKey;
    }

    /**
     * Returns the new foreign key.
     *
     * @return The new foreign key
     */
    public ForeignKey getNewForeignKey()
    {
        return _newForeignKey;
    }

    /**
     * Returns the table where the foreign key is to be added.
     *
     * @return The table
     */
    public Table getTable()
    {
        return _table;
    }
}
