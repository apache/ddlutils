/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
import java.util.Collections;
import java.util.List;

// TODO: Add a name property to the foreignkey that is respected by
//       create/alter/drop
public class ForeignKey implements Cloneable
{
    private String foreignTable;
        
    private ArrayList references = new ArrayList();
    
    public ForeignKey() {}
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        ForeignKey result = new ForeignKey();

        result.foreignTable = foreignTable;
        result.references   = (ArrayList)references.clone();
        return result;
    }

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

    public Reference firstReference() {
        return (Reference) (references.size() == 0 ? null : references.get(0));
    }

    public boolean equals(Object o) {
        boolean result = o != null && getClass().equals(o.getClass());
        if ( result ) {
            ForeignKey fk = (ForeignKey) o;
            result = this.foreignTable.equalsIgnoreCase(fk.foreignTable) && this.references.size() == fk.references.size();
            if ( result ) {
                //check all references - need to ensure order is same for valid comparison
                List copyThis = (List) this.references.clone();
                List copyThat = (List) fk.references.clone();
                Collections.sort(copyThis);
                Collections.sort(copyThat);
                result = copyThis.equals(copyThat);
            }
        }
        return result;
    }
    
    public String toString() {
        return "ForeignKey[" + this.foreignTable + "]";
        //TODO show references
    }
}
