package org.apache.ddlutils;

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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import junit.framework.AssertionFailedError;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.io.DataReader;
import org.apache.ddlutils.io.DataToDatabaseSink;
import org.apache.ddlutils.model.CloneHelper;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.CreationParameters;
import org.apache.ddlutils.platform.DefaultValueHelper;
import org.apache.ddlutils.platform.firebird.FirebirdPlatform;
import org.apache.ddlutils.platform.interbase.InterbasePlatform;

/**
 * Base class for database writer tests.
 * 
 * @version $Revision: 289996 $
 */
public abstract class TestDatabaseWriterBase extends TestPlatformBase
{
    /** The name of the property that specifies properties file with the settings for the connection to test against. */
    public static final String JDBC_PROPERTIES_PROPERTY = "jdbc.properties.file";
    /** The prefix for properties of the datasource. */
    public static final String DATASOURCE_PROPERTY_PREFIX = "datasource.";
    /** The prefix for properties for ddlutils. */
    public static final String DDLUTILS_PROPERTY_PREFIX = "ddlutils.";
    /** The property for specifying the platform. */
    public static final String DDLUTILS_PLATFORM_PROPERTY = DDLUTILS_PROPERTY_PREFIX + "platform";
    /** The property specifying the catalog for the tests. */
    public static final String DDLUTILS_CATALOG_PROPERTY = DDLUTILS_PROPERTY_PREFIX + "catalog";
    /** The property specifying the schema for the tests. */
    public static final String DDLUTILS_SCHEMA_PROPERTY = DDLUTILS_PROPERTY_PREFIX + "schema";
    /** The prefix for table creation properties. */
    public static final String DDLUTILS_TABLE_CREATION_PREFIX = DDLUTILS_PROPERTY_PREFIX + "tableCreation.";

    /** The test properties as defined by an external properties file. */
    private static Properties _testProps;
    /** The data source to test against. */
    private static DataSource _dataSource;
    /** The database name. */
    private static String _databaseName;
    /** The database model. */
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
     * Returns the test properties.
     * 
     * @return The properties
     */
    protected Properties getTestProperties()
    {
    	if (_testProps == null)
    	{
    		String propFile = System.getProperty(JDBC_PROPERTIES_PROPERTY);
	
	        if (propFile == null)
	        {
	        	throw new RuntimeException("Please specify the properties file via the jdbc.properties.file environment variable");
	        }

	        InputStream propStream = null;

	        try
	        {
	            propStream = TestDatabaseWriterBase.class.getResourceAsStream(propFile);

	            if (propStream == null)
	            {
	                propStream = new FileInputStream(propFile);
	            }

	            Properties props = new Properties();

	            props.load(propStream);
	            _testProps = props;
	        }
	        catch (Exception ex)
	        {
	        	throw new RuntimeException(ex);
	        }
	        finally
	        {
	            if (propStream != null)
	            {
	                try
	                {
	                    propStream.close();
	                }
	                catch (IOException ex)
	                {
	                    getLog().error("Could not close the stream used to read the test jdbc properties", ex);
	                }
	            }
	        }
    	}
    	return _testProps;
    }
    
    /**
     * Initializes the test datasource and the platform.
     */
    private void init()
    {
        // the data source won't change during the tests, hence
        // it is static and needs to be initialized only once
        if (_dataSource != null)
        {
            return;
        }

        Properties props = getTestProperties();

        try
        {
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
            throw new DatabaseOperationException(ex);
        }

        _databaseName = props.getProperty(DDLUTILS_PLATFORM_PROPERTY);
        if (_databaseName == null)
        {
            // property not set, then try to determine
            _databaseName = new PlatformUtils().determineDatabaseType(_dataSource);
            if (_databaseName == null)
            {
                throw new DatabaseOperationException("Could not determine platform from datasource, please specify it in the jdbc.properties via the ddlutils.platform property");
            }
        }
    }

    /**
     * Returns the test table creation parameters for the given model.
     * 
     * @param model The model
     * @return The creation parameters
     */
    protected CreationParameters getTableCreationParameters(Database model)
    {
        CreationParameters params = new CreationParameters();

        for (Iterator entryIt = _testProps.entrySet().iterator(); entryIt.hasNext();)
        {
            Map.Entry entry = (Map.Entry)entryIt.next();
            String    name  = (String)entry.getKey();
            String    value = (String)entry.getValue();

            if (name.startsWith(DDLUTILS_TABLE_CREATION_PREFIX))
            {
                name = name.substring(DDLUTILS_TABLE_CREATION_PREFIX.length());
                for (int tableIdx = 0; tableIdx < model.getTableCount(); tableIdx++)
                {
                    params.addParameter(model.getTable(tableIdx), name, value);
                }
            }
        }
        return params;
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

    /**
     * {@inheritDoc}
     */
    protected String getDatabaseName()
    {
        return _databaseName;
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
     * {@inheritDoc}
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        getPlatform().setDataSource(getDataSource());
    }

    /**
     * {@inheritDoc}
     */
    protected void tearDown() throws Exception
    {
        try
        {
            if (_model != null)
            {
                dropDatabase();
                _model = null;
            }
        }
        finally
        {
            assertAndEnsureClearDatabase();
            super.tearDown();
        }
    }

    /**
     * Creates a new database from the given XML database schema.
     * 
     * @param schemaXml The XML database schema
     * @return The parsed database model
     */
    protected Database createDatabase(String schemaXml) throws DatabaseOperationException
    {
    	Database model = parseDatabaseFromString(schemaXml);

    	createDatabase(model);
    	return model;
    }

    /**
     * Creates a new database from the given model.
     * 
     * @param model The model
     */
    protected void createDatabase(Database model) throws DatabaseOperationException
    {
        try
        {
            _model = model;

            getPlatform().setSqlCommentsOn(false);
            getPlatform().createModel(_model, getTableCreationParameters(_model), false, false);
        }
        catch (Exception ex)
        {
            throw new DatabaseOperationException(ex);
        }
    }

    /**
     * Alters the database to match the given model.
     * 
     * @param schemaXml The model XML
     * @return The model object
     */
    protected Database alterDatabase(String schemaXml) throws DatabaseOperationException
    {
        Database model = parseDatabaseFromString(schemaXml);

        alterDatabase(model);
        return model;
    }

    /**
     * Alters the database to match the given model.
     * 
     * @param desiredModel The model
     */
    protected void alterDatabase(Database desiredModel) throws DatabaseOperationException
    {
        try
        {
            _model = desiredModel;
            _model.resetDynaClassCache();

            Database liveModel = readModelFromDatabase(desiredModel.getName());

            getPlatform().setSqlCommentsOn(false);
            getPlatform().alterModel(liveModel, _model, getTableCreationParameters(_model), false);
        }
        catch (Exception ex)
        {
            throw new DatabaseOperationException(ex);
        }
    }

    /**
     * Inserts data into the database.
     * 
     * @param dataXml The data xml
     * @return The database
     */
    protected Database insertData(String dataXml) throws DatabaseOperationException
    {
        try
        {
            DataReader dataReader = new DataReader();

            dataReader.setModel(_model);
            dataReader.setSink(new DataToDatabaseSink(getPlatform(), _model));
            dataReader.getSink().start();
            dataReader.parse(new StringReader(dataXml));
            dataReader.getSink().end();
            return _model;
        }
        catch (Exception ex)
        {
            throw new DatabaseOperationException(ex);
        }
    }

    /**
     * Drops the tables defined in the database model.
     */
    protected void dropDatabase() throws DatabaseOperationException
    {
        getPlatform().dropModel(_model, true);
    }

    /**
     * Checks that the database is clear, and if not clears it (no tables, sequences etc. left) and
     * throws an {@link AssertionFailedError}.
     */
    protected void assertAndEnsureClearDatabase()
    {
        Database liveModel = readModelFromDatabase("tmp");
        boolean  hasStuff  = false;

        if (liveModel.getTableCount() > 0)
        {
            hasStuff = true;
            try
            {
                getPlatform().dropModel(liveModel, true);
            }
            catch (Exception ex)
            {
                getLog().error("Could not clear database", ex);
            }
        }
        if (FirebirdPlatform.DATABASENAME.equals(getPlatform().getName()) ||
            InterbasePlatform.DATABASENAME.equals(getPlatform().getName()))
        {
            Connection connection = null;

            try
            {
                connection = getPlatform().borrowConnection();

                hasStuff = hasStuff | dropTriggers(connection);
                hasStuff = hasStuff | dropGenerators(connection);
            }
            catch (Exception ex)
            {
                getLog().error("Could not clear database", ex);
            }
            finally
            {
                getPlatform().returnConnection(connection);
            }
        }
        // TODO: Check for sequences
        if (hasStuff)
        {
            fail("Database is not empty after test");
        }
    }

    /**
     * Drops generators left by a test in a Firebird/Interbase database.
     * 
     * @param connection The database connection
     * @return Whether generators were dropped
     */
    private boolean dropGenerators(Connection connection)
    {
        Statement stmt          = null;
        boolean   hasGenerators = false;

        try
        {
            stmt = connection.createStatement();

            ResultSet rs    = stmt.executeQuery("SELECT RDB$GENERATOR_NAME FROM RDB$GENERATORS WHERE RDB$GENERATOR_NAME NOT LIKE '%$%'");
            List      names = new ArrayList();
    
            while (rs.next())
            {
                names.add(rs.getString(1));
            }
            rs.close();
    
            for (Iterator it = names.iterator(); it.hasNext();)
            {
                String name = (String)it.next();

                if (name.toLowerCase().startsWith("gen_"))
                {
                    hasGenerators = true;
                    stmt.execute("DROP GENERATOR " + name);
                }
            }
        }
        catch (Exception ex)
        {
            getLog().error("Error while dropping the remaining generators", ex);
        }
        finally
        {
            if (stmt != null)
            {
                try
                {
                    stmt.close();
                }
                catch (Exception ex)
                {
                    getLog().error("Error while clearing the database", ex);
                }
            }
        }
        return hasGenerators;
    }

    /**
     * Drops triggers left by a test in a Firebird/Interbase database.
     * 
     * @param connection The database connection
     * @return Whether triggers were dropped
     */
    private boolean dropTriggers(Connection connection)
    {
        Statement stmt        = null;
        boolean   hasTriggers = false;

        try
        {
            stmt = connection.createStatement();

            ResultSet rs    = stmt.executeQuery("SELECT * FROM RDB$TRIGGERS WHERE RDB$SYSTEM_FLAG IS NULL OR RDB$SYSTEM_FLAG = 0");
            List      names = new ArrayList();
    
            while (rs.next())
            {
                names.add(rs.getString(1));
            }
            rs.close();
    
            for (Iterator it = names.iterator(); it.hasNext();)
            {
                String name = (String)it.next();

                if (name.toLowerCase().startsWith("trg_"))
                {
                    hasTriggers = true;
                    stmt.execute("DROP TRIGGER " + name);
                }
            }
        }
        catch (Exception ex)
        {
            getLog().error("Error while dropping the remaining triggers", ex);
        }
        finally
        {
            if (stmt != null)
            {
                try
                {
                    stmt.close();
                }
                catch (Exception ex)
                {
                    getLog().error("Error while clearing the database", ex);
                }
            }
        }
        return hasTriggers;
    }
    
    /**
     * Reads the database model from a live database.
     * 
     * @param databaseName The name of the resulting database
     * @return The model
     */
    protected Database readModelFromDatabase(String databaseName)
    {
    	Properties props   = getTestProperties();
        String     catalog = props.getProperty(DDLUTILS_CATALOG_PROPERTY);
        String     schema  = props.getProperty(DDLUTILS_SCHEMA_PROPERTY);

    	return getPlatform().readModelFromDatabase(databaseName, catalog, schema, null);
    }

    /**
     * Returns a copy of the given model adjusted for type changes because of the native type mappings
     * which when read back from the database will map to different types.
     * 
     * @param sourceModel The source model
     * @return The adjusted model
     */
    protected Database adjustModel(Database sourceModel)
    {
        Database model = new CloneHelper().clone(sourceModel);

        for (int tableIdx = 0; tableIdx < model.getTableCount(); tableIdx++)
        {
            Table table = model.getTable(tableIdx);

            for (int columnIdx = 0; columnIdx < table.getColumnCount(); columnIdx++)
            {
                Column column     = table.getColumn(columnIdx);
                int    origType   = column.getTypeCode();
                int    targetType = getPlatformInfo().getTargetJdbcType(origType);

                // we adjust the column types if the native type would back-map to a
                // different jdbc type
                if (targetType != origType)
                {
                    column.setTypeCode(targetType);
                    // we should also adapt the default value
                    if (column.getDefaultValue() != null)
                    {
                        DefaultValueHelper helper = getPlatform().getSqlBuilder().getDefaultValueHelper();

                        column.setDefaultValue(helper.convert(column.getDefaultValue(), origType, targetType));
                    }
                }
                // we also promote the default size if the column has no size
                // spec of its own
                if ((column.getSize() == null) && getPlatformInfo().hasSize(targetType))
                {
                    Integer defaultSize = getPlatformInfo().getDefaultSize(targetType);

                    if (defaultSize != null)
                    {
                        column.setSize(defaultSize.toString());
                    }
                }
                // finally the platform might return a synthetic default value if the column
                // is a primary key column
                if (getPlatformInfo().isSyntheticDefaultValueForRequiredReturned() &&
                    (column.getDefaultValue() == null) && column.isRequired() && !column.isAutoIncrement())
                {
                    switch (column.getTypeCode())
                    {
                        case Types.TINYINT:
                        case Types.SMALLINT:
                        case Types.INTEGER:
                        case Types.BIGINT:
                            column.setDefaultValue("0");
                            break;
                        case Types.REAL:
                        case Types.FLOAT:
                        case Types.DOUBLE:
                            column.setDefaultValue("0.0");
                            break;
                        case Types.BIT:
                            column.setDefaultValue("false");
                            break;
                        default:
                            column.setDefaultValue("");
                            break;
                    }
                }
                if (column.isPrimaryKey() && getPlatformInfo().isPrimaryKeyColumnAutomaticallyRequired())
                {
                    column.setRequired(true);
                }
                if (column.isAutoIncrement() && getPlatformInfo().isIdentityColumnAutomaticallyRequired())
                {
                    column.setRequired(true);
                }
            }
            // we also add the default names to foreign keys that are initially unnamed
            for (int fkIdx = 0; fkIdx < table.getForeignKeyCount(); fkIdx++)
            {
                ForeignKey fk = table.getForeignKey(fkIdx);

                if (fk.getName() == null)
                {
                    fk.setName(getPlatform().getSqlBuilder().getForeignKeyName(table, fk));
                }
            }
        }
        return model;
    }

    /**
     * Returns the original model adjusted for type changes because of the native type mappings
     * which when read back from the database will map to different types.
     * 
     * @return The adjusted model
     */
    protected Database getAdjustedModel()
    {
        return adjustModel(getModel());
    }

    /**
     * Returns the SQL for altering the live database so that it matches the given model.
     * 
     * @param desiredModel The desired model
     * @return The alteration SQL
     */
    protected String getAlterTablesSql(Database desiredModel)
    {
        Database liveModel = readModelFromDatabase(desiredModel.getName());

        return getPlatform().getAlterModelSql(liveModel, desiredModel, getTableCreationParameters(desiredModel));
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
        if (getPlatform().isDelimitedIdentifierModeOn())
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
