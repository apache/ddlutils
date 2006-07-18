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

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.platform.CreationParameters;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Creates the SQL for a schema and writes it to a file.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class WriteSchemaSqlToFileCommand extends DatabaseCommandWithCreationParameters
{
    /** The file to output the DTD to. */
    private File _outputFile;
    /** Whether to alter or re-set the database if it already exists. */
    private boolean _alterDb = true;
    /** Whether to drop tables and the associated constraints if necessary. */
    private boolean _doDrops = true;

    /**
     * Sets the file to output the sql to.
     * 
     * @param outputFile The output file
     */
    public void setOutputFile(File outputFile)
    {
        _outputFile = outputFile;
    }

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
     * @return <code>true</code> if drops shall be performed if necessary
     */
    protected boolean isDoDrops()
    {
        return _doDrops;
    }

    /**
     * Specifies whether to drop tables and the associated constraints if necessary.
     * 
     * @param doDrops <code>true</code> if drops shall be performed if necessary
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
        if (_outputFile == null)
        {
            throw new BuildException("No output file specified");
        }
        if (_outputFile.exists() && !_outputFile.canWrite())
        {
            throw new BuildException("Cannot overwrite output file "+_outputFile.getAbsolutePath());
        }

        Platform           platform        = getPlatform();
        boolean            isCaseSensitive = platform.isDelimitedIdentifierModeOn();
        CreationParameters params          = getFilteredParameters(model, platform.getName(), isCaseSensitive);

        try
        {
            FileWriter writer = new FileWriter(_outputFile);

            if (platform.getPlatformInfo().isSqlCommentsSupported())
            {
                // we're generating SQL comments if possible
                platform.setSqlCommentsOn(true);
            }
            platform.getSqlBuilder().setWriter(writer);

            boolean shouldAlter = isAlterDatabase();

            if (shouldAlter)
            {
                if (getDataSource() == null)
                {
                    shouldAlter = false;
                    task.log("Cannot alter the database because no database connection was specified." +
                             " SQL for database creation will be generated instead.",
                             Project.MSG_WARN);
                }
                else
                {
                    try
                    {
                        Connection connection = getDataSource().getConnection();

                        connection.close();
                    }
                    catch (SQLException ex)
                    {
                        shouldAlter = false;
                        task.log("Could not establish a connection to the specified database, " +
                                 "so SQL for database creation will be generated instead. The error was: " +
                                 ex.getMessage(),
                                 Project.MSG_WARN);
                    }
                }
            }
            if (shouldAlter)
            {
                Database currentModel = (getCatalogPattern() != null) || (getSchemaPattern() != null) ?
                                             platform.readModelFromDatabase(null, getCatalogPattern(), getSchemaPattern(), null) :
                                             platform.readModelFromDatabase(null);

                platform.getSqlBuilder().alterDatabase(currentModel, model, params, _doDrops, true);
            }
            else
            {
                platform.getSqlBuilder().createTables(model, params, _doDrops);
            }
            writer.close();
            task.log("Written SQL to "+_outputFile.getAbsolutePath(), Project.MSG_INFO);
        }
        catch (Exception ex)
        {
            if (isFailOnError())
            {
                throw new BuildException(ex);
            }
            else
            {
                task.log(ex.getMessage() == null ? ex.toString() : ex.getMessage(), Project.MSG_ERR);
            }
        }
    }
}
