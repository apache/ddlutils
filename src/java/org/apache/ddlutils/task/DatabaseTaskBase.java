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
import java.util.Iterator;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Database;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Base class for DdlUtils Ant tasks that operate on a database.
 * 
 * @version $Revision: 289996 $
 * @ant.task ignore="true"
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
     * Specifies the database type. You should only need to specify this if DdlUtils is not able to
     * derive the setting from the name of the used jdbc driver or the jdbc connection url.
     * If you need to specify this, please post your jdbc driver and connection url combo
     * to the user mailing list so that DdlUtils can be enhanced to support this combo.<br/>
     * Valid values are currently: <code>axion, cloudscape, db2, derby, firebird, hsqldb, interbase,
     * maxdb, mckoi, mssql, mysql, mysql5, oracle, oracle9, oracle10, postgresql, sapdb, sybase</code>
     * 
     * @param type The database type
     * @ant.not-required Per default, DdlUtils tries to determine the database type via JDBC.
     */
    public void setDatabaseType(String type)
    {
        if ((type != null) && (type.length() > 0))
        {
            _platformConf.setDatabaseType(type);
        }
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
     * Sets the catalog pattern used when accessing the database.
     * 
     * @param catalogPattern The catalog pattern
     * @ant.not-required Per default, no specific catalog is used.
     */
    public void setCatalogPattern(String catalogPattern)
    {
        if ((catalogPattern != null) && (catalogPattern.length() > 0))
        {
            _platformConf.setCatalogPattern(catalogPattern);
        }
    }
    
    /**
     * Sets the schema pattern used when accessing the database.
     * 
     * @param schemaPattern The schema pattern
     * @ant.not-required Per default, no specific schema is used.
     */
    public void setSchemaPattern(String schemaPattern)
    {
        if ((schemaPattern != null) && (schemaPattern.length() > 0))
        {
            _platformConf.setSchemaPattern(schemaPattern);
        }
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
     * Specifies whether DdlUtils shall use delimited (quoted) identifiers (table names, column names etc.)
     * In most databases, undelimited identifiers will be converted to uppercase by the database,
     * and the case of the identifier is ignored when performing any SQL command. Undelimited
     * identifiers can contain only alphanumerical characters and the underscore. Also, no reserved
     * words can be used as such identifiers.<br/>
     * The limitations do not exist for delimited identifiers. However case of the identifier will be
     * important in every SQL command executed against the database.
     *
     * @param useDelimitedSqlIdentifiers <code>true</code> if delimited SQL identifiers shall be used
     * @ant.not-required Default is <code>false</code>.
     */
    public void setUseDelimitedSqlIdentifiers(boolean useDelimitedSqlIdentifiers)
    {
        _platformConf.setUseDelimitedSqlIdentifiers(useDelimitedSqlIdentifiers);
    }

    /**
     * Determines whether a table's foreign keys read from a live database
     * shall be sorted alphabetically. Is <code>false</code> by default.
     *
     * @return <code>true</code> if the foreign keys shall be sorted
     */
    public boolean isSortForeignKeys()
    {
        return _platformConf.isSortForeignKeys();
    }

    /**
     * Specifies whether a table's foreign keys read from a live database shall be sorted
     * alphabetically or left in the order that they are returned by the database. Note that
     * the sort is case sensitive only if delimied identifier mode is on
     * (<code>useDelimitedSqlIdentifiers</code> is set to <code>true</code>).
     *
     * @param sortForeignKeys <code>true</code> if the foreign keys shall be sorted
     * @ant.not-required Default is <code>false</code>.
     */
    public void setSortForeignKeys(boolean sortForeignKeys)
    {
        _platformConf.setSortForeignKeys(sortForeignKeys);
    }

    /**
     * Determines whether the database shall be shut down after the task has finished.
     *
     * @return <code>true</code> if the database shall be shut down
     */
    public boolean isShutdownDatabase()
    {
        return _platformConf.isShutdownDatabase();
    }

    /**
     * Specifies whether the database shall be shut down after the task has finished.
     * This is mostly usefule for embedded databases.
     *
     * @param shutdownDatabase <code>true</code> if the database shall be shut down
     * @ant.not-required Default is <code>false</code>.
     */
    public void setShutdownDatabase(boolean shutdownDatabase)
    {
        _platformConf.setShutdownDatabase(shutdownDatabase);
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
     * Returns the commands.
     * 
     * @return The commands
     */
    protected Iterator getCommands()
    {
        return _commands.iterator();
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
     * Reads the database model on which the commands will work.
     * 
     * @return The database model
     */
    protected abstract Database readModel();

    /**
     * Executes the commands.
     * 
     * @param model The database model
     */
    protected void executeCommands(Database model) throws BuildException
    {
        for (Iterator it = getCommands(); it.hasNext();)
        {
            Command cmd = (Command)it.next();

            if (cmd.isRequiringModel() && (model == null))
            {
                throw new BuildException("No database model specified");
            }
            if (cmd instanceof DatabaseCommand)
            {
                ((DatabaseCommand)cmd).setPlatformConfiguration(_platformConf);
            }
            cmd.execute(this, model);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws BuildException
    {
        if (!hasCommands())
        {
            log("No sub tasks specified, so there is nothing to do.", Project.MSG_INFO);
            return;
        }

        ClassLoader    sysClassLoader = Thread.currentThread().getContextClassLoader();
        AntClassLoader newClassLoader = new AntClassLoader(getClass().getClassLoader(), true);

        // we're changing the thread classloader so that we can access resources
        // from the classpath used to load this task's class
        Thread.currentThread().setContextClassLoader(newClassLoader);
        
        try
        {
            executeCommands(readModel());
        }
        finally
        {
            if ((getDataSource() != null) && isShutdownDatabase())
            {
                getPlatform().shutdownDatabase();
            }
            // rollback of our classloader change
            Thread.currentThread().setContextClassLoader(sysClassLoader);
        }
    }
}
