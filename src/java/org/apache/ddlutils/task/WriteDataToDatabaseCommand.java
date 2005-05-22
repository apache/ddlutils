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

import javax.sql.DataSource;

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
 * Command for inserting data into a database.
 */
public class WriteDataToDatabaseCommand implements Command, WantsDatabaseInfo
{
    /** The data source pointing to the database */
    private DataSource _dataSource;
    /** The database type */
    private String     _databaseType;
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
        FileSet fileSet = new FileSet();

        fileSet.setIncludesfile(dataFile);
        _fileSets.add(fileSet);
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.task.WantsDatabaseInfo#setDatabaseInfo(javax.sql.DataSource, java.lang.String)
     */
    public void setDatabaseInfo(DataSource dataSource, String type) throws BuildException
    {
        _dataSource   = dataSource;
        _databaseType = type;
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.task.Command#execute(org.apache.tools.ant.Task, org.apache.ddlutils.model.Database)
     */
    public void execute(Task task, Database model) throws BuildException
    {
        try
        {
            SqlBuilder         builder = SqlBuilderFactory.newSqlBuilder(_databaseType);
            DataToDatabaseSink sink    = new DataToDatabaseSink(_dataSource, model, builder);
            DataReader         reader  = new DataReader(model, sink);

            for (Iterator it = _fileSets.iterator(); it.hasNext();)
            {
                FileSet          fileSet    = (FileSet)it.next();
                File             fileSetDir = fileSet.getDir(task.getProject());
                DirectoryScanner scanner    = fileSet.getDirectoryScanner(task.getProject());
                String[]         files      = scanner.getIncludedFiles();

                for (int idx = 0; (files != null) && (idx < files.length); idx++)
                {
                    File curSchemaFile = new File(fileSetDir, files[idx]);

                    if (!curSchemaFile.exists())
                    {
                        task.log("Could not find data file "+files[idx], Project.MSG_ERR);
                    }
                    else if (!curSchemaFile.isFile())
                    {
                        task.log("Path "+curSchemaFile.getAbsolutePath()+" does not denote a data file", Project.MSG_ERR);
                    }
                    else if (!curSchemaFile.canRead())
                    {
                        task.log("Could not read data file "+curSchemaFile.getAbsolutePath(), Project.MSG_ERR);
                    }
                    else
                    {
                        try
                        {
                            reader.parse(curSchemaFile);
                            task.log("Read data file "+curSchemaFile.getAbsolutePath(), Project.MSG_INFO);
                        }
                        catch (Exception ex)
                        {
                            throw new BuildException("Could not read data file "+curSchemaFile.getAbsolutePath(), ex);
                        }
                    }
                }
            }

        }
        catch (Exception ex)
        {
            throw new BuildException(ex);
        }
    }

}
