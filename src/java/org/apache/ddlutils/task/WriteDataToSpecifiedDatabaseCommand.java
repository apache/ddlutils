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
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.ddlutils.builder.SqlBuilder;
import org.apache.ddlutils.builder.SqlBuilderFactory;
import org.apache.ddlutils.io.DataReader;
import org.apache.ddlutils.io.DataToDatabaseSink;
import org.apache.ddlutils.model.Database;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * Command for inserting data into a database that is specified as a sub-element.
 */
public class WriteDataToSpecifiedDatabaseCommand extends DatabaseCommand
{
    /** A single data file to insert */
    private File _singleDataFile = null;
    /** The input files */
    private ArrayList  _fileSets = new ArrayList();

    /**
     * Adds a fileset.
     * 
     * @param fileset The additional input files
     */
    public void addConfiguredFileset(FileSet fileset)
    {
        _fileSets.add(fileset);
    }

    /**
     * Set the xml data file.
     *
     * @param dataFile The data file
     */
    public void setDataFile(File dataFile)
    {
        _singleDataFile = dataFile;
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

        try
        {
            SqlBuilder         builder = SqlBuilderFactory.newSqlBuilder(getDatabaseType());
            DataToDatabaseSink sink    = new DataToDatabaseSink(getDataSource(), model, builder);
            DataReader         reader  = new DataReader();

            reader.setModel(model);
            reader.setSink(sink);
            if ((_singleDataFile != null) && !_fileSets.isEmpty())
            {
                throw new BuildException("Please use either the datafile attribute or the sub fileset element, but not both");
            }
            if (_singleDataFile != null)
            {
                readSingleDataFile(task, reader, _singleDataFile);
            }
            else
            {
                for (Iterator it = _fileSets.iterator(); it.hasNext();)
                {
                    FileSet          fileSet    = (FileSet)it.next();
                    File             fileSetDir = fileSet.getDir(task.getProject());
                    DirectoryScanner scanner    = fileSet.getDirectoryScanner(task.getProject());
                    String[]         files      = scanner.getIncludedFiles();
    
                    for (int idx = 0; (files != null) && (idx < files.length); idx++)
                    {
                        readSingleDataFile(task, reader, new File(fileSetDir, files[idx]));
                    }
                }
            }
        }
        catch (Exception ex)
        {
            throw new BuildException(ex);
        }
    }

    /**
     * Reads a single data file.
     * 
     * @param task       The parent task
     * @param reader     The data reader
     * @param schemaFile The schema file
     */
    private void readSingleDataFile(Task task, DataReader reader, File schemaFile)
    {
        if (!schemaFile.exists())
        {
            task.log("Could not find data file "+schemaFile.getAbsolutePath(), Project.MSG_ERR);
        }
        else if (!schemaFile.isFile())
        {
            task.log("Path "+schemaFile.getAbsolutePath()+" does not denote a data file", Project.MSG_ERR);
        }
        else if (!schemaFile.canRead())
        {
            task.log("Could not read data file "+schemaFile.getAbsolutePath(), Project.MSG_ERR);
        }
        else
        {
            try
            {
                reader.parse(schemaFile);
                task.log("Read data file "+schemaFile.getAbsolutePath(), Project.MSG_INFO);
            }
            catch (Exception ex)
            {
                throw new BuildException("Could not read data file "+schemaFile.getAbsolutePath(), ex);
            }
        }
    }
}
