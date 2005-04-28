package org.apache.ddlutils.model;

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


public class Reference implements Cloneable, Comparable
{
    private String local;
    private String foreign;
    
    public Reference() {}

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        Reference result = new Reference();

        result.local   = local;
        result.foreign = foreign;
        return result;
    }

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
    
    public boolean equals(Object o) {
        boolean result = o != null && getClass().equals(o.getClass());
        if ( result ) {
            Reference ref = (Reference) o;
            result = this.local.equalsIgnoreCase(ref.local) && this.foreign.equalsIgnoreCase(ref.foreign);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        Reference ref = (Reference) o;
        int result = this.local.compareTo(ref.local);
        if ( result == 0 ) {
            result = this.foreign.compareTo(ref.foreign);
        }
        return result;
    }
    
    public String toString() {
        return "Reference[" + this.local + " to " + this.foreign + "]";
    }
}
