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
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.ddlutils.Platform;
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
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class WriteDataToDatabaseCommand extends ConvertingDatabaseCommand
{
    /** A single data file to insert. */
    private File      _singleDataFile = null;
    /** The input files. */
    private ArrayList _fileSets = new ArrayList();
    /** Whether we should use batch mode. */
    private Boolean _useBatchMode;
    /** The maximum number of objects to insert in one batch. */
    private Integer _batchSize;
    
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

    /**
     * Sets the maximum number of objects to insert in one batch.
     *
     * @param batchSize The number of objects
     */
    public void setBatchSize(int batchSize)
    {
        _batchSize = new Integer(batchSize);
    }

    /**
     * Specifies whether we shall be using batch mode.
     *
     * @param useBatchMode <code>true</code> if we shall use batch mode
     */
    public void setUseBatchMode(boolean useBatchMode)
    {
        _useBatchMode = Boolean.valueOf(useBatchMode);
    }

    /**
     * {@inheritDoc}
     */
    public void execute(Task task, Database model) throws BuildException
    {
        try
        {
            Platform           platform = getPlatform();
            DataToDatabaseSink sink     = new DataToDatabaseSink(platform, model);
            DataReader         reader   = new DataReader();

            if (_useBatchMode != null)
            {
                sink.setUseBatchMode(_useBatchMode.booleanValue());
                if (_batchSize != null)
                {
                    sink.setBatchSize(_batchSize.intValue());
                }
            }
            
            reader.setModel(model);
            reader.setSink(sink);
            registerConverters(reader.getConverterConfiguration());
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
            if (ex instanceof BuildException)
            {
                throw (BuildException)ex;
            }
            else
            {
                throw new BuildException(ex);
            }
        }
    }

    /**
     * Reads a single data file.
     * 
     * @param task     The parent task
     * @param reader   The data reader
     * @param dataFile The schema file
     */
    private void readSingleDataFile(Task task, DataReader reader, File dataFile)
    {
        if (!dataFile.exists())
        {
            task.log("Could not find data file "+dataFile.getAbsolutePath(), Project.MSG_ERR);
        }
        else if (!dataFile.isFile())
        {
            task.log("Path "+dataFile.getAbsolutePath()+" does not denote a data file", Project.MSG_ERR);
        }
        else if (!dataFile.canRead())
        {
            task.log("Could not read data file "+dataFile.getAbsolutePath(), Project.MSG_ERR);
        }
        else
        {
            try
            {
                reader.parse(dataFile);
                task.log("Read data file "+dataFile.getAbsolutePath(), Project.MSG_INFO);
            }
            catch (Exception ex)
            {
                if (isFailOnError())
                {
                    throw new BuildException("Could not read data file "+dataFile.getAbsolutePath(), ex);
                }
                else
                {
                    task.log("Could not read data file "+dataFile.getAbsolutePath(), Project.MSG_ERR);
                }
            }
        }
    }
}
