/*
 * $Header: /home/cvs/jakarta-commons-sandbox/jelly/src/java/org/apache/commons/jelly/CompilableTag.java,v 1.5 2002/05/17 15:18:12 jstrachan Exp $
 * $Revision: 1.5 $
 * $Date: 2002/05/17 15:18:12 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 * 
 * $Id: CompilableTag.java,v 1.5 2002/05/17 15:18:12 jstrachan Exp $
 */
package org.apache.commons.sql.model;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

public class Table 
{
    private String catalog = null;
    
    private String name = null;

    private String schema = null;

    private String remarks = null;
    
    private String type = null;
    
    private List columns = new ArrayList();
    
    private List foreignKeys = new ArrayList();

    private List indexes = new ArrayList();
    
    public Table() 
    {
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
    
    public void addColumn(Column column)
    {
        columns.add(column);
    }

    public void addColumns(List columns)
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
