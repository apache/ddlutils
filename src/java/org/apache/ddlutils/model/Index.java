package org.apache.ddlutils.model;

/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

import java.io.Serializable;

/**
 * Represents an index definition for a table which may be either unique or non-unique.
 */
public interface Index extends Cloneable, Serializable
{
    /**
     * Determines whether this index is unique or not.
     * 
     * @return <code>true</code> if the index is an unique one
     */
    public boolean isUnique();

    /**
     * Returns the name of the index.
     * 
     * @return The name
     */
    public String getName();
    
    /**
     * Sets the name of the index.
     * 
     * @param name The name
     */
    public void setName(String name);

    /**
     * Returns the number of columns that make up this index.
     * 
     * @return The number of index columns
     */
    public int getColumnCount();

    /**
     * Returns the indicated column making up this index.
     * 
     * @param idx The index of the column
     * @return The column
     */
    public IndexColumn getColumn(int idx);

    /**
     * Returns the columns that make up this index.
     * 
     * @return The columns
     */
    public IndexColumn[] getColumns();

    /**
     * Adds a column that makes up this index.
     * 
     * @param column The column to add
     */
    public void addColumn(IndexColumn column);

    /**
     * Adds a column that makes up this index at the specified position.
     * 
     * @param idx    The position to add the index colum at
     * @param column The column to add
     */
    public void addColumn(int idx, IndexColumn column);

    /**
     * Removes the given index column from this index.
     * 
     * @param column The column to remove
     */
    public void removeColumn(IndexColumn column);

    /**
     * Removes the column at the specified position in this index.
     * 
     * @param idx The position of the index column to remove
     */
    public void removeColumn(int idx);
}
