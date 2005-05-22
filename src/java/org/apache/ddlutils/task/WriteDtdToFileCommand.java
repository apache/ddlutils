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

import org.apache.ddlutils.io.DataDtdWriter;
import org.apache.ddlutils.model.Database;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * The command for creating a data DTD for a given database model.
 */
public class WriteDtdToFileCommand implements Command
{
    /** The file to output the DTD to */
    private File _outputFile;

    /**
     * Sets the file to output the DTD to.
     * 
     * @param outputFile The output file
     */
    public void setOutputFile(File outputFile)
    {
        _outputFile = outputFile;
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.task.Command#execute(org.apache.ddlutils.model.Database)
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

        try
        {
            FileWriter    outputWriter = new FileWriter(_outputFile);
            DataDtdWriter dtdWriter    = new DataDtdWriter();

            dtdWriter.writeDtd(model, outputWriter);
            outputWriter.close();
            task.log("Written DTD to "+_outputFile.getAbsolutePath(), Project.MSG_INFO);
        }
        catch (Exception ex)
        {
            throw new BuildException("Failed to write to output file "+_outputFile.getAbsolutePath(), ex);
        }
    }
}
