package org.apache.ddlutils;

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

import java.io.StringReader;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.builder.BuilderUtils;
import org.apache.ddlutils.dynabean.DynaSqlException;
import org.apache.ddlutils.io.DataReader;
import org.apache.ddlutils.io.DataToDatabaseSink;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.util.DDLExecutor;

/**
 * Base class for database writer tests.
 */
public abstract class TestDatabaseWriterBase extends TestBuilderBase
{
    /** The name of the property that specifies properties file with the settings for the connection to test against */
    public static final String JDBC_PROPERTIES_PROPERTY = "jdbc.properties.file";
    /** The prefix for properties of the datasource */
    public static final String DATASOURCE_PROPERTY_PREFIX = "datasource.";
    /** The property for specifying the platform */
    public static final String PLATFORM_PROPERTY = "ddlutils.platform";

    /** The data source to test against */
    private DataSource _dataSource;
    /** The platform */
    private String _platform;
    /** The database model */
    private Database _model;

    /**
     * Creates a new test case instance.
     */
    public TestDatabaseWriterBase()
    {
        super();
        init();
    }

    /**
     * Initializes the test datasource and the platform.
     */
    private void init()
    {
        Properties props    = new Properties();
        String     propFile = System.getProperty(JDBC_PROPERTIES_PROPERTY);

        try
        {
            props.load(getClass().getResourceAsStream(propFile));

            String dataSourceClass = props.getProperty(DATASOURCE_PROPERTY_PREFIX + "class", BasicDataSource.class.getName());

            _dataSource = (DataSource)Class.forName(dataSourceClass).newInstance();

            for (Iterator it = props.entrySet().iterator(); it.hasNext();)
            {
                Map.Entry entry    = (Map.Entry)it.next();
                String    propName = (String)entry.getKey();

                if (propName.startsWith(DATASOURCE_PROPERTY_PREFIX) && !propName.equals(DATASOURCE_PROPERTY_PREFIX +"class"))
                {
                    BeanUtils.setProperty(_dataSource,
                                          propName.substring(DATASOURCE_PROPERTY_PREFIX.length()),
                                          entry.getValue());
                }
            }
        }
        catch (Exception ex)
        {
            throw new DynaSqlException(ex);
        }
        try
        {
            _platform = new BuilderUtils().determineDatabaseType(_dataSource);
        }
        catch (SQLException ex)
        {
            throw new DynaSqlException(ex);
        }
        if (_platform == null)
        {
            // could not determine, perhaps the property has been set ?
            _platform = props.getProperty(PLATFORM_PROPERTY);
            if (_platform == null)
            {
                throw new DynaSqlException("Could not determine platform from datasource, please specify it in the jdbc.properties via the ddlutils.platform property");
            }
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

    /* (non-Javadoc)
     * @see org.apache.ddlutils.TestBuilderBase#getDatabaseName()
     */
    protected String getDatabaseName()
    {
        return _platform;
    }

    /**
     * Returns the database model.
     * 
     * @return The model
     */
    protected Database getModel()
    {
        return _model;
    }

    /**
     * Returns a DDL executor instance.
     * 
     * @return The executor instance
     */
    protected DDLExecutor getDDLExecutor()
    {
        return new DDLExecutor(getDataSource(), getBuilder());
    }


    /* (non-Javadoc)
     * @see org.apache.ddlutils.TestBuilderBase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        if (_model != null)
        {
            dropDatabase();
            _model = null;
        }
        super.tearDown();
    }

    /**
     * Creates a new database from the given XML database schema.
     * 
     * @param schemaXml The XML database schema
     * @return The database model
     */
    protected void createDatabase(String schemaXml) throws DynaSqlException
    {
        try
        {
            _model = parseDatabaseFromString(schemaXml);

            getDDLExecutor().createDatabase(_model, true);
        }
        catch (Exception ex)
        {
            throw new DynaSqlException(ex);
        }
    }

    /**
     * Inserts data into the database.
     * 
     * @param dataXml The data xml
     */
    protected void insertData(String dataXml) throws DynaSqlException
    {
        try
        {
            DataReader dataReader = new DataReader();

            dataReader.setModel(_model);
            dataReader.setSink(new DataToDatabaseSink(getDataSource(), _model, getBuilder()));
            dataReader.parse(new StringReader(dataXml));
        }
        catch (Exception ex)
        {
            throw new DynaSqlException(ex);
        }
    }

    /**
     * Drops the tables defined in the database model.
     */
    protected void dropDatabase() throws DynaSqlException
    {
        try
        {
            getDDLExecutor().dropDatabase(_model);
        }
        catch (Exception ex)
        {
            throw new DynaSqlException(ex);
        }
    }

    /**
     * Determines the value of the bean's property that has the given name. Depending on the
     * case-setting of the current builder, the case of teh name is considered or not. 
     * 
     * @param bean     The bean
     * @param propName The name of the property
     * @return The value
     */
    protected Object getPropertyValue(DynaBean bean, String propName)
    {
        if (getBuilder().isCaseSensitive())
        {
            return bean.get(propName);
        }
        else
        {
            DynaProperty[] props = bean.getDynaClass().getDynaProperties();
    
            for (int idx = 0; idx < props.length; idx++)
            {
                if (propName.equalsIgnoreCase(props[idx].getName()))
                {
                    return bean.get(props[idx].getName());
                }
            }
            throw new IllegalArgumentException("The bean has no property with the name "+propName);
        }
    }
}
