package org.apache.commons.sql.model;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

public class Column 
{
    private String name;
    private boolean primaryKey = false;
    private boolean required = false;
    private boolean autoIncrement = false;
    private String type;
    private int size = 0;
    
    public Column () {}
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public boolean isPrimaryKey()
    {
        return primaryKey;
    }
    
    public void setPrimaryKey(boolean primaryKey)
    {
        this.primaryKey = primaryKey;
    }	
    
    public boolean isRequired()
    {
        return required;
    }

    public void setRequired(boolean required)
    {
        this.required = required;
    }
    
    public boolean isAutoIncrement()
    {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement)
    {
        this.autoIncrement = autoIncrement;
    }
    
    public String getType()
    {
        return type;
    }
    
    public void setType(String type)
    {
        this.type = type;
    }
    
    public int getSize()
    {
        return size;
    }

    public void setSize(int size)
    {
        this.size=size;
    }
    
    
    // Helper methods
    //-------------------------------------------------------------------------                
    
    /**
     * @return the full SQL type string including the size, such as "VARCHAR (2000)"
     */
    public String getTypeString() {
        if ( getSize() > 0 ) {
            return getType() + " (" + getSize() + ")";
        }
        return getType();
    }

}
