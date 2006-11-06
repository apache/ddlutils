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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang.exception.ExceptionUtils;
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
 * Inserts the data defined by the data XML file(s) into the database. This requires the schema
 * in the database to match the schema defined by the XML files specified at the enclosing task.<br/>
 * DdlUtils will honor the order imposed by the foreign keys. Ie. first all required entries are
 * inserted, then the dependent ones. Obviously this requires that no circular references exist
 * in the schema (DdlUtils currently does not check this). Also, the referenced entries must be
 * present in the data, otherwise the task will fail. This behavior can be turned off via the
 * <code>ensureForeignKeyOrder</code> attribute.<br/>
 * In order to define data for foreign key dependencies that use auto-incrementing primary keys,
 * simply use unique values for their columns. DdlUtils will automatically use the real primary
 * key values. Note though that not every database supports the retrieval of auto-increment values.
 * 
 * @version $Revision: 289996 $
 * @ant.task name="writeDataToDatabase"
 */
public class WriteDataToDatabaseCommand extends ConvertingDatabaseCommand
{
    /** A single data file to insert. */
    private File      _singleDataFile = null;
    /** The input files. */
    private ArrayList _fileSets = new ArrayList();
    /** Whether foreign key order shall be followed when inserting data into the database. */
    private boolean _ensureFKOrder = true;
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
     * Specifies the name of the single XML file that contains the data to insert into the database.
     *
     * @param dataFile The data file
     * @ant.not-required If not specified, no data is inserted into the database upon creation.
     */
    public void setDataFile(File dataFile)
    {
        _singleDataFile = dataFile;
    }

    /**
     * The maximum number of insert statements to combine in one batch. The number typically
     * depends on the JDBC driver and the amount of available memory.<br/>
     * This value is only used if <code>useBatchMode</code> is <code>true</code>.
     *
     * @param batchSize The number of objects
     * @ant.not-required The default value is 1
     */
    public void setBatchSize(int batchSize)
    {
        _batchSize = new Integer(batchSize);
    }

    /**
     * Specifies whether batch mode shall be used for inserting the data. In this mode, insert statements
     * for the same table are bundled together and executed as one statement which can be a lot faster
     * than single insert statements. To achieve the highest performance, you should group the data in the
     * XML file according to the tables because a batch insert only works for one table which means when
     * the table changes the batch is executed and a new one will be started.
     *
     * @param useBatchMode <code>true</code> if batch mode shall be used
     * @ant.not-required Per default, batch mode is not used
     */
    public void setUseBatchMode(boolean useBatchMode)
    {
        _useBatchMode = Boolean.valueOf(useBatchMode);
    }

    /**
     * Specifies whether the foreign key order shall be honored when inserted data into the database.
     * Otherwise, DdlUtils will simply assume that the entry order is ok. Note that execution will
     * be slower when DdlUtils has to ensured in the inserted data, so if you know that the data is
     * specified in foreign key order (i.e. referenced rows come before referencing rows), then
     * turn this off.
     *
     * @param ensureFKOrder <code>true</code> if the foreign key order shall be followed
     * @ant.not-required Per default, foreign key order is honored
     */
    public void setEnsureForeignKeyOrder(boolean ensureFKOrder)
    {
        _ensureFKOrder = ensureFKOrder;
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

            sink.setEnsureForeignKeyOrder(_ensureFKOrder);
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
                task.log("Written data file "+dataFile.getAbsolutePath() + " to database", Project.MSG_INFO);
            }
            catch (Exception ex)
            {
                if (isFailOnError())
                {
                    throw new BuildException("Could not parse or write data file "+dataFile.getAbsolutePath(), ex);
                }
                else
                {
                    task.log("Could not parse or write data file "+dataFile.getAbsolutePath() + ":", Project.MSG_ERR);
                    task.log(ExceptionUtils.getFullStackTrace(ex));
                }
            }
        }
    }
}
