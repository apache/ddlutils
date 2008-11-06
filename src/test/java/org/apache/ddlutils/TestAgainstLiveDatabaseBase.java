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
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
import junit.framework.TestSuite;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.dynabean.SqlDynaBean;
import org.apache.ddlutils.dynabean.SqlDynaClass;
import org.apache.ddlutils.dynabean.SqlDynaProperty;
import org.apache.ddlutils.io.BinaryObjectsHelper;
import org.apache.ddlutils.io.DataReader;
import org.apache.ddlutils.io.DataToDatabaseSink;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.CascadeActionEnum;
import org.apache.ddlutils.model.CloneHelper;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.TypeMap;
import org.apache.ddlutils.platform.CreationParameters;
import org.apache.ddlutils.platform.DefaultValueHelper;
import org.apache.ddlutils.platform.firebird.FirebirdPlatform;
import org.apache.ddlutils.platform.interbase.InterbasePlatform;
import org.apache.ddlutils.util.StringUtilsExt;

/**
 * Base class tests that are executed against a live database.
 * 
 * @version $Revision: 289996 $
 */
public abstract class TestAgainstLiveDatabaseBase extends TestPlatformBase
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

    /**
     * Creates the test suite for the given test class which must be a sub class of
     * {@link RoundtripTestBase}. If the platform supports it, it will be tested
     * with both delimited and undelimited identifiers.
     * 
     * @param testedClass The tested class
     * @return The tests
     */
    protected static TestSuite getTests(Class testedClass)
    {
        if (!TestAgainstLiveDatabaseBase.class.isAssignableFrom(testedClass) ||
            Modifier.isAbstract(testedClass.getModifiers()))
        {
            throw new DdlUtilsException("Cannot create parameterized tests for class "+testedClass.getName());
        }

        TestSuite  suite      = new TestSuite();
        Properties props      = readTestProperties();

        if (props == null)
        {
            return suite;
        }

        DataSource dataSource   = initDataSourceFromProperties(props);
        String     databaseName = determineDatabaseName(props, dataSource);

        try
        {
            Method[]               methods = testedClass.getMethods();
            PlatformInfo           info    = null;
            TestAgainstLiveDatabaseBase newTest;
    
            for (int idx = 0; (methods != null) && (idx < methods.length); idx++)
            {
                if (methods[idx].getName().startsWith("test") &&
                    ((methods[idx].getParameterTypes() == null) || (methods[idx].getParameterTypes().length == 0)))
                {
                    newTest = (TestAgainstLiveDatabaseBase)testedClass.newInstance();
                    newTest.setName(methods[idx].getName());
                    newTest.setTestProperties(props);
                    newTest.setDataSource(dataSource);
                    newTest.setDatabaseName(databaseName);
                    newTest.setUseDelimitedIdentifiers(false);
                    suite.addTest(newTest);

                    if (info == null)
                    {
                        info = PlatformFactory.createNewPlatformInstance(newTest.getDatabaseName()).getPlatformInfo();
                    }
                    if (info.isDelimitedIdentifiersSupported())
                    {
                        newTest = (TestAgainstLiveDatabaseBase)testedClass.newInstance();
                        newTest.setName(methods[idx].getName());
                        newTest.setTestProperties(props);
                        newTest.setDataSource(dataSource);
                        newTest.setDatabaseName(databaseName);
                        newTest.setUseDelimitedIdentifiers(true);
                        suite.addTest(newTest);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            throw new DdlUtilsException(ex);
        }
        
        return suite;
    }

    /**
     * Reads the test properties as specified by the property.
     * 
     * @return The properties or <code>null</code> if no properties have been specified
     */
    protected static Properties readTestProperties()
    {
        String propFile = System.getProperty(JDBC_PROPERTIES_PROPERTY);

        if (propFile == null)
        {
            return null;
        }

        InputStream propStream = null;

        try
        {
            propStream = TestAgainstLiveDatabaseBase.class.getResourceAsStream(propFile);

            if (propStream == null)
            {
                propStream = new FileInputStream(propFile);
            }

            Properties props = new Properties();

            props.load(propStream);
            return props;
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
                    LogFactory.getLog(TestAgainstLiveDatabaseBase.class).error("Could not close the stream used to read the test jdbc properties", ex);
                }
            }
        }
    }

    /**
     * Initializes the test datasource and the platform.
     */
    private static DataSource initDataSourceFromProperties(Properties props)
    {
        if (props == null)
        {
            return null;
        }

        try
        {
            String     dataSourceClass = props.getProperty(DATASOURCE_PROPERTY_PREFIX + "class", BasicDataSource.class.getName());
            DataSource dataSource      = (DataSource)Class.forName(dataSourceClass).newInstance();

            for (Iterator it = props.entrySet().iterator(); it.hasNext();)
            {
                Map.Entry entry    = (Map.Entry)it.next();
                String    propName = (String)entry.getKey();

                if (propName.startsWith(DATASOURCE_PROPERTY_PREFIX) && !propName.equals(DATASOURCE_PROPERTY_PREFIX +"class"))
                {
                    BeanUtils.setProperty(dataSource,
                                          propName.substring(DATASOURCE_PROPERTY_PREFIX.length()),
                                          entry.getValue());
                }
            }
            return dataSource;
        }
        catch (Exception ex)
        {
            throw new DatabaseOperationException(ex);
        }
    }

    /**
     * Determines the name of the platform to use from the properties or the data source if no
     * platform is specified in the properties.
     * 
     * @param props      The test properties
     * @param dataSource The data source
     * @return The name of the platform
     */
    private static String determineDatabaseName(Properties props, DataSource dataSource)
    {
        String platformName = props.getProperty(DDLUTILS_PLATFORM_PROPERTY);

        if (platformName == null)
        {
            // property not set, then try to determine
            platformName = new PlatformUtils().determineDatabaseType(dataSource);
            if (platformName == null)
            {
                throw new DatabaseOperationException("Could not determine platform from datasource, please specify it in the jdbc.properties via the ddlutils.platform property");
            }
        }
        return platformName;
    }


    /** The test properties as defined by an external properties file. */
    private Properties _testProps;
    /** The data source to test against. */
    private DataSource _dataSource;
    /** The database name. */
    private String _databaseName;
    /** The database model. */
    private Database _model;
    /** Whether to use delimited identifiers for the test. */
    private boolean _useDelimitedIdentifiers;

    /**
     * Returns the test properties.
     * 
     * @return The properties
     */
    protected Properties getTestProperties()
    {
        return _testProps;
    }

    /**
     * Sets the test properties.
     * 
     * @param props The properties
     */
    private void setTestProperties(Properties props)
    {
        _testProps = props;
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
     * Specifies whether the test shall use delimited identifiers.
     * 
     * @param useDelimitedIdentifiers Whether to use delimited identifiers
     */
    protected void setUseDelimitedIdentifiers(boolean useDelimitedIdentifiers)
    {
        _useDelimitedIdentifiers = useDelimitedIdentifiers;
    }

    /**
     * Determines whether the test shall use delimited identifiers.
     * 
     * @return Whether to use delimited identifiers
     */
    protected boolean isUseDelimitedIdentifiers()
    {
        return _useDelimitedIdentifiers;
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
     * Sets the data source.
     * 
     * @param dataSource The data source
     */
    private void setDataSource(DataSource dataSource)
    {
        _dataSource = dataSource;
    }

    /**
     * {@inheritDoc}
     */
    protected String getDatabaseName()
    {
        return _databaseName;
    }

    /**
     * Sets the database name.
     * 
     * @param databaseName The name of the database
     */
    private void setDatabaseName(String databaseName)
    {
        _databaseName = databaseName;
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
        getPlatform().setDelimitedIdentifierModeOn(_useDelimitedIdentifiers);
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
            dataReader.read(new StringReader(dataXml));
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
     * Inserts a row into the designated table.
     * 
     * @param tableName    The name of the table (case insensitive)
     * @param columnValues The values for the columns in order of definition
     * @return The dyna bean for the row
     */
    protected DynaBean insertRow(String tableName, Object[] columnValues)
    {
        Table    table = getModel().findTable(tableName);
        DynaBean bean  = getModel().createDynaBeanFor(table);

        for (int idx = 0; (idx < table.getColumnCount()) && (idx < columnValues.length); idx++)
        {
            Column column = table.getColumn(idx);

            bean.set(column.getName(), columnValues[idx]);
        }
        getPlatform().insert(getModel(), bean);
        return bean;
    }

    /**
     * Updates the row in the designated table.
     * 
     * @param tableName    The name of the table (case insensitive)
     * @param oldBean      The bean representing the current row
     * @param columnValues The values for the columns in order of definition
     * @returne The dyna bean for the new row
     */
    protected DynaBean updateRow(String tableName, DynaBean oldBean, Object[] columnValues)
    {
        Table    table = getModel().findTable(tableName);
        DynaBean bean  = getModel().createDynaBeanFor(table);

        for (int idx = 0; (idx < table.getColumnCount()) && (idx < columnValues.length); idx++)
        {
            Column column = table.getColumn(idx);

            bean.set(column.getName(), columnValues[idx]);
        }
        getPlatform().update(getModel(), oldBean, bean);
        return bean;
    }

    /**
     * Deletes the specified row from the table.
     * 
     * @param tableName      The name of the table (case insensitive)
     * @param pkColumnValues The values for the pk columns in order of definition
     */
    protected void deleteRow(String tableName, Object[] pkColumnValues)
    {
        Table    table     = getModel().findTable(tableName);
        DynaBean bean      = getModel().createDynaBeanFor(table);
        Column[] pkColumns = table.getPrimaryKeyColumns();

        for (int idx = 0; (idx < pkColumns.length) && (idx < pkColumnValues.length); idx++)
        {
            bean.set(pkColumns[idx].getName(), pkColumnValues[idx]);
        }
        getPlatform().delete(getModel(), bean);
    }

    /**
     * Returns a "SELECT * FROM [table name]" statement. It also takes
     * delimited identifier mode into account if enabled.
     *  
     * @param table       The table
     * @param orderColumn The column to order the rows by (can be <code>null</code>)
     * @return The statement
     */
    protected String getSelectQueryForAllString(Table table, String orderColumn)
    {
        StringBuffer query = new StringBuffer();

        query.append("SELECT * FROM ");
        if (getPlatform().isDelimitedIdentifierModeOn())
        {
            query.append(getPlatformInfo().getDelimiterToken());
        }
        query.append(table.getName());
        if (getPlatform().isDelimitedIdentifierModeOn())
        {
            query.append(getPlatformInfo().getDelimiterToken());
        }
        if (orderColumn != null)
        {
            query.append(" ORDER BY ");
            if (getPlatform().isDelimitedIdentifierModeOn())
            {
                query.append(getPlatformInfo().getDelimiterToken());
            }
            query.append(orderColumn);
            if (getPlatform().isDelimitedIdentifierModeOn())
            {
                query.append(getPlatformInfo().getDelimiterToken());
            }
        }
        return query.toString();
    }

    /**
     * Retrieves all rows from the given table.
     * 
     * @param tableName The table
     * @return The rows
     */
    protected List getRows(String tableName)
    {
        Table table = getModel().findTable(tableName, getPlatform().isDelimitedIdentifierModeOn());
        
        return getPlatform().fetch(getModel(),
                                   getSelectQueryForAllString(table, null),
                                   new Table[] { table });
    }

    /**
     * Retrieves all rows from the given table.
     * 
     * @param tableName   The table
     * @param orderColumn The column to order the rows by
     * @return The rows
     */
    protected List getRows(String tableName, String orderColumn)
    {
        Table table = getModel().findTable(tableName, getPlatform().isDelimitedIdentifierModeOn());
        
        return getPlatform().fetch(getModel(),
                                   getSelectQueryForAllString(table, orderColumn),
                                   new Table[] { table });
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
        Database model = getModel();

        return model == null ? null : adjustModel(model);
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

    /**
     * Asserts that the two given database models are equal, and if not, writes both of them
     * in XML form to <code>stderr</code>.
     * 
     * @param expected The expected model
     * @param actual   The actual model
     */
    protected void assertEquals(Database expected, Database actual)
    {
        assertEquals(expected, actual, _useDelimitedIdentifiers);
    }

    /**
     * Asserts that the two given database tables are equal.
     * 
     * @param expected The expected table
     * @param actual   The actual table
     */
    protected void assertEquals(Table expected, Table actual)
    {
        assertEquals(expected, actual, _useDelimitedIdentifiers);
    }

    /**
     * Asserts that the two given columns are equal.
     * 
     * @param expected The expected column
     * @param actual   The actual column
     */
    protected void assertEquals(Column expected, Column actual)
    {
        assertEquals(expected, actual, _useDelimitedIdentifiers);
    }

    /**
     * Asserts that the two given foreign keys are equal.
     * 
     * @param expected The expected foreign key
     * @param actual   The actual foreign key
     */
    protected void assertEquals(ForeignKey expected, ForeignKey actual)
    {
        assertEquals(expected, actual, _useDelimitedIdentifiers);
    }

    /**
     * Asserts that the two given references are equal.
     * 
     * @param expected The expected reference
     * @param actual   The actual reference
     */
    protected void assertEquals(Reference expected, Reference actual)
    {
        assertEquals(expected, actual, _useDelimitedIdentifiers);
    }

    /**
     * Asserts that the two given indices are equal.
     * 
     * @param expected The expected index
     * @param actual   The actual index
     */
    protected void assertEquals(Index expected, Index actual)
    {
        assertEquals(expected, actual, _useDelimitedIdentifiers);
    }

    /**
     * Asserts that the two given index columns are equal.
     * 
     * @param expected The expected index column
     * @param actual   The actual index column
     */
    protected void assertEquals(IndexColumn expected, IndexColumn actual)
    {
        assertEquals(expected, actual, _useDelimitedIdentifiers);
    }

    /**
     * Compares the specified attribute value of the given bean with the expected object.
     * 
     * @param expected The expected object
     * @param bean     The bean
     * @param attrName The attribute name
     */
    protected void assertEquals(Object expected, Object bean, String attrName)
    {
        DynaBean dynaBean = (DynaBean)bean;
        Object   value    = dynaBean.get(attrName);

        if ((value instanceof byte[]) && !(expected instanceof byte[]) && (dynaBean instanceof SqlDynaBean))
        {
            SqlDynaClass dynaClass = (SqlDynaClass)((SqlDynaBean)dynaBean).getDynaClass();
            Column       column    = ((SqlDynaProperty)dynaClass.getDynaProperty(attrName)).getColumn();

            if (TypeMap.isBinaryType(column.getTypeCode()))
            {
                value = new BinaryObjectsHelper().deserialize((byte[])value);
            }
        }
        if (expected == null)
        {
            assertNull(value);
        }
        else
        {
            assertEquals(expected, value);
        }
    }

    /**
     * Asserts that the two given database models are equal, and if not, writes both of them
     * in XML form to <code>stderr</code>.
     * 
     * @param expected      The expected model
     * @param actual        The actual model
     * @param caseSensitive Whether case matters when comparing
     */
    protected void assertEquals(Database expected, Database actual, boolean caseSensitive)
    {
        try
        {
            assertEquals("Model names do not match.",
                         expected.getName(),
                         actual.getName());
            assertEquals("Not the same number of tables.",
                         expected.getTableCount(),
                         actual.getTableCount());
            for (int tableIdx = 0; tableIdx < actual.getTableCount(); tableIdx++)
            {
                assertEquals(expected.getTable(tableIdx),
                             actual.getTable(tableIdx),
                             caseSensitive);
            }
        }
        catch (Throwable ex)
        {
            StringWriter writer = new StringWriter();
            DatabaseIO   dbIo   = new DatabaseIO();

            dbIo.write(expected, writer);

            getLog().error("Expected model:\n" + writer.toString());

            writer = new StringWriter();
            dbIo.write(actual, writer);

            getLog().error("Actual model:\n" + writer.toString());

            if (ex instanceof Error)
            {
                throw (Error)ex;
            }
            else
            {
                throw new DdlUtilsException(ex);
            }
        }
    }

    /**
     * Asserts that the two given database tables are equal.
     * 
     * @param expected      The expected table
     * @param actual        The actual table
     * @param caseSensitive Whether case matters when comparing
     */
    protected void assertEquals(Table expected, Table actual, boolean caseSensitive)
    {
        if (caseSensitive)
        {
            assertEquals("Table names do not match.",
                         getPlatform().getSqlBuilder().shortenName(expected.getName(), getSqlBuilder().getMaxTableNameLength()),
                         getPlatform().getSqlBuilder().shortenName(actual.getName(), getSqlBuilder().getMaxTableNameLength()));
        }
        else
        {
            assertEquals("Table names do not match (ignoring case).",
                         getPlatform().getSqlBuilder().shortenName(expected.getName().toUpperCase(), getSqlBuilder().getMaxTableNameLength()),
                         getPlatform().getSqlBuilder().shortenName(actual.getName().toUpperCase(), getSqlBuilder().getMaxTableNameLength()));
        }
        assertEquals("Not the same number of columns in table "+actual.getName()+".",
                     expected.getColumnCount(),
                     actual.getColumnCount());
        for (int columnIdx = 0; columnIdx < actual.getColumnCount(); columnIdx++)
        {
            assertEquals(expected.getColumn(columnIdx),
                         actual.getColumn(columnIdx),
                         caseSensitive);
        }
        assertEquals("Not the same number of foreign keys in table "+actual.getName()+".",
                     expected.getForeignKeyCount(),
                     actual.getForeignKeyCount());
        // order is not assumed with the way foreignkeys are returned.
        for (int expectedFkIdx = 0; expectedFkIdx < expected.getForeignKeyCount(); expectedFkIdx++)
        {
            ForeignKey expectedFk   = expected.getForeignKey(expectedFkIdx);
            String     expectedName = getPlatform().getSqlBuilder().shortenName(expectedFk.getName(), getSqlBuilder().getMaxForeignKeyNameLength());

            for (int actualFkIdx = 0; actualFkIdx < actual.getForeignKeyCount(); actualFkIdx++)
            {
                ForeignKey actualFk   = actual.getForeignKey(actualFkIdx);
                String     actualName = getPlatform().getSqlBuilder().shortenName(actualFk.getName(), getSqlBuilder().getMaxForeignKeyNameLength());

                if (StringUtilsExt.equals(expectedName, actualName, caseSensitive))
                {
                    assertEquals(expectedFk,
                                 actualFk,
                                 caseSensitive);
                }
            }
        }
        assertEquals("Not the same number of indices in table "+actual.getName()+".",
                     expected.getIndexCount(),
                     actual.getIndexCount());
        for (int indexIdx = 0; indexIdx < actual.getIndexCount(); indexIdx++)
        {
            assertEquals(expected.getIndex(indexIdx),
                         actual.getIndex(indexIdx),
                         caseSensitive);
        }
    }

    /**
     * Asserts that the two given columns are equal.
     * 
     * @param expected      The expected column
     * @param actual        The actual column
     * @param caseSensitive Whether case matters when comparing
     */
    protected void assertEquals(Column expected, Column actual, boolean caseSensitive)
    {
        if (caseSensitive)
        {
            assertEquals("Column names do not match.",
                         getPlatform().getSqlBuilder().shortenName(expected.getName(), getSqlBuilder().getMaxColumnNameLength()),
                         getPlatform().getSqlBuilder().shortenName(actual.getName(), getSqlBuilder().getMaxColumnNameLength()));
        }
        else
        {
            assertEquals("Column names do not match (ignoring case).",
                         getPlatform().getSqlBuilder().shortenName(expected.getName().toUpperCase(), getSqlBuilder().getMaxColumnNameLength()),
                         getPlatform().getSqlBuilder().shortenName(actual.getName().toUpperCase(), getSqlBuilder().getMaxColumnNameLength()));
        }
        assertEquals("Primary key status not the same for column "+actual.getName()+".",
                     expected.isPrimaryKey(),
                     actual.isPrimaryKey());
        assertEquals("Required status not the same for column "+actual.getName()+".",
                     expected.isRequired(),
                     actual.isRequired());
        if (getPlatformInfo().getIdentityStatusReadingSupported())
        {
            // we're only comparing this if the platform can actually read the
            // auto-increment status back from an existing database
            assertEquals("Auto-increment status not the same for column "+actual.getName()+".",
                         expected.isAutoIncrement(),
                         actual.isAutoIncrement());
        }
        assertEquals("Type not the same for column "+actual.getName()+".",
                     expected.getType(),
                     actual.getType());
        assertEquals("Type code not the same for column "+actual.getName()+".",
                     expected.getTypeCode(),
                     actual.getTypeCode());
        assertEquals("Parsed default values do not match for column "+actual.getName()+".",
                     expected.getParsedDefaultValue(),
                     actual.getParsedDefaultValue());

        // comparing the size makes only sense for types where it is relevant
        if ((expected.getTypeCode() == Types.NUMERIC) ||
            (expected.getTypeCode() == Types.DECIMAL))
        {
            assertEquals("Precision not the same for column "+actual.getName()+".",
                         expected.getSizeAsInt(),
                         actual.getSizeAsInt());
            assertEquals("Scale not the same for column "+actual.getName()+".",
                         expected.getScale(),
                         actual.getScale());
        }
        else if ((expected.getTypeCode() == Types.CHAR) ||
                 (expected.getTypeCode() == Types.VARCHAR) ||
                 (expected.getTypeCode() == Types.BINARY) ||
                 (expected.getTypeCode() == Types.VARBINARY))
        {
            assertEquals("Size not the same for column "+actual.getName()+".",
                         expected.getSize(),
                         actual.getSize());
        }
    }

    /**
     * Asserts that the two given foreign keys are equal.
     * 
     * @param expected      The expected foreign key
     * @param actual        The actual foreign key
     * @param caseSensitive Whether case matters when comparing
     */
    protected void assertEquals(ForeignKey expected, ForeignKey actual, boolean caseSensitive)
    {
        if (caseSensitive)
        {
            assertEquals("Foreign key names do not match.",
                         getPlatform().getSqlBuilder().shortenName(expected.getName(), getSqlBuilder().getMaxForeignKeyNameLength()),
                         getPlatform().getSqlBuilder().shortenName(actual.getName(), getSqlBuilder().getMaxForeignKeyNameLength()));
            assertEquals("Referenced table names do not match.",
                         getPlatform().getSqlBuilder().shortenName(expected.getForeignTableName(), getSqlBuilder().getMaxTableNameLength()),
                         getPlatform().getSqlBuilder().shortenName(actual.getForeignTableName(), getSqlBuilder().getMaxTableNameLength()));
        }
        else
        {
            assertEquals("Foreign key names do not match (ignoring case).",
                         getPlatform().getSqlBuilder().shortenName(expected.getName().toUpperCase(), getSqlBuilder().getMaxForeignKeyNameLength()),
                         getPlatform().getSqlBuilder().shortenName(actual.getName().toUpperCase(), getSqlBuilder().getMaxForeignKeyNameLength()));
            assertEquals("Referenced table names do not match (ignoring case).",
                         getPlatform().getSqlBuilder().shortenName(expected.getForeignTableName().toUpperCase(), getSqlBuilder().getMaxTableNameLength()),
                         getPlatform().getSqlBuilder().shortenName(actual.getForeignTableName().toUpperCase(), getSqlBuilder().getMaxTableNameLength()));
        }

        CascadeActionEnum realExpectedOnUpdateAction = expected.getOnUpdate();

        if (!getPlatformInfo().isActionSupportedForOnUpdate(realExpectedOnUpdateAction))
        {
            realExpectedOnUpdateAction = getPlatformInfo().getDefaultOnUpdateAction();
        }
        assertEquals("Not the same onUpdate setting in foreign key "+actual.getName()+".",
                     realExpectedOnUpdateAction,
                     actual.getOnUpdate());

        CascadeActionEnum realExpectedOnDeleteAction = expected.getOnDelete();

        if (!getPlatformInfo().isActionSupportedForOnDelete(realExpectedOnDeleteAction))
        {
            realExpectedOnDeleteAction = getPlatformInfo().getDefaultOnDeleteAction();
        }
        assertEquals("Not the same onDelete setting in foreign key "+actual.getName()+".",
                     realExpectedOnDeleteAction,
                     actual.getOnDelete());

        assertEquals("Not the same number of references in foreign key "+actual.getName()+".",
                     expected.getReferenceCount(),
                     actual.getReferenceCount());
        for (int refIdx = 0; refIdx < actual.getReferenceCount(); refIdx++)
        {
            assertEquals(expected.getReference(refIdx),
                         actual.getReference(refIdx),
                         caseSensitive);
        }
    }

    /**
     * Asserts that the two given references are equal.
     * 
     * @param expected      The expected reference
     * @param actual        The actual reference
     * @param caseSensitive Whether case matters when comparing
     */
    protected void assertEquals(Reference expected, Reference actual, boolean caseSensitive)
    {
        if (caseSensitive)
        {
            assertEquals("Local column names do not match.",
                         getPlatform().getSqlBuilder().shortenName(expected.getLocalColumnName(), getSqlBuilder().getMaxColumnNameLength()),
                         getPlatform().getSqlBuilder().shortenName(actual.getLocalColumnName(), getSqlBuilder().getMaxColumnNameLength()));
            assertEquals("Foreign column names do not match.",
                         getPlatform().getSqlBuilder().shortenName(expected.getForeignColumnName(), getSqlBuilder().getMaxColumnNameLength()),
                         getPlatform().getSqlBuilder().shortenName(actual.getForeignColumnName(), getSqlBuilder().getMaxColumnNameLength()));
        }
        else
        {
            assertEquals("Local column names do not match (ignoring case).",
                         getPlatform().getSqlBuilder().shortenName(expected.getLocalColumnName().toUpperCase(), getSqlBuilder().getMaxColumnNameLength()),
                         getPlatform().getSqlBuilder().shortenName(actual.getLocalColumnName().toUpperCase(), getSqlBuilder().getMaxColumnNameLength()));
            assertEquals("Foreign column names do not match (ignoring case).",
                         getPlatform().getSqlBuilder().shortenName(expected.getForeignColumnName().toUpperCase(), getSqlBuilder().getMaxColumnNameLength()),
                         getPlatform().getSqlBuilder().shortenName(actual.getForeignColumnName().toUpperCase(), getSqlBuilder().getMaxColumnNameLength()));
        }
    }

    /**
     * Asserts that the two given indices are equal.
     * 
     * @param expected      The expected index
     * @param actual        The actual index
     * @param caseSensitive Whether case matters when comparing
     */
    protected void assertEquals(Index expected, Index actual, boolean caseSensitive)
    {
        if (caseSensitive)
        {
            assertEquals("Index names do not match.",
                         getPlatform().getSqlBuilder().shortenName(expected.getName(), getSqlBuilder().getMaxConstraintNameLength()),
                         getPlatform().getSqlBuilder().shortenName(actual.getName(), getSqlBuilder().getMaxConstraintNameLength()));
        }
        else
        {
            assertEquals("Index names do not match (ignoring case).",
                         getPlatform().getSqlBuilder().shortenName(expected.getName().toUpperCase(), getSqlBuilder().getMaxConstraintNameLength()),
                         getPlatform().getSqlBuilder().shortenName(actual.getName().toUpperCase(), getSqlBuilder().getMaxConstraintNameLength()));
        }
        assertEquals("Unique status not the same for index "+actual.getName()+".",
                     expected.isUnique(),
                     actual.isUnique());
        assertEquals("Not the same number of columns in index "+actual.getName()+".",
                     expected.getColumnCount(),
                     actual.getColumnCount());
        for (int columnIdx = 0; columnIdx < actual.getColumnCount(); columnIdx++)
        {
            assertEquals(expected.getColumn(columnIdx),
                         actual.getColumn(columnIdx),
                         caseSensitive);
        }
    }

    /**
     * Asserts that the two given index columns are equal.
     * 
     * @param expected      The expected index column
     * @param actual        The actual index column
     * @param caseSensitive Whether case matters when comparing
     */
    protected void assertEquals(IndexColumn expected, IndexColumn actual, boolean caseSensitive)
    {
        if (caseSensitive)
        {
            assertEquals("Index column names do not match.",
                         getPlatform().getSqlBuilder().shortenName(expected.getName(), getSqlBuilder().getMaxColumnNameLength()),
                         getPlatform().getSqlBuilder().shortenName(actual.getName(), getSqlBuilder().getMaxColumnNameLength()));
        }
        else
        {
            assertEquals("Index column names do not match (ignoring case).",
                         getPlatform().getSqlBuilder().shortenName(expected.getName().toUpperCase(), getSqlBuilder().getMaxColumnNameLength()),
                         getPlatform().getSqlBuilder().shortenName(actual.getName().toUpperCase(), getSqlBuilder().getMaxColumnNameLength()));
        }
        assertEquals("Size not the same for index column "+actual.getName()+".",
                     expected.getSize(),
                     actual.getSize());
    }
}
