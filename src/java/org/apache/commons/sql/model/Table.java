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
package org.apache.commons.sql.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Models a table.
 *
 * @version $Id$
 * @author John Marshall/Connectria
 * @author Matthew Hawthorne
 */
import org.apache.commons.collections.iterators.FilterIterator;
import org.apache.commons.collections.Predicate;

public class Table implements Cloneable
{
    private static final Predicate UNIQUE_PREDICATE = new Predicate() {
        public boolean evaluate(Object input) {
            return ((Index)input).isUnique();
        }
    };

    private String catalog = null;

    private String name = null;

    private String javaName = null;

    private String schema = null;

    private String remarks = null;

    private String type = null;

    private ArrayList columns = new ArrayList();

    private ArrayList foreignKeys = new ArrayList();

    private ArrayList indexes = new ArrayList();

    public Table()
    {
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        Table result = new Table();

        result.catalog     = catalog;
        result.name        = name;
        result.javaName    = javaName;
        result.schema      = schema;
        result.remarks     = remarks;
        result.type        = type;
        result.columns     = (ArrayList)columns.clone();
        result.foreignKeys = (ArrayList)foreignKeys.clone();
        result.indexes     = (ArrayList)indexes.clone();
        return result;
    }

    public String getCatalog()
    {
        return this.catalog;
    }

    public void setCatalog(String catalog)
    {
        this.catalog = catalog;
    }

    public String getRemarks()
    {
        return this.remarks;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    public String getSchema()
    {
        return this.schema;
    }

    public void setSchema(String schema)
    {
        this.schema = schema;
    }

    public String getType()
    {
        return (type == null) ? "(null)" : type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name=name;
    }

    public String getJavaName()
    {
        return javaName;
    }

    public void setJavaName(String javaName)
    {
        this.javaName = javaName;
    }

    public void addColumn(Column column)
    {
        columns.add(column);
    }

    public void addAll(List columns)
    {
        if(columns != null &&
           columns.size() > 0)
        {
            int columnsSize = columns.size();
            for(int i = 0; i < columnsSize; i++)
            {
                Column column = (Column) columns.get(i);
                if(column != null)
                {
                    this.addColumn(column);
                }
            }
        }
    }

    public List getColumns()
    {
        return columns;
    }

    public void addForeignKey(ForeignKey foreignKey)
    {
        foreignKeys.add(foreignKey);
    }

    public List getForeignKeys()
    {
        return foreignKeys;
    }

    public Column getColumn(int index)
    {
        return (Column) columns.get(index);
    }

    public ForeignKey getForeignKey(int index)
    {
        return (ForeignKey) foreignKeys.get(index);
    }

    public void addIndex(Index index)
    {
        indexes.add(index);
    }

    public List getIndexes()
    {
        return indexes;
    }

    public Index getIndex(int index)
    {
        return (Index) indexes.get(index);
    }

//take this out of Unique is annoying
//this is in here to support <unique> in the xml
    /**
     * Add a unique index to this table
     * @param index The unique index
     */
    public void addUnique(Unique index)
    {
        addIndex(index);
    }

    /**
     * Gets a list of unique indexes on this table.
     * @return an Iterator of Index objects where isUnique == true
     */
    public Iterator getUniques()
    {
        return new FilterIterator( indexes.iterator(), UNIQUE_PREDICATE );
    }
//end unique


    // Helper methods
    //-------------------------------------------------------------------------

    /**
     * @return true if there is at least one primary key column
     *  on this table
     */
    public boolean hasPrimaryKey()
    {
        for (Iterator iter = getColumns().iterator(); iter.hasNext(); )
        {
            Column column = (Column) iter.next();
            if ( column.isPrimaryKey() )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the table with the specified name, using case insensitive matching.
     * Note that this method is not called getColumn(String) to avoid introspection
     * problems.
     */
    public Column findColumn(String name)
    {
        for (Iterator iter = getColumns().iterator(); iter.hasNext(); )
        {
            Column column = (Column) iter.next();

            // column names are typically case insensitive
            if (column.getName().equalsIgnoreCase( name ))
            {
                return column;
            }
        }
        return null;
    }

    /**
     * Finds the index with the specified name, using case insensitive matching.
     * Note that this method is not called getIndex(String) to avoid introspection
     * problems.
     */
    public Index findIndex(String name)
    {
        for (Iterator iter = getIndexes().iterator(); iter.hasNext(); )
        {
            Index index = (Index) iter.next();

            // column names are typically case insensitive
            if (index.getName().equalsIgnoreCase( name ))
            {
                return index;
            }
        }
        return null;
    }

    /**
     * @return a List of primary key columns or an empty list if there are no
     * primary key columns for this Table
     */
    public List getPrimaryKeyColumns()
    {
        List answer = new ArrayList();
        for (Iterator iter = getColumns().iterator(); iter.hasNext(); )
        {
            Column column = (Column) iter.next();
            if ( column.isPrimaryKey() )
            {
                answer.add(column);
            }
        }
        return answer;
    }

    /**
     * @return the auto increment column, if there is one, otherwise null is returned
     */
    public Column getAutoIncrementColumn()
    {
        for (Iterator iter = getColumns().iterator(); iter.hasNext(); )
        {
            Column column = (Column) iter.next();
            if ( column.isAutoIncrement() )
            {
                return column;
            }
        }
        return null;
    }
}
