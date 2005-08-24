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

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.io.JdbcModelReader;
import org.apache.ddlutils.model.Database;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Creates the SQL for a schema and writes it to a file.
 */
public class WriteSchemaSqlToFileCommand extends DatabaseCommand
{
    /** The file to output the DTD to */
    private File _outputFile;
    /** Whether to alter or re-set the database if it already exists */
    private boolean _alterDb = true;
    /** Whether to drop tables and the associated constraints first */
    private boolean _dropTablesFirst = true;

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
        if (_outputFile == null)
        {
            throw new BuildException("No output file specified");
        }
        if (_outputFile.exists() && !_outputFile.canWrite())
        {
            throw new BuildException("Cannot overwrite output file "+_outputFile.getAbsolutePath());
        }

        Platform   platform   = getPlatform();
        Connection connection = null;

        try
        {
            FileWriter writer = new FileWriter(_outputFile);

            platform.getSqlBuilder().setWriter(writer);
            if (isAlterDatabase())
            {
                if (getDataSource() == null)
                {
                    throw new BuildException("No database specified.");
                }
                connection = getDataSource().getConnection();

                Database currentModel = new JdbcModelReader(connection).getDatabase();

                platform.getSqlBuilder().alterDatabase(currentModel, model, _dropTablesFirst, true);
            }
            else
            {
                platform.getSqlBuilder().createTables(model, _dropTablesFirst);
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
                task.log(ex.getLocalizedMessage(), Project.MSG_ERR);
            }
        }
        finally
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException ex)
                {}
            }
        }
    }
}
