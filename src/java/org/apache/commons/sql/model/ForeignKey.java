package org.apache.commons.sql.model;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

public class ForeignKey 
{
    private String foreignTable;
        
    private List references = new ArrayList();
    
    public ForeignKey() {}
    
    public String getForeignTable()
    {
        return foreignTable;
    }
    
    public void setForeignTable(String foreignTable)
    {
        this.foreignTable= foreignTable;
    }
    
    public void addReference(Reference reference)
    {
        references.add(reference);
    }
    
    public List getReferences()
    {
        return references;
    }
}
