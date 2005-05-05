package org.apache.ddlutils.io;

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

import java.io.FileInputStream;
import java.util.Properties;
import javax.sql.DataSource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.builder.TestBuilderBase;

/**
 * Base class for database writer tests.
 */
public abstract class TestDatabaseWriterBase extends TestBuilderBase
{
    /** The name of the properties file that contains the settings for the connection to test against */
    public static final String DATASOURCE_PROPERTIES = "jdbc.properties";

    /** The data source to test against */
    private DataSource _dataSource;

    /**
     * Creates a new test case instance.
     */
    public TestDatabaseWriterBase()
    {
        super();
        initDataSource();
    }

    /**
     * Initializes the test datasource.
     */
    private void initDataSource()
    {
        try
        {
            Properties props = new Properties();
    
            props.load(new FileInputStream(DATASOURCE_PROPERTIES));
            
            _dataSource = new BasicDataSource();
    
            BeanUtils.copyProperties(_dataSource, props);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Returns the data source.
     * 
     * @return The data source
     */
    protected DataSource getDataSource()
    {
        return _dataSource;
    }
}
