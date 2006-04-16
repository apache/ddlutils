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

import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;

/**
 * Represents the addition of an index to a table.
 * 
 * @version $Revision: $
 */
public class AddIndexChange
{
    /** The table to add the index to. */
    private Table _table;
    /** The new index. */
    private Index _newIndex;

    /**
     * Creates a new change object.
     * 
     * @param table    The table to add the index to
     * @param newIndex The new index
     */
    public AddIndexChange(Table table, Index newIndex)
    {
        _table    = table;
        _newIndex = newIndex;
    }

    /**
     * Returns the new index.
     *
     * @return The new index
     */
    public Index getNewIndex()
    {
        return _newIndex;
    }

    /**
     * Returns the table where the index is to be added.
     *
     * @return The table
     */
    public Table getTable()
    {
        return _table;
    }
}
