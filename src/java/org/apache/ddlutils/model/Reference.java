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

/**
 * Represents a reference between a column in the local table and a column in another table.
 * 
 * @author Thomas Dudziak
 * @version $Revision$
 */
public class Reference implements Cloneable, Comparable
{
    /** The local column. */
    private Column _localColumn;
    /** The foreign column. */
    private Column _foreignColumn;
    /** The name of the local column. */
    private String _localColumnName;
    /** The name of the foreign column. */
    private String _foreignColumnName;

    /**
     * Creates a new, empty reference.
     */
    public Reference()
    {}

    /**
     * Creates a new reference between the two given columns.
     * 
     * @param localColumn   The local column
     * @param foreignColumn The remote column
     */
    public Reference(Column localColumn, Column foreignColumn)
    {
        setLocalColumn(localColumn);
        setForeignColumn(foreignColumn);
    }

    /**
     * Returns the local column.
     *
     * @return The local column
     */
    public Column getLocalColumn()
    {
        return _localColumn;
    }

    /**
     * Sets the local column.
     *
     * @param localColumn The local column
     */
    public void setLocalColumn(Column localColumn)
    {
        _localColumn     = localColumn;
        _localColumnName = (localColumn == null ? null : localColumn.getName());
    }

    /**
     * Returns the foreign column.
     *
     * @return The foreign column
     */
    public Column getForeignColumn()
    {
        return _foreignColumn;
    }

    /**
     * Sets the foreign column.
     *
     * @param foreignColumn The foreign column
     */
    public void setForeignColumn(Column foreignColumn)
    {
        _foreignColumn     = foreignColumn;
        _foreignColumnName = (foreignColumn == null ? null : foreignColumn.getName());
    }

    /**
     * Returns the name of the local column.
     * 
     * @return The column name
     */
    public String getLocalColumnName()
    {
        return _localColumnName;
    }

    /**
     * Sets the name of the local column. Note that you should not use this method when
     * manipulating the model manually. Rather use the {@link #setLocalColumn(Column)} method.
     * 
     * @param localColumnName The column name
     */
    public void setLocalColumnName(String localColumnName)
    {
        if ((_localColumn != null) && !_localColumn.getName().equals(localColumnName))
        {
            _localColumn = null;
        }
        _localColumnName = localColumnName;
    }
    
    /**
     * Returns the name of the foreign column.
     * 
     * @return The column name
     */
    public String getForeignColumnName()
    {
        return _foreignColumnName;
    }
    
    /**
     * Sets the name of the remote column. Note that you should not use this method when
     * manipulating the model manually. Rather use the {@link #setForeignColumn(Column)} method.
     * 
     * @param foreignColumnName The column name
     */
    public void setForeignColumnName(String foreignColumnName)
    {
        if ((_foreignColumn != null) && !_foreignColumn.getName().equals(foreignColumnName))
        {
            _foreignColumn = null;
        }
        _foreignColumnName = foreignColumnName;
    }

    /**
     * {@inheritDoc}
     */
    public Object clone() throws CloneNotSupportedException
    {
        return new Reference(getLocalColumn(), getForeignColumn());
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(Object other)
    {
        Reference ref = (Reference)other;

        int result = getLocalColumnName().compareTo(ref.getLocalColumnName());

        if (result == 0)
        {
            result = getForeignColumnName().compareTo(ref.getForeignColumnName());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object other)
    {
        boolean result = (other != null) && getClass().equals(other.getClass());

        if (result)
        {
            Reference ref = (Reference) other;

            // TODO: Compare the columns, not their names
            result = getLocalColumnName().equals(ref.getLocalColumnName()) &&
                     getForeignColumnName().equals(ref.getForeignColumnName());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return "Reference[" + getLocalColumnName() + " to " + getForeignColumnName() + "]";
    }
}
