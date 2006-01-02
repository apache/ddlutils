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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.Platform;
import org.apache.tools.ant.BuildException;

/**
 * Base type for commands that have the database info embedded.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public abstract class DatabaseCommand implements Command
{
    /** The platform configuration. */
    private PlatformConfiguration _platformConf = new PlatformConfiguration();
    /** Whether to stop execution upon an error. */
    private boolean _failOnError = true;

    /**
     * Returns the database type.
     * 
     * @return The database type
     */
    protected String getDatabaseType()
    {
        return _platformConf.getDatabaseType();
    }

    /**
     * Returns the data source to use for accessing the database.
     * 
     * @return The data source
     */
    protected BasicDataSource getDataSource()
    {
        return _platformConf.getDataSource();
    }

    /**
     * Sets the platform configuration.
     * 
     * @param platformConf The platform configuration
     */
    protected void setPlatformConfiguration(PlatformConfiguration platformConf)
    {
        _platformConf = platformConf;
    }

    /**
     * Determines whether the command execution will be stopped upon an error.
     * Default value is <code>true</code>.
     *
     * @return <code>true</code> if the execution stops in case of an error
     */
    public boolean isFailOnError()
    {
        return _failOnError;
    }

    /**
     * Specifies whether the command execution will be stopped upon an error.
     *
     * @param failOnError <code>true</code> if the execution stops in case of an error
     */
    public void setFailOnError(boolean failOnError)
    {
        _failOnError = failOnError;
    }

    /**
     * Creates the platform for the configured database.
     * 
     * @return The platform
     */
    protected Platform getPlatform() throws BuildException
    {
        return _platformConf.getPlatform();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isRequiringModel()
    {
        return true;
    }
}
