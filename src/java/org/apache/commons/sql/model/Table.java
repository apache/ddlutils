package org.apache.commons.sql.model;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

public class Table 
{
    private String name;
    
    private List columns = new ArrayList();
    
    private List foreignKeys = new ArrayList();
    
    public Table() 
    {
    }
    
    public String getName() 
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name=name;
    }
    
    public void addColumn(Column column)
    {
        columns.add(column);
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

}
