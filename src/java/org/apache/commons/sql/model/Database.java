package org.apache.commons.sql.model;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

public class Database 
{
    private String name;
    
    private String idMethod;
    
    private List tables = new ArrayList();
    
    public Database()
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
    
    public void setIdMethod(String idMethod)
    {
        this.idMethod=idMethod;
    }
        
        
    public void addTable(Table table)
    {
        tables.add(table);
    }	

    public List getTables()
    {
        return tables;
    }

    // Additions for PropertyUtils
    
    public void setTable(int index, Table table)
    {
        addTable(table);
    }        

    public Table getTable(int index)
    {
        return (Table) tables.get(index);
    }
    
    
    public String toString() 
    {
        return super.toString() + "[name=" + name + ";tableCount=" + tables.size() + "]";
    }
}
