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
import org.apache.ddlutils.io.JdbcModelReader;
import org.apache.ddlutils.model.Database;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Ant task for working with a database, e.g. retrieving the schema from a database, dumping data,
 */
public class DatabaseToDdlTask extends Task
{
    /** The type of the database */
    private String _databaseType;
    /** The data source to use for accessing the database */
    private BasicDataSource _dataSource;
    /** The sub tasks to execute */
    private ArrayList _commands = new ArrayList();

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
     * Adds the data source to use for accessing the database.
     * 
     * @param dataSource The data source
     */
    public void addConfiguredDatabase(BasicDataSource dataSource)
    {
        _dataSource = dataSource;
    }

    /**
     * Adds the "create dtd"-command.
     * 
     * @param command The command
     */
    public void addWriteDtdToFile(WriteDtdToFileCommand command)
    {
        _commands.add(command);
    }

    /**
     * Adds the "write schema to file"-command.
     * 
     * @param command The command
     */
    public void addWriteSchemaToFile(WriteSchemaToFileCommand command)
    {
        _commands.add(command);
    }

    /**
     * Adds the "write schema sql to file"-command
     * 
     * @param command The command
     */
    public void addWriteSchemaSqlToFile(WriteSchemaSqlToFileCommand command)
    {
        _commands.add(command);
    }

    /**
     * Reads the schemas from the specified database.
     * 
     * @return The database model
     */
    private Database readSchema()
    {
        // TODO: This should largely be deducable from the jdbc connection url
        if (_databaseType == null)
        {
            throw new BuildException("The database type needs to be defined.");
        }
        if (_dataSource == null)
        {
            throw new BuildException("No database specified.");
        }

        try
        {
            JdbcModelReader reader = new JdbcModelReader(_dataSource.getConnection());

            return reader.getDatabase();
        }
        catch (Exception ex)
        {
            throw new BuildException("Could not read the schema from the specified database", ex);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException
    {
        if (_commands.isEmpty())
        {
            System.out.println("No sub tasks specified, so there is nothing to do.");
            return;
        }

        Database model = readSchema();

        if (model == null)
        {
            System.out.println("No schemas read, so there is nothing to do.");
            return;
        }

        for (Iterator it = _commands.iterator(); it.hasNext();)
        {
            Command cmd = (Command)it.next();

            if (cmd instanceof WantsDatabaseInfo)
            {
                ((WantsDatabaseInfo)cmd).setDatabaseInfo(_dataSource, _databaseType);
            }
            cmd.execute(this, model);
        }
    }

}
