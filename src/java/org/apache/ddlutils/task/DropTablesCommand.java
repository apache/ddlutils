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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Database;
import org.apache.tools.ant.BuildException;

/**
 * Sub task for dropping tables.
 * 
 * @version $Revision: $
 * @ant.task name="dropTables"
 */
public class DropTablesCommand extends DatabaseCommand
{
    /**
     * {@inheritDoc}
     */
    public boolean isRequiringModel()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(DatabaseTaskBase task, Database model) throws BuildException
    {
        BasicDataSource dataSource = getDataSource();

        if (dataSource == null)
        {
            throw new BuildException("No database specified.");
        }

        Platform platform    = getPlatform();
        Database targetModel = new Database();

        try
        {
            platform.alterModel(model, targetModel, isFailOnError());

            _log.info("Dropped tables");
        }
        catch (Exception ex)
        {
            handleException(ex, ex.getMessage());
        }
    }
}
