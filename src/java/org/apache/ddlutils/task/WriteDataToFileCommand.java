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
import java.io.FileOutputStream;

import org.apache.ddlutils.model.Database;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Reads the data currently in the table in the live database (as specified by the
 * enclosing task), and writes it as XML to a file.
 * 
 * @version $Revision: 289996 $
 * @ant.task name="writeDataToFile"
 */
public class WriteDataToFileCommand extends ConvertingDatabaseCommand
{
    /** The file to output the data to. */
    private File   _outputFile;
    /** The character encoding to use. */
    private String _encoding;

    /**
     * Specifies the file to write the data XML to.
     * 
     * @param outputFile The output file
     * @ant.required
     */
    public void setOutputFile(File outputFile)
    {
        _outputFile = outputFile;
    }

    /**
     * Specifies the encoding of the XML file.
     * 
     * @param encoding The encoding
     * @ant.not-required The default encoding is <code>UTF-8</code>.
     */
    public void setEncoding(String encoding)
    {
        _encoding = encoding;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(Task task, Database model) throws BuildException
    {
        try
        {
            getDataIO().writeDataToXML(getPlatform(),
                                       new FileOutputStream(_outputFile), _encoding);
        }
        catch (Exception ex)
        {
            throw new BuildException(ex);
        }
    }

}
