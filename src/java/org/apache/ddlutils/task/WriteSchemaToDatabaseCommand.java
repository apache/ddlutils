package org.apache.ddlutils.task;

/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
import org.apache.ddlutils.platform.CreationParameters;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Command for writing a database schema into the database.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class WriteSchemaToDatabaseCommand extends DatabaseCommandWithCreationParameters
{
    /** Whether to alter or re-set the database if it already exists. */
    private boolean _alterDb = true;
    /** Whether to drop tables and the associated constraints if necessary. */
    private boolean _doDrops = true;

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
     * Determines whether to drop tables and the associated constraints if necessary.
     * 
     * @return <code>true</code> if drops shall be performed
     */
    protected boolean isDoDrops()
    {
        return _doDrops;
    }

    /**
     * Specifies whether to drop tables and the associated constraints if necessary.
     * 
     * @param doDrops <code>true</code> if drops shall be performed
     */
    public void setDoDrops(boolean doDrops)
    {
        _doDrops = doDrops;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(Task task, Database model) throws BuildException
    {
        if (getDataSource() == null)
        {
            throw new BuildException("No database specified.");
        }

        Platform           platform        = getPlatform();
        boolean            isCaseSensitive = platform.getPlatformInfo().isUseDelimitedIdentifiers();
        CreationParameters params          = getFilteredParameters(model, platform.getName(), isCaseSensitive);

        try
        {
            if (isAlterDatabase())
            {
                platform.alterTables(model, params, _doDrops, true, true);
            }
            else
            {
                platform.createTables(model, params, _doDrops, true);
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
