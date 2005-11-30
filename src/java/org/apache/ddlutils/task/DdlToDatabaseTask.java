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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

/**
 * Ant task for working with DDL, e.g. generating the database from a schema, inserting data.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class DdlToDatabaseTask extends DatabaseTaskBase
{
    /** A single schema file to read. */
    private File _singleSchemaFile = null;
    /** The input files. */
    private ArrayList _fileSets = new ArrayList();
    /** Whether XML input files are validated against the internal or an external DTD. */
    private boolean _useInternalDtd = true;

    /**
     * Specifies whether XML input files are validated against the internal or an external DTD.
     *
     * @param isValidating <code>true</code> if input files are to be validated against the internal DTD
     */
    public void setUseInternalDtd(boolean useInternalDtd)
    {
        _useInternalDtd = useInternalDtd;
    }

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
     * Set the xml schema describing the application model.
     *
     * @param schemaFile The schema
     */
    public void setSchemaFile(File schemaFile)
    {
        _singleSchemaFile = schemaFile;
    }

    /**
     * Adds the "create database"-command.
     * 
     * @param command The command
     */
    public void addCreateDatabase(CreateDatabaseCommand command)
    {
        addCommand(command);
    }

    /**
     * Adds the "drop database"-command.
     * 
     * @param command The command
     */
    public void addDropDatabase(DropDatabaseCommand command)
    {
        addCommand(command);
    }

    /**
     * Adds the "write dtd to file"-command.
     * 
     * @param command The command
     */
    public void addWriteDtdToFile(WriteDtdToFileCommand command)
    {
        addCommand(command);
    }

    /**
     * Adds the "write schema to database"-command.
     * 
     * @param command The command
     */
    public void addWriteSchemaToDatabase(WriteSchemaToDatabaseCommand command)
    {
        addCommand(command);
    }

    /**
     * Adds the "write schema sql to file"-command.
     * 
     * @param command The command
     */
    public void addWriteSchemaSqlToFile(WriteSchemaSqlToFileCommand command)
    {
        addCommand(command);
    }

    /**
     * Adds the "write data to database"-command.
     * 
     * @param command The command
     */
    public void addWriteDataToDatabase(WriteDataToDatabaseCommand command)
    {
        addCommand(command);
    }

    /**
     * Adds the "write data to file"-command.
     * 
     * @param command The command
     */
    public void addWriteDataToFile(WriteDataToFileCommand command)
    {
        addCommand(command);
    }

    /**
     * {@inheritDoc}
     */
    protected Database readModel()
    {
        DatabaseIO reader = new DatabaseIO();
        Database   model  = null;

        reader.setUseInternalDtd(_useInternalDtd);
        if ((_singleSchemaFile != null) && !_fileSets.isEmpty())
        {
            throw new BuildException("Please use either the schemafile attribute or the sub fileset element, but not both");
        }
        if (_singleSchemaFile != null)
        {
            model = readSingleSchemaFile(reader, _singleSchemaFile);
        }
        else
        {
            for (Iterator it = _fileSets.iterator(); it.hasNext();)
            {
                FileSet          fileSet    = (FileSet)it.next();
                File             fileSetDir = fileSet.getDir(getProject());
                DirectoryScanner scanner    = fileSet.getDirectoryScanner(getProject());
                String[]         files      = scanner.getIncludedFiles();
    
                for (int idx = 0; (files != null) && (idx < files.length); idx++)
                {
                    Database curModel = readSingleSchemaFile(reader, new File(fileSetDir, files[idx]));
    
                    if (model == null)
                    {
                        model = curModel;
                    }
                    else if (curModel != null)
                    {
                        try
                        {
                            model.mergeWith(curModel);
                        }
                        catch (IllegalArgumentException ex)
                        {
                            throw new BuildException("Could not merge with schema from file "+files[idx]+": "+ex.getLocalizedMessage(), ex);
                        }
                    }
                }
            }
        }
        return model;
    }

    /**
     * Reads a single schema file.
     * 
     * @param reader     The schema reader 
     * @param schemaFile The schema file
     * @return The model
     */
    private Database readSingleSchemaFile(DatabaseIO reader, File schemaFile)
    {
        Database model = null;

        if (!schemaFile.isFile())
        {
            log("Path "+schemaFile.getAbsolutePath()+" does not denote a schema file", Project.MSG_ERR);
        }
        else if (!schemaFile.canRead())
        {
            log("Could not read schema file "+schemaFile.getAbsolutePath(), Project.MSG_ERR);
        }
        else
        {
            try
            {
                model = reader.read(schemaFile);
                log("Read schema file "+schemaFile.getAbsolutePath(), Project.MSG_INFO);
            }
            catch (Exception ex)
            {
                throw new BuildException("Could not read schema file "+schemaFile.getAbsolutePath()+": "+ex.getLocalizedMessage(), ex);
            }
        }
        return model;
    }
}
