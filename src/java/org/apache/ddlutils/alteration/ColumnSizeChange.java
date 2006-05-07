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
 * Represents the change of the size or scale of a column.
 * 
 * @version $Revision: $
 */
public class ColumnSizeChange extends TableChangeImplBase
{
    /** The column. */
    private Column _column;
    /** The new size. */
    private int _newSize;
    /** The new scale. */
    private int _newScale;

    /**
     * Creates a new change object.
     * 
     * @param table    The table of the column
     * @param column   The column
     * @param newSize  The new size
     * @param newScale The new scale
     */
    public ColumnSizeChange(Table table, Column column, int newSize, int newScale)
    {
        super(table);
        _column   = column;
        _newSize  = newSize;
        _newScale = newScale;
    }

    /**
     * Returns the column.
     *
     * @return The column
     */
    public Column getColumn()
    {
        return _column;
    }

    /**
     * Returns the new size of the column.
     *
     * @return The new size
     */
    public int getNewSize()
    {
        return _newSize;
    }

    /**
     * Returns the new scale of the column.
     *
     * @return The new scale
     */
    public int getNewScale()
    {
        return _newScale;
    }

    /**
     * {@inheritDoc}
     */
    public void apply(Database database)
    {
        Table  table  = database.findTable(getChangedTable().getName());
        Column column = table.findColumn(_column.getName());

        column.setSizeAndScale(_newSize, _newScale);
    }
}
