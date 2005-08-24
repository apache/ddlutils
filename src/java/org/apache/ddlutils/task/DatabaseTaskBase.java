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

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.model.Database;
import org.apache.tools.ant.Task;

public abstract class DatabaseTaskBase extends Task
{
    /** The type of the database */
    private String _databaseType;
    /** The data source to use for accessing the database */
    private BasicDataSource _dataSource;
    /** The sub tasks to execute */
    private ArrayList _commands = new ArrayList();

    /**
     * Returns the database type.
     *
     * @return The database type
     */
    public String getDatabaseType()
    {
        return _databaseType;
    }

    /**
     * Sets the database type.
     * 
     * @param type The database type
     */
    public void setDatabaseType(String type)
    {
        _databaseType = type;
    }

    /**
     * Returns the data source.
     *
     * @return The data source
     */
    public BasicDataSource getDataSource()
    {
        return _dataSource;
    }

    /**
     * Adds the data source to use for accessing the database.
     * 
     * @param dataSource The data source
     */
    public void addConfiguredDatabase(BasicDataSource dataSource)
    {
        _dataSource = dataSource;
    }

    /**
     * Adds a command.
     * 
     * @param command The command
     */
    protected void addCommand(Command command)
    {
        _commands.add(command);
    }

    /**
     * Determines whether there are commands to perform.
     * 
     * @return <code>true</code> if there are commands
     */
    protected boolean hasCommands()
    {
        return !_commands.isEmpty();
    }

    /**
     * Executes the commands.
     * 
     * @param model The database model
     */
    protected void executeCommands(Database model)
    {
        for (Iterator it = _commands.iterator(); it.hasNext();)
        {
            Command cmd = (Command)it.next();

            if (cmd instanceof DatabaseCommand)
            {
                ((DatabaseCommand)cmd).setDatabaseInfo(getDataSource(), getDatabaseType());
            }
            cmd.execute(this, model);
        }
    }
}
