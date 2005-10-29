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

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Database;
import org.apache.tools.ant.Task;

/**
 * Base class for DdlUtils Ant tasks that operate on a database.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public abstract class DatabaseTaskBase extends Task
{
    /** The platform configuration. */
    private PlatformConfiguration _platformConf = new PlatformConfiguration();
    /** The sub tasks to execute. */
    private ArrayList _commands = new ArrayList();

    /**
     * Returns the database type.
     *
     * @return The database type
     */
    public String getDatabaseType()
    {
        return _platformConf.getDatabaseType();
    }

    /**
     * Sets the database type.
     * 
     * @param type The database type
     */
    public void setDatabaseType(String type)
    {
        _platformConf.setDatabaseType(type);
    }

    /**
     * Returns the data source.
     *
     * @return The data source
     */
    public BasicDataSource getDataSource()
    {
        return _platformConf.getDataSource();
    }

    /**
     * Adds the data source to use for accessing the database.
     * 
     * @param dataSource The data source
     */
    public void addConfiguredDatabase(BasicDataSource dataSource)
    {
        _platformConf.setDataSource(dataSource);
    }

    /**
     * Determines whether delimited SQL identifiers shall be used (the default).
     *
     * @return <code>true</code> if delimited SQL identifiers shall be used
     */
    public boolean isUseDelimitedSqlIdentifiers()
    {
        return _platformConf.isUseDelimitedSqlIdentifiers();
    }

    /**
     * Specifies whether delimited SQL identifiers shall be used.
     *
     * @param useDelimitedSqlIdentifiers <code>true</code> if delimited SQL identifiers shall be used
     */
    public void setUseDelimitedSqlIdentifiers(boolean useDelimitedSqlIdentifiers)
    {
        _platformConf.setUseDelimitedSqlIdentifiers(useDelimitedSqlIdentifiers);
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
     * Creates the platform for the configured database.
     * 
     * @return The platform
     */
    protected Platform getPlatform()
    {
        return _platformConf.getPlatform();
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
                ((DatabaseCommand)cmd).setPlatformConfiguration(_platformConf);
            }
            cmd.execute(this, model);
        }
    }
}
