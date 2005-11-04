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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Database;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Command for creating a database.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 231306 $
 */
public class CreateDatabaseCommand extends DatabaseCommand
{
    /** The additional creation parameters. */
    private ArrayList _parameters = new ArrayList();

    /**
     * Adds a parameter which is a name-value pair.
     * 
     * @param param The parameter
     */
    public void addConfiguredParameter(Parameter param)
    {
        _parameters.add(param);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isRequiringModel()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(Task task, Database model) throws BuildException
    {
        BasicDataSource dataSource = getDataSource();

        if (dataSource == null)
        {
            throw new BuildException("No database specified.");
        }

        Platform platform = getPlatform();
        
        try
        {
            platform.createDatabase(dataSource.getDriverClassName(),
                                    dataSource.getUrl(),
                                    dataSource.getUsername(),
                                    dataSource.getPassword(),
                                    getFilteredParameters(platform.getName()));

            task.log("Created database", Project.MSG_INFO);
        }
        catch (UnsupportedOperationException ex)
        {
            task.log("Database platform "+getPlatform().getName()+" does not support database creation via JDBC", Project.MSG_ERR);
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
    }

    /**
     * Filters the parameters for the indicated platform.
     * 
     * @param platformName The name of the platform
     * @return The filtered parameters
     */
    private Map getFilteredParameters(String platformName)
    {
        LinkedHashMap parameters = new LinkedHashMap();

        for (Iterator it = _parameters.iterator(); it.hasNext();)
        {
            Parameter param = (Parameter)it.next();

            if (param.isForPlatform(platformName))
            {
                parameters.put(param.getName(), param.getValue());
            }
        }
        return parameters;
    }
}
