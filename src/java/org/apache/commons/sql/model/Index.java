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
import java.util.List;

public class Index implements Cloneable
{
    protected String    name;
    protected ArrayList indexColumns = new ArrayList();
    protected boolean   unique       = false;

    public Index() {}

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        Index result = new Index();

        result.name         = name;
        result.indexColumns = (ArrayList)indexColumns.clone();
        result.unique       = unique;
        return result;
    }

    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public void addIndexColumn(IndexColumn indexColumn)
    {
        indexColumns.add(indexColumn);
    }
    
    public List getIndexColumns()
    {
        return indexColumns;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

}
