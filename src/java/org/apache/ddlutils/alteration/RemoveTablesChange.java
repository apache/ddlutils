package org.apache.ddlutils.alteration;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Table;

/**
 * Represents the removal of tables and foreign keys to them, from a model.
 * 
 * @version $Revision: $
 */
public class RemoveTablesChange implements ModelChange
{
    /** The names of the tables to be removed. */
    private String[] _tableNames;
    /** The regular expression matching the names of the tables to be removed. */
    private Pattern _tableNameRegExp;

    /**
     * Creates a new change object.
     * 
     * @param tableNames The names of the tables to be removed
     */
    public RemoveTablesChange(String[] tableNames)
    {
        _tableNames = new String[tableNames.length];
        System.arraycopy(tableNames, 0, _tableNames, 0, tableNames.length);
    }

    /**
     * Creates a new change object.
     * 
     * @param tableNameRegExp The regular expression matching the names of the tables
     *                        to be removed (see {@link java.util.regex.Pattern}
     *                        for details); for case insensitive matching, an uppercase
     *                        name can be assumed
     */
    public RemoveTablesChange(String tableNameRegExp)
    {
        _tableNameRegExp = Pattern.compile(tableNameRegExp);
    }

    /**
     * {@inheritDoc}
     */
    public void apply(Database model, boolean caseSensitive)
    {
        ArrayList tables = new ArrayList();

        if (_tableNames != null)
        {
            for (int idx = 0; idx < _tableNames.length; idx++)
            {
                Table table = model.findTable(_tableNames[idx], caseSensitive);

                if (table != null)
                {
                    tables.add(table);
                }
            }
        }
        else if (_tableNameRegExp != null)
        {
            for (int idx = 0; idx < model.getTableCount(); idx++)
            {
                Table  table     = model.getTable(idx);
                String tableName = table.getName();

                if (!caseSensitive)
                {
                    tableName = tableName.toUpperCase();
                }
                if (_tableNameRegExp.matcher(tableName).matches())
                {
                    tables.add(table);
                }
            }
        }
        for (Iterator tableIt = tables.iterator(); tableIt.hasNext();)
        {
            Table targetTable = (Table)tableIt.next();
            
            for (int tableIdx = 0; tableIdx < model.getTableCount(); tableIdx++)
            {
                Table     curTable    = model.getTable(tableIdx);
                ArrayList fksToRemove = new ArrayList();

                for (int fkIdx = 0; fkIdx < curTable.getForeignKeyCount(); fkIdx++)
                {
                    ForeignKey curFk = curTable.getForeignKey(fkIdx);

                    if (curFk.getForeignTable().equals(targetTable))
                    {
                        fksToRemove.add(curFk);
                    }
                }
                for (Iterator fkIt = fksToRemove.iterator(); fkIt.hasNext();)
                {
                    curTable.removeForeignKey((ForeignKey)fkIt.next());
                }
            }
        }
        for (Iterator tableIt = tables.iterator(); tableIt.hasNext();)
        {
            model.removeTable((Table)tableIt.next());
        }
    }
}
