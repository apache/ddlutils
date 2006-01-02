package org.apache.ddlutils.io;

/*
 * Copyright 1999-2006 The Apache Software Foundation.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Stores the identity of an database object as defined by its primary keys. Is used
 * by {@link org.apache.ddlutils.io.DataToDatabaseSink} class for inserting objects
 * in the correct order. 
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class Identity
{
    /** The name of the table. */
    private String _tableName;
    /** The optional foreign key name whose referenced object this identity represents. */
    private String _fkName;
    /** The identity columns and their values. */
    private HashMap _columnValues = new HashMap();

    /**
     * Creates a new identity object for the indicated table.
     * 
     * @param tableName The name of the table
     */
    public Identity(String tableName)
    {
        _tableName = tableName;
    }

    /**
     * Creates a new identity object for the indicated table.
     * 
     * @param tableName The name of the table
     * @param fkName    The name of the foreign key whose referenced object this identity represents
     */
    public Identity(String tableName, String fkName)
    {
        _tableName = tableName;
        _fkName    = fkName;
    }

    /**
     * Returns the name of the foreign key whose referenced object this identity represents. This
     * name is <code>null</code> if the identity is not for a foreign key, or if the foreign key
     * was unnamed.
     * 
     * @return The foreign key name
     */
    public String getForeignKeyName()
    {
        return _fkName;
    }
    
    /**
     * Specifies the value for one of the identity columns.
     * 
     * @param name  The column name
     * @param value The value for the column
     */
    public void setIdentityColumn(String name, Object value)
    {
        _columnValues.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Identity))
        {
            return false;
        }

        Identity otherIdentity = (Identity)obj;

        if (!_tableName.equals(otherIdentity._tableName))
        {
            return false;
        }
        if (_columnValues.keySet().size() != otherIdentity._columnValues.keySet().size())
        {
            return false;
        }
        for (Iterator it = _columnValues.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry entry      = (Map.Entry)it.next();
            Object    otherValue = otherIdentity._columnValues.get(entry.getKey());

            if (entry.getValue() == null)
            {
                if (otherValue != null)
                {
                    return false;
                }
            }
            else
            {
                if (!entry.getValue().equals(otherValue))
                {
                    return false;
                }
            }
        }
        
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return toString().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append(_tableName);
        buffer.append(":");
        for (Iterator it = _columnValues.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry entry = (Map.Entry)it.next();

            buffer.append(entry.getKey());
            buffer.append("=");
            buffer.append(entry.getValue());
            if (it.hasNext())
            {
                buffer.append(";");
            }
        }
        return buffer.toString();
    }
}
