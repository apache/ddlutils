package org.apache.ddlutils.task;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.ddlutils.model.Table;

/**
 * A {@link org.apache.ddlutils.task.Parameter} intended for specific tables.
 * 
 * TODO: Some wildcard/regular expression mechanism would be useful
 * 
 * @author Thomas Dudziak
 * @version $Revision: 231306 $
 */
public class TableSpecificParameter extends Parameter
{
    /** The tables for which this parameter is applicable. */
    private ArrayList _tables = new ArrayList();

    /**
     * Sets the tables as a comma-separated list.
     * 
     * @param tableList The tables
     */
    public void setTables(String tableList)
    {
        StringTokenizer tokenizer = new StringTokenizer(tableList, ",");

        while (tokenizer.hasMoreTokens())
        {
            String tableName = tokenizer.nextToken().trim();

            // TODO: Quotation, escaped characters ?
            _tables.add(tableName);
        }
    }

    /**
     * Sets the single table.
     * 
     * @param tableName The table
     */
    public void setTable(String tableName)
    {
        _tables.add(tableName);
    }

    /**
     * Determines whether this parameter is applicable to the given table.
     * 
     * @param table         The table
     * @param caseSensitive Whether the case of the table name is relevant
     * @return <code>true</code> if this parameter is applicable to the table
     */
    public boolean isForTable(Table table, boolean caseSensitive)
    {
        if (_tables.isEmpty())
        {
            return true;
        }
        for (Iterator it = _tables.iterator(); it.hasNext();)
        {
            String tableName = (String)it.next();

            if ((caseSensitive  && tableName.equals(table.getName())) ||
                (!caseSensitive && tableName.equalsIgnoreCase(table.getName())))
            {
                return true;
            }
        }
        return false;
    }
}
