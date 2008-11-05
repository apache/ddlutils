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

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.TestAgainstLiveDatabaseBase;
import org.apache.tools.ant.Project;

/**
 * Base class for ant task tests. 
 * 
 * @version $Revision: $
 */
public abstract class TestTaskBase extends TestAgainstLiveDatabaseBase
{
    /**
     * Returns an instance of the {@link DatabaseToDdlTask}, already configured with
     * a project and the tested database.
     * 
     * @return The task object
     */
    protected DatabaseToDdlTask getDatabaseToDdlTaskInstance()
    {
        DatabaseToDdlTask task       = new DatabaseToDdlTask();
        Properties        props      = getTestProperties();
        String            catalog    = props.getProperty(DDLUTILS_CATALOG_PROPERTY);
        String            schema     = props.getProperty(DDLUTILS_SCHEMA_PROPERTY);
        DataSource        dataSource = getDataSource();

        if (!(dataSource instanceof BasicDataSource))
        {
            fail("Datasource needs to be of type " + BasicDataSource.class.getName());
        }
        task.setProject(new Project());
        task.addConfiguredDatabase((BasicDataSource)getDataSource());
        task.setCatalogPattern(catalog);
        task.setSchemaPattern(schema);
        task.setUseDelimitedSqlIdentifiers(isUseDelimitedIdentifiers());
        return task;
    }

    /**
     * Returns an instance of the {@link DdlToDatabaseTask}, already configured with
     * a project and the tested database.
     * 
     * @return The task object
     */
    protected DdlToDatabaseTask getDdlToDatabaseTaskInstance()
    {
        DdlToDatabaseTask task       = new DdlToDatabaseTask();
        Properties        props      = getTestProperties();
        String            catalog    = props.getProperty(DDLUTILS_CATALOG_PROPERTY);
        String            schema     = props.getProperty(DDLUTILS_SCHEMA_PROPERTY);
        DataSource        dataSource = getDataSource();

        if (!(dataSource instanceof BasicDataSource))
        {
            fail("Datasource needs to be of type " + BasicDataSource.class.getName());
        }
        task.setProject(new Project());
        task.addConfiguredDatabase((BasicDataSource)getDataSource());
        task.setCatalogPattern(catalog);
        task.setSchemaPattern(schema);
        task.setUseDelimitedSqlIdentifiers(isUseDelimitedIdentifiers());
        return task;
    }
}
