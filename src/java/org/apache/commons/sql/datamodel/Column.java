package org.apache.commons.sql.datamodel;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

public class Column 
{
    private String name;
    private boolean primaryKey = false;
    private boolean required = false;
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
}
