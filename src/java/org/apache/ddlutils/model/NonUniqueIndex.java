package org.apache.ddlutils.model;

/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import java.util.ArrayList;

/**
 * Represents an index definition for a table.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class NonUniqueIndex implements Index
{
    /** Unique ID for serialization purposes. */
    private static final long serialVersionUID = -3591499395114850301L;

    /** The name of the index. */
    protected String    _name;
    /** The columns making up the index. */
    protected ArrayList _columns = new ArrayList();

    /**
     * {@inheritDoc}
     */
    public boolean isUnique()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return _name;
    }

    /**
     * {@inheritDoc}
     */
    public void setName(String name)
    {
        _name = name;
    }

    /**
     * {@inheritDoc}
     */
    public int getColumnCount()
    {
        return _columns.size();
    }

    /**
     * {@inheritDoc}
     */
    public IndexColumn getColumn(int idx)
    {
        return (IndexColumn)_columns.get(idx);
    }

    /**
     * {@inheritDoc}
     */
    public IndexColumn[] getColumns()
    {
        return (IndexColumn[])_columns.toArray(new IndexColumn[_columns.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public void addColumn(IndexColumn column)
    {
        if (column != null)
        {
            _columns.add(column);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addColumn(int idx, IndexColumn column)
    {
        if (column != null)
        {
            _columns.add(idx, column);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeColumn(IndexColumn column)
    {
        _columns.remove(column);
    }

    /**
     * {@inheritDoc}
     */
    public void removeColumn(int idx)
    {
        _columns.remove(idx);
    }

    /**
     * {@inheritDoc}
     */
    public Object clone() throws CloneNotSupportedException
    {
        NonUniqueIndex result = new NonUniqueIndex();

        result._name    = _name;
        result._columns = (ArrayList)_columns.clone();
        return result;
    }
}
