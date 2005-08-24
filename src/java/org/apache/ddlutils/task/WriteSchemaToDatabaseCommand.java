package org.apache.ddlutils.task;

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

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Database;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Command for writing a database schema into the database.
 */
public class WriteSchemaToDatabaseCommand extends DatabaseCommand
{
    /** Whether to alter or re-set the database if it already exists */
    private boolean _alterDb = true;
    /** Whether to drop tables and the associated constraints first */
    private boolean _dropTablesFirst = true;

    /**
     * Determines whether to alter the database if it already exists, or re-set it.
     * 
     * @return <code>true</code> if to alter the database
     */
    protected boolean isAlterDatabase()
    {
        return _alterDb;
    }

    /**
     * Specifies whether to alter the database if it already exists, or re-set it.
     * 
     * @param alterTheDb <code>true</code> if to alter the database
     */
    public void setAlterDatabase(boolean alterTheDb)
    {
        _alterDb = alterTheDb;
    }

    /**
     * Determines whether to drop tables and the associated constraints first.
     * 
     * @return <code>true</code> if a drop shall be performed first
     */
    protected boolean isDropTablesFirst()
    {
        return _dropTablesFirst;
    }

    /**
     * Specifies whether to drop tables and the associated constraints first.
     * 
     * @param doDrops <code>true</code> if a drop shall be performed first
     */
    public void setDropTablesFirst(boolean doDrops)
    {
        _dropTablesFirst = doDrops;
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.task.Command#execute(org.apache.tools.ant.Task, org.apache.ddlutils.model.Database)
     */
    public void execute(Task task, Database model) throws BuildException
    {
        if (getDataSource() == null)
        {
            throw new BuildException("No database specified.");
        }

        Platform platform = getPlatform();

        try
        {
            if (isAlterDatabase())
            {
                platform.alterTables(model, _dropTablesFirst, true, true);
            }
            else
            {
                platform.createTables(model, _dropTablesFirst, true);
            }

            task.log("Written schema to database", Project.MSG_INFO);
        }
        catch (Exception ex)
        {
            if (isFailOnError())
            {
                throw new BuildException(ex);
            }
            else
            {
                task.log(ex.getLocalizedMessage(), Project.MSG_ERR);
            }
        }
    }
}
