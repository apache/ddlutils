package org.apache.ddlutils.task;

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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.alteration.RemoveTablesChange;
import org.apache.ddlutils.model.CloneHelper;
import org.apache.ddlutils.model.Database;
import org.apache.tools.ant.BuildException;

/**
 * Sub task for dropping tables.
 * 
 * @version $Revision: $
 * @ant.task name="dropTables"
 */
public class DropTablesCommand extends DatabaseCommand
{
    /** The names of the tables to be dropped. */
    private String[] _tableNames; 
    /** The regular expression matching the names of the tables to be dropped. */
    private String _tableNameRegExp;

    /**
     * Sets the names of the tables to be removed, as a comma-separated list. Escape a
     * comma via '\,' if it is part of the table name. Please note that table names are
     * not trimmed which means that whitespace characters should only be present in
     * this string if they are actually part of the table name (i.e. in delimited
     * identifer mode).
     * 
     * @param tableNameList The comma-separated list of table names
     * @ant.not-required If no table filter is specified, then all tables will be dropped.
     */
    public void setTableNames(String tableNameList)
    {
        String[]  tmpTableNames = tableNameList.split(",");
        ArrayList tableNames    = new ArrayList();
        String    last          = null;

        for (int idx = 0; idx < tmpTableNames.length; idx++)
        {
            String  str         = tmpTableNames[idx];
            int     strLen      = str.length();
            boolean endsInSlash = (strLen > 0) && (str.charAt(strLen - 1) == '\\') &&
                                  ((strLen == 1) || (str.charAt(strLen - 2) != '\\'));

            if (last != null)
            {
                last += "," + str;
                if (!endsInSlash)
                {
                    tableNames.add(last);
                    last = null;
                }
            }
            else if (endsInSlash)
            {
                last = str.substring(0, strLen - 1);
            }
            else
            {
                tableNames.add(str);
            }
        }
        if (last != null)
        {
            tableNames.add(last + ",");
        }
        _tableNames = (String[])tableNames.toArray(new String[tableNames.size()]);
    }

    /**
     * Sets the regular expression matching the names of the tables to be removed.
     * For case insensitive matching, an uppercase name can be assumed. If no
     * regular expressionis specified
     * 
     * @param tableNameRegExp The regular expression; see {@link java.util.regex.Pattern}
     *                        for details
     * @ant.not-required If no table filter is specified, then all tables will be dropped.
     */
    public void setTableNameRegExp(String tableNameRegExp)
    {
        _tableNameRegExp = tableNameRegExp;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isRequiringModel()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(DatabaseTaskBase task, Database model) throws BuildException
    {
        BasicDataSource dataSource = getDataSource();

        if (dataSource == null)
        {
            throw new BuildException("No database specified.");
        }

        Platform platform    = getPlatform();
        Database targetModel = new Database();

        if ((_tableNames != null) || (_tableNameRegExp != null))
        {
            RemoveTablesChange change = _tableNames != null ? new RemoveTablesChange(_tableNames)
                                                            : new RemoveTablesChange(_tableNameRegExp);

            targetModel = new CloneHelper().clone(model);
            targetModel.initialize();
            change.apply(targetModel, task.isUseDelimitedSqlIdentifiers());
        }
        try
        {
            platform.alterModel(model, targetModel, isFailOnError());

            _log.info("Dropped tables");
        }
        catch (Exception ex)
        {
            handleException(ex, ex.getMessage());
        }
    }
}
