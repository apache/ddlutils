package org.apache.commons.sql.model;

public class Reference 
{
    private String local;
    private String foreign;
    
    public Reference() {}
    
    public String getLocal()
    {
        return local;
    }
    
    public void setLocal(String local)
    {
        this.local = local;
    }
    
    public String getForeign()
    {
        return foreign;
    }
    
    public void setForeign(String foreign)
    {
        this.foreign = foreign;
    }
}
