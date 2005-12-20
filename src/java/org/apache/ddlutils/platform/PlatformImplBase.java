package org.apache.ddlutils.platform;

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

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.DynaSqlException;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.dynabean.DynaSqlIterator;
import org.apache.ddlutils.dynabean.SqlDynaClass;
import org.apache.ddlutils.dynabean.SqlDynaProperty;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.util.JdbcSupport;

/**
 * Base class for platform implementations.
 * 
 * @author James Strachan
 * @author Thomas Dudziak
 * @version $Revision: 231110 $
 */
public abstract class PlatformImplBase extends JdbcSupport implements Platform
{
    /** The log for this platform. */
    private final Log _log = LogFactory.getLog(getClass());

    /** The sql builder for this platform. */
    private SqlBuilder _builder;
    /** The model reader for this platform. */
    private JdbcModelReader _modelReader = new JdbcModelReader();

    /**
     * {@inheritDoc}
     */
    public SqlBuilder getSqlBuilder()
    {
        return _builder;
    }

    /**
     * Sets the sql builder for this platform.
     * 
     * @param builder The sql builder
     */
    protected void setSqlBuilder(SqlBuilder builder)
    {
        _builder = builder;
    }

    /**
     * {@inheritDoc}
     */
    public JdbcModelReader getModelReader()
    {
        return _modelReader;
    }

    /**
     * Sets the model reader for this platform.
     * 
     * @param modelReader The model reader
     */
    protected void setModelReader(JdbcModelReader modelReader)
    {
        _modelReader = modelReader;
    }

    /**
     * {@inheritDoc}
     */
    public PlatformInfo getPlatformInfo()
    {
        return _builder.getPlatformInfo();
    }

    /**
     * Returns the log for this platform.
     * 
     * @return The log
     */
    protected Log getLog()
    {
        return _log;
    }

    /**
     * Logs any warnings associated to the given connection. Note that the connection needs
     * to be open for this.
     * 
     * @param connection The open connection
     */
    protected void logWarnings(Connection connection) throws SQLException
    {
        SQLWarning warning = connection.getWarnings();

        while (warning != null)
        {
            getLog().warn(warning.getLocalizedMessage(), warning.getCause());
            warning = warning.getNextWarning();
        }
    }

    /**
     * {@inheritDoc}
     */
    public int evaluateBatch(String sql, boolean continueOnError) throws DynaSqlException
    {
        Connection connection = borrowConnection();

        try
        {
            return evaluateBatch(connection, sql, continueOnError);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int evaluateBatch(Connection connection, String sql, boolean continueOnError) throws DynaSqlException
    {
        Statement statement    = null;
        int       errors       = 0;
        int       commandCount = 0;

        try
        {
            statement = connection.createStatement();
            
            StringTokenizer tokenizer = new StringTokenizer(sql, ";");

            while (tokenizer.hasMoreTokens())
            {
                String command = tokenizer.nextToken();
                
                // ignore whitespace
                command = command.trim();
                if (command.length() == 0)
                {
                    continue;
                }
                
                commandCount++;
                
                if (_log.isDebugEnabled())
                {
                    _log.debug("About to execute SQL " + command);
                }
                try
                {
                    int results = statement.executeUpdate(command);

                    if (_log.isDebugEnabled())
                    {
                        _log.debug("After execution, " + results + " row(s) have been changed");
                    }
                }
                catch (SQLException ex)
                {
                    if (continueOnError)
                    {
                        System.err.println("SQL Command " + command + " failed with " + ex.getMessage());
                        errors++;
                    }
                    else
                    {
                        throw new DynaSqlException("Error while executing SQL "+command, ex);
                    }
                }

                // lets display any warnings
                SQLWarning warning = connection.getWarnings();

                while (warning != null)
                {
                    _log.warn(warning.toString());
                    warning = warning.getNextWarning();
                }
                connection.clearWarnings();
            }
            _log.info("Executed "+ commandCount + " SQL command(s) with " + errors + " error(s)");
        }
        catch (SQLException ex)
        {
            throw new DynaSqlException("Error while executing SQL", ex);
        }
        finally
        {
            closeStatement(statement);
        }

        return errors;
    }

    /**
     * {@inheritDoc}
     */
    public void createDatabase(String jdbcDriverClassName, String connectionUrl, String username, String password, Map parameters) throws DynaSqlException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Database creation is not supported for the database platform "+getName());
    }

    /**
     * {@inheritDoc}
     */
    public void dropDatabase(String jdbcDriverClassName, String connectionUrl, String username, String password) throws DynaSqlException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Database deletion is not supported for the database platform "+getName());
    }

    /**
     * {@inheritDoc}
     */
    public void createTables(Database model, boolean dropTablesFirst, boolean continueOnError) throws DynaSqlException
    {
        Connection connection = borrowConnection();

        try
        {
            createTables(connection, model, dropTablesFirst, continueOnError);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createTables(Connection connection, Database model, boolean dropTablesFirst, boolean continueOnError) throws DynaSqlException
    {
        String sql = getCreateTablesSql(model, dropTablesFirst, continueOnError);

        evaluateBatch(connection, sql, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public String getCreateTablesSql(Database model, boolean dropTablesFirst, boolean continueOnError) throws DynaSqlException
    {
        String sql = null;

        try
        {
            StringWriter buffer = new StringWriter();

            getSqlBuilder().setWriter(buffer);
            getSqlBuilder().createTables(model, dropTablesFirst);
            sql = buffer.toString();
        }
        catch (IOException e)
        {
            // won't happen because we're using a string writer
        }
        return sql;
    }

    /**
     * {@inheritDoc}
     */
    public void createTables(Database model, CreationParameters params, boolean dropTablesFirst, boolean continueOnError) throws DynaSqlException
    {
        Connection connection = borrowConnection();

        try
        {
            createTables(connection, model, params, dropTablesFirst, continueOnError);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createTables(Connection connection, Database model, CreationParameters params, boolean dropTablesFirst, boolean continueOnError) throws DynaSqlException
    {
        String sql = getCreateTablesSql(model, params, dropTablesFirst, continueOnError);

        evaluateBatch(connection, sql, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public String getCreateTablesSql(Database model, CreationParameters params, boolean dropTablesFirst, boolean continueOnError) throws DynaSqlException
    {
        String sql = null;

        try
        {
            StringWriter buffer = new StringWriter();

            getSqlBuilder().setWriter(buffer);
            getSqlBuilder().createTables(model, params, dropTablesFirst);
            sql = buffer.toString();
        }
        catch (IOException e)
        {
            // won't happen because we're using a string writer
        }
        return sql;
    }

    /**
     * {@inheritDoc}
     */
    public void alterTables(Database desiredDb, boolean continueOnError) throws DynaSqlException
    {
        alterTables(desiredDb, false, false, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public void alterTables(Database desiredDb, boolean doDrops, boolean modifyColumns, boolean continueOnError) throws DynaSqlException
    {
        Connection connection = borrowConnection();

        try
        {
            alterTables(connection, desiredDb, doDrops, modifyColumns, continueOnError);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getAlterTablesSql(Database desiredDb, boolean doDrops, boolean modifyColumns, boolean continueOnError) throws DynaSqlException
    {
        Connection connection = borrowConnection();

        try
        {
            return getAlterTablesSql(connection, desiredDb, doDrops, modifyColumns, continueOnError);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void alterTables(Database desiredDb, CreationParameters params, boolean continueOnError) throws DynaSqlException
    {
        alterTables(desiredDb, params, false, false, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public void alterTables(Database desiredDb, CreationParameters params, boolean doDrops, boolean modifyColumns, boolean continueOnError) throws DynaSqlException
    {
        Connection connection = borrowConnection();

        try
        {
            alterTables(connection, desiredDb, params, doDrops, modifyColumns, continueOnError);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getAlterTablesSql(Database desiredDb, CreationParameters params, boolean doDrops, boolean modifyColumns, boolean continueOnError) throws DynaSqlException
    {
        Connection connection = borrowConnection();

        try
        {
            return getAlterTablesSql(connection, desiredDb, params, doDrops, modifyColumns, continueOnError);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void alterTables(Connection connection, Database desiredDb, boolean continueOnError) throws DynaSqlException
    {
        alterTables(connection, desiredDb, false, false, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public void alterTables(Connection connection, Database desiredModel, boolean doDrops, boolean modifyColumns, boolean continueOnError) throws DynaSqlException
    {
        String sql = getAlterTablesSql(connection, desiredModel, doDrops, modifyColumns, continueOnError);

        evaluateBatch(connection, sql, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public String getAlterTablesSql(Connection connection, Database desiredModel, boolean doDrops, boolean modifyColumns, boolean continueOnError) throws DynaSqlException
    {
        String   sql          = null;
        Database currentModel = readModelFromDatabase(connection);

        try
        {
            StringWriter buffer = new StringWriter();

            getSqlBuilder().setWriter(buffer);
            getSqlBuilder().alterDatabase(currentModel, desiredModel, doDrops, modifyColumns);
            sql = buffer.toString();
        }
        catch (IOException ex)
        {
            // won't happen because we're using a string writer
        }
        return sql;
    }

    /**
     * {@inheritDoc}
     */
    public void alterTables(Connection connection, Database desiredDb, CreationParameters params, boolean continueOnError) throws DynaSqlException
    {
        alterTables(connection, desiredDb, params, false, false, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public void alterTables(Connection connection, Database desiredModel, CreationParameters params, boolean doDrops, boolean modifyColumns, boolean continueOnError) throws DynaSqlException
    {
        String sql = getAlterTablesSql(connection, desiredModel, params, doDrops, modifyColumns, continueOnError);

        evaluateBatch(connection, sql, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public String getAlterTablesSql(Connection connection, Database desiredModel, CreationParameters params, boolean doDrops, boolean modifyColumns, boolean continueOnError) throws DynaSqlException
    {
        String   sql          = null;
        Database currentModel = readModelFromDatabase(connection);

        try
        {
            StringWriter buffer = new StringWriter();

            getSqlBuilder().setWriter(buffer);
            getSqlBuilder().alterDatabase(currentModel, desiredModel, params, doDrops, modifyColumns);
            sql = buffer.toString();
        }
        catch (IOException ex)
        {
            // won't happen because we're using a string writer
        }
        return sql;
    }

    /**
     * {@inheritDoc}
     */
    public void dropTables(Database model, boolean continueOnError) throws DynaSqlException
    {
        Connection connection = borrowConnection();

        try
        {
            dropTables(connection, model, continueOnError);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void dropTables(Connection connection, Database model, boolean continueOnError) throws DynaSqlException 
    {
        String sql = getDropTablesSql(model, continueOnError);

        evaluateBatch(connection, sql, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public String getDropTablesSql(Database model, boolean continueOnError) throws DynaSqlException 
    {
        String sql = null;

        try
        {
            StringWriter buffer = new StringWriter();

            getSqlBuilder().setWriter(buffer);
            getSqlBuilder().dropTables(model);
            sql = buffer.toString();
        }
        catch (IOException e)
        {
            // won't happen because we're using a string writer
        }
        return sql;
    }

    /**
     * {@inheritDoc}
     */
    public Iterator query(Database model, String sql) throws DynaSqlException
    {
        return query(model, sql, (Table[])null);
    }

    /**
     * {@inheritDoc}
     */
    public Iterator query(Database model, String sql, Collection parameters) throws DynaSqlException
    {
        return query(model, sql, parameters, null);
    }

    /**
     * {@inheritDoc}
     */
    public Iterator query(Database model, String sql, Table[] queryHints) throws DynaSqlException
    {
        Connection connection = borrowConnection();
        Statement  statement  = null;
        ResultSet  resultSet  = null;
        Iterator   answer     = null;

        try
        {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            answer    = createResultSetIterator(model, resultSet, queryHints);
            return answer;
        }
        catch (SQLException ex)
        {
            throw new DynaSqlException("Error while performing a query", ex);
        }
        finally
        {
            // if any exceptions are thrown, close things down
            // otherwise we're leaving it open for the iterator
            if (answer == null)
            {
                closeStatement(statement);
                returnConnection(connection);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Iterator query(Database model, String sql, Collection parameters, Table[] queryHints) throws DynaSqlException
    {
        Connection        connection = borrowConnection();
        PreparedStatement statement  = null;
        ResultSet         resultSet  = null;
        Iterator          answer     = null;

        try
        {
            statement = connection.prepareStatement(sql);

            int paramIdx = 1;

            for (Iterator iter = parameters.iterator(); iter.hasNext(); paramIdx++)
            {
                statement.setObject(paramIdx, iter.next());
            }
            resultSet = statement.executeQuery();
            answer    = createResultSetIterator(model, resultSet, queryHints);
            return answer;
        }
        catch (SQLException ex)
        {
            throw new DynaSqlException("Error while performing a query", ex);
        }
        finally
        {
            // if any exceptions are thrown, close things down
            // otherwise we're leaving it open for the iterator
            if (answer == null)
            {
                closeStatement(statement);
                returnConnection(connection);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public List fetch(Database model, String sql) throws DynaSqlException
    {
        return fetch(model, sql, (Table[])null, 0, -1);
    }

    /**
     * {@inheritDoc}
     */
    public List fetch(Database model, String sql, Table[] queryHints) throws DynaSqlException
    {
        return fetch(model, sql, queryHints, 0, -1);
    }

    /**
     * {@inheritDoc}
     */
    public List fetch(Database model, String sql, int start, int end) throws DynaSqlException
    {
        return fetch(model, sql, (Table[])null, start, end);
    }

    /**
     * {@inheritDoc}
     */
    public List fetch(Database model, String sql, Table[] queryHints, int start, int end) throws DynaSqlException
    {
        Connection connection = borrowConnection();
        Statement  statement  = null;
        ResultSet  resultSet  = null;
        List       result     = new ArrayList();

        try
        {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);

            int rowIdx = 0;

            for (Iterator it = createResultSetIterator(model, resultSet, queryHints); ((end < 0) || (rowIdx <= end)) && it.hasNext(); rowIdx++)
            {
                if (rowIdx >= start)
                {
                    result.add(it.next());
                }
            }
        }
        catch (SQLException ex)
        {
            // any other exception comes from the iterator which closes the resources automatically
            closeStatement(statement);
            returnConnection(connection);
            throw new DynaSqlException("Error while fetching data from the database", ex);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public List fetch(Database model, String sql, Collection parameters) throws DynaSqlException
    {
        return fetch(model, sql, parameters, null, 0, -1);
    }

    /**
     * {@inheritDoc}
     */
    public List fetch(Database model, String sql, Collection parameters, int start, int end) throws DynaSqlException
    {
        return fetch(model, sql, parameters, null, start, end);
    }

    /**
     * {@inheritDoc}
     */
    public List fetch(Database model, String sql, Collection parameters, Table[] queryHints) throws DynaSqlException
    {
        return fetch(model, sql, parameters, queryHints, 0, -1);
    }

    /**
     * {@inheritDoc}
     */
    public List fetch(Database model, String sql, Collection parameters, Table[] queryHints, int start, int end) throws DynaSqlException
    {
        Connection        connection = borrowConnection();
        PreparedStatement statement  = null;
        ResultSet         resultSet  = null;
        List              result     = new ArrayList();

        try
        {
            statement = connection.prepareStatement(sql);

            int paramIdx = 1;

            for (Iterator iter = parameters.iterator(); iter.hasNext(); paramIdx++)
            {
                statement.setObject(paramIdx, iter.next());
            }
            resultSet = statement.executeQuery();

            int rowIdx = 0;

            for (Iterator it = createResultSetIterator(model, resultSet, queryHints); ((end < 0) || (rowIdx <= end)) && it.hasNext(); rowIdx++)
            {
                if (rowIdx >= start)
                {
                    result.add(it.next());
                }
            }
        }
        catch (SQLException ex)
        {
            // any other exception comes from the iterator which closes the resources automatically
            closeStatement(statement);
            returnConnection(connection);
            throw new DynaSqlException("Error while fetching data from the database", ex);
        }
        return result;
    }

    /**
     * Creates the SQL for inserting an object of the given type. If a concrete bean is given,
     * then a concrete insert statement is created, otherwise an insert statement usable in a
     * prepared statement is build. 
     *
     * @param model      The database model
     * @param dynaClass  The type
     * @param properties The properties to write
     * @param bean       Optionally the concrete bean to insert
     * @return The SQL required to insert an instance of the class
     */
    protected String createInsertSql(Database model, SqlDynaClass dynaClass, SqlDynaProperty[] properties, DynaBean bean)
    {
        Table   table        = model.findTable(dynaClass.getTableName());
        HashMap columnValues = toColumnValues(properties, bean);

        return _builder.getInsertSql(table, columnValues, bean == null);
    }

    /**
     * Creates the SQL for querying for the id generated by the last insert of an object of the given type.
     * 
     * @param model     The database model
     * @param dynaClass The type
     * @return The SQL required for querying for the id, or <code>null</code> if the database does not
     *         support this
     */
    protected String createSelectLastInsertIdSql(Database model, SqlDynaClass dynaClass)
    {
        Table table = model.findTable(dynaClass.getTableName());

        return _builder.getSelectLastInsertId(table);
    }

    /**
     * {@inheritDoc}
     */
    public String getInsertSql(Database model, DynaBean dynaBean)
    {
        SqlDynaClass      dynaClass  = model.getDynaClassFor(dynaBean);
        SqlDynaProperty[] properties = dynaClass.getSqlDynaProperties();

        if (properties.length == 0)
        {
            _log.info("Cannot insert instances of type " + dynaClass + " because it has no properties");
            return null;
        }

        return createInsertSql(model, dynaClass, properties, dynaBean);
    }

    /**
     * Returns all properties where the column is not non-autoincrement and for which the bean
     * either has a value or the column hasn't got a default value, for the given dyna class.
     * 
     * @param model     The database model
     * @param dynaClass The dyna class
     * @param bean      The bean
     * @return The properties
     */
    private SqlDynaProperty[] getPropertiesForInsertion(Database model, SqlDynaClass dynaClass, final DynaBean bean)
    {
        SqlDynaProperty[] properties = dynaClass.getSqlDynaProperties();

        Collection result = CollectionUtils.select(Arrays.asList(properties), new Predicate() {
            public boolean evaluate(Object input) {
                SqlDynaProperty prop = (SqlDynaProperty)input;

                return !prop.getColumn().isAutoIncrement() &&
                       ((bean.get(prop.getName()) != null) || (prop.getColumn().getDefaultValue() == null));
            }
        });

        return (SqlDynaProperty[])result.toArray(new SqlDynaProperty[result.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public void insert(Connection connection, Database model, DynaBean dynaBean) throws DynaSqlException
    {
        SqlDynaClass      dynaClass       = model.getDynaClassFor(dynaBean);
        SqlDynaProperty[] properties      = getPropertiesForInsertion(model, dynaClass, dynaBean);
        Column[]          autoIncrColumns = model.findTable(dynaClass.getTableName()).getAutoIncrementColumns();

        if ((properties.length == 0) && (autoIncrColumns.length == 0))
        {
            _log.warn("Cannot insert instances of type " + dynaClass + " because it has no usable properties");
            return;
        }

        String            insertSql  = createInsertSql(model, dynaClass, properties, null);
        String            queryIdSql = autoIncrColumns.length > 0 ? createSelectLastInsertIdSql(model, dynaClass) : null;
        PreparedStatement statement  = null;

        if (_log.isDebugEnabled())
        {
            _log.debug("About to execute SQL: " + insertSql);
        }
        if ((autoIncrColumns.length > 0) && (queryIdSql == null))
        {
            _log.warn("The database does not support querying for auto-generated pk values");
        }

        try
        {
            statement = connection.prepareStatement(insertSql);

            for (int idx = 0; idx < properties.length; idx++ )
            {
                setObject(statement, idx + 1, dynaBean, properties[idx]);
            }

            int count = statement.executeUpdate();

            if (count != 1)
            {
                _log.warn("Attempted to insert a single row " + dynaBean +
                         " in table " + dynaClass.getTableName() +
                         " but changed " + count + " row(s)");
            }
        }
        catch (SQLException ex)
        {
            throw new DynaSqlException("Error while inserting into the database", ex);
        }
        finally
        {
            closeStatement(statement);
        }
        if (queryIdSql != null)
        {
            Statement queryStmt       = null;
            ResultSet lastInsertedIds = null;

            try
            {
                // we'll have to commit the statement(s) because otherwise most likely
                // the auto increment hasn't happened yet (the db didn't actually
                // perform the insert yet so no triggering of sequences did occur)
                if (!connection.getAutoCommit())
                {
                    connection.commit();
                }

                queryStmt       = connection.createStatement();
                lastInsertedIds = queryStmt.executeQuery(queryIdSql);

                lastInsertedIds.next();

                for (int idx = 0; idx < autoIncrColumns.length; idx++)
                {
                    Object value = lastInsertedIds.getObject(autoIncrColumns[idx].getName());
    
                    PropertyUtils.setProperty(dynaBean, autoIncrColumns[idx].getName(), value);
                }
            }
            catch (NoSuchMethodException ex)
            {
                // Can't happen because we're using dyna beans
            }
            catch (IllegalAccessException ex)
            {
                // Can't happen because we're using dyna beans
            }
            catch (InvocationTargetException ex)
            {
                // Can't happen because we're using dyna beans
            }
            catch (SQLException ex)
            {
                throw new DynaSqlException("Error while retrieving the auto-generated primary key from the database", ex);
            }
            finally
            {
                if (lastInsertedIds != null)
                {
                    try
                    {
                        lastInsertedIds.close();
                    }
                    catch (SQLException ex)
                    {
                        // we ignore this one
                    }
                }
                closeStatement(statement);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void insert(Database model, DynaBean dynaBean) throws DynaSqlException
    {
        Connection connection = borrowConnection();

        try
        {
            insert(connection, model, dynaBean);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void insert(Connection connection, Database model, Collection dynaBeans) throws DynaSqlException
    {
        SqlDynaClass      dynaClass  = null;
        SqlDynaProperty[] properties = null;
        PreparedStatement statement  = null;
        int               addedStmts = 0;

        for (Iterator it = dynaBeans.iterator(); it.hasNext();)
        {
            DynaBean     dynaBean     = (DynaBean)it.next();
            SqlDynaClass curDynaClass = model.getDynaClassFor(dynaBean);

            if (curDynaClass != dynaClass)
            {
                if (dynaClass != null)
                {
                    executeBatch(statement, addedStmts, dynaClass.getTableName());
                    addedStmts = 0;
                }

                dynaClass  = curDynaClass;
                properties = getPropertiesForInsertion(model, curDynaClass, dynaBean);
    
                if (properties.length == 0)
                {
                    _log.warn("Cannot insert instances of type " + dynaClass + " because it has no usable properties");
                    continue;
                }

                String insertSql = createInsertSql(model, dynaClass, properties, null);

                if (_log.isDebugEnabled())
                {
                    _log.debug("Starting new batch with SQL: " + insertSql);
                }
                try
                {
                    statement = connection.prepareStatement(insertSql);
                }
                catch (SQLException ex)
                {
                    throw new DynaSqlException("Error while preparing insert statement", ex);
                }
            }
            try
            {
                for (int idx = 0; idx < properties.length; idx++ )
                {
                    setObject(statement, idx + 1, dynaBean, properties[idx]);
                }
                statement.addBatch();
                addedStmts++;
            }
            catch (SQLException ex)
            {
                throw new DynaSqlException("Error while adding batch insert", ex);
            }
        }
        if (dynaClass != null)
        {
            executeBatch(statement, addedStmts, dynaClass.getTableName());
        }
    }

    /**
     * Performs the batch for the given statement, and checks that the specified amount of rows have been changed.
     * 
     * @param statement The prepared statement
     * @param numRows   The number of rows that should change
     * @param tableName The name of the changed table
     */
    private void executeBatch(PreparedStatement statement, int numRows, String tableName) throws DynaSqlException
    {
        if (statement != null)
        {
            try
            {
                int[] results = statement.executeBatch();

                closeStatement(statement);

                int sum = 0;

                for (int idx = 0; (results != null) && (idx < results.length); idx++)
                {
                    sum += results[idx];
                }
                if (sum != numRows)
                {
                    _log.warn("Attempted to insert " + numRows + " rows into table " + tableName + " but changed " + sum + " rows");
                }
            }
            catch (SQLException ex)
            {
                throw new DynaSqlException("Error while inserting into the database", ex);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void insert(Database model, Collection dynaBeans) throws DynaSqlException
    {
        Connection connection = borrowConnection();

        try
        {
            insert(connection, model, dynaBeans);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * Creates the SQL for updating an object of the given type. If a concrete bean is given,
     * then a concrete update statement is created, otherwise an update statement usable in a
     * prepared statement is build.
     * 
     * @param model       The database model
     * @param dynaClass   The type
     * @param primaryKeys The primary keys
     * @param properties  The properties to write
     * @param bean        Optionally the concrete bean to update
     * @return The SQL required to update the instance
     */
    protected String createUpdateSql(Database model, SqlDynaClass dynaClass, SqlDynaProperty[] primaryKeys, SqlDynaProperty[] properties, DynaBean bean)
    {
        Table   table        = model.findTable(dynaClass.getTableName());
        HashMap columnValues = toColumnValues(properties, bean);

        return _builder.getUpdateSql(table, columnValues, bean == null);
    }

    /**
     * {@inheritDoc}
     */
    public String getUpdateSql(Database model, DynaBean dynaBean)
    {
        SqlDynaClass      dynaClass   = model.getDynaClassFor(dynaBean);
        SqlDynaProperty[] primaryKeys = dynaClass.getPrimaryKeyProperties();

        if (primaryKeys.length == 0)
        {
            _log.info("Cannot update instances of type " + dynaClass + " because it has no primary keys");
            return null;
        }

        return createUpdateSql(model, dynaClass, primaryKeys, dynaClass.getNonPrimaryKeyProperties(), dynaBean);
    }

    /**
     * {@inheritDoc}
     */
    public void update(Connection connection, Database model, DynaBean dynaBean) throws DynaSqlException
    {
        SqlDynaClass      dynaClass   = model.getDynaClassFor(dynaBean);
        SqlDynaProperty[] primaryKeys = dynaClass.getPrimaryKeyProperties();

        if (primaryKeys.length == 0)
        {
            _log.info("Cannot update instances of type " + dynaClass + " because it has no primary keys");
            return;
        }

        SqlDynaProperty[] properties = dynaClass.getNonPrimaryKeyProperties();
        String            sql        = createUpdateSql(model, dynaClass, primaryKeys, properties, null);
        PreparedStatement statement  = null;

        if (_log.isDebugEnabled())
        {
            _log.debug("About to execute SQL: " + sql);
        }
        try
        {
            statement = connection.prepareStatement(sql);

            int sqlIndex = 1;

            for (int idx = 0; idx < properties.length; idx++)
            {
                setObject(statement, sqlIndex++, dynaBean, properties[idx]);
            }
            for (int idx = 0; idx < properties.length; idx++)
            {
                setObject(statement, sqlIndex++, dynaBean, primaryKeys[idx]);
            }

            int count = statement.executeUpdate();

            if (count != 1)
            {
                _log.warn("Attempted to insert a single row " + dynaBean +
                         " into table " + dynaClass.getTableName() +
                         " but changed " + count + " row(s)");
            }
        }
        catch (SQLException ex)
        {
            throw new DynaSqlException("Error while updating in the database", ex);
        }
        finally
        {
            closeStatement(statement);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void update(Database model, DynaBean dynaBean) throws DynaSqlException
    {
        Connection connection = borrowConnection();

        try
        {
            update(connection, model, dynaBean);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * Determines whether the given dyna bean is stored in the database.
     * 
     * @param dynaBean   The bean
     * @param connection The connection
     * @return <code>true</code> if this dyna bean has a primary key
     */
    protected boolean exists(Connection connection, DynaBean dynaBean)
    {
        // TODO: check for the pk value, and if present, query against database
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void store(Database model, DynaBean dynaBean) throws DynaSqlException
    {
        Connection connection = borrowConnection();

        try
        {
            if (exists(connection, dynaBean))
            {
                update(connection, model, dynaBean);
            }
            else
            {
                insert(connection, model, dynaBean);
            }
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * Creates the SQL for deleting an object of the given type. If a concrete bean is given,
     * then a concrete delete statement is created, otherwise a delete statement usable in a
     * prepared statement is build.
     * 
     * @param model       The database model
     * @param dynaClass   The type
     * @param primaryKeys The primary keys
     * @param bean        Optionally the concrete bean to update
     * @return The SQL required to delete the instance
     */
    protected String createDeleteSql(Database model, SqlDynaClass dynaClass, SqlDynaProperty[] primaryKeys, DynaBean bean)
    {
        Table   table    = model.findTable(dynaClass.getTableName());
        HashMap pkValues = toColumnValues(primaryKeys, bean);

        return _builder.getDeleteSql(table, pkValues, bean == null);
    }

    /**
     * {@inheritDoc}
     */
    public String getDeleteSql(Database model, DynaBean dynaBean)
    {
        SqlDynaClass      dynaClass   = model.getDynaClassFor(dynaBean);
        SqlDynaProperty[] primaryKeys = dynaClass.getPrimaryKeyProperties();

        if (primaryKeys.length == 0)
        {
            _log.warn("Cannot delete instances of type " + dynaClass + " because it has no primary keys");
            return null;
        }
        else
        {
            return createDeleteSql(model, dynaClass, primaryKeys, dynaBean);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void delete(Database model, DynaBean dynaBean) throws DynaSqlException
    {
        Connection connection = borrowConnection();

        try
        {
            delete(connection, model, dynaBean);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void delete(Connection connection, Database model, DynaBean dynaBean) throws DynaSqlException
    {
        PreparedStatement statement  = null;

        try
        {
            SqlDynaClass      dynaClass   = model.getDynaClassFor(dynaBean);
            SqlDynaProperty[] primaryKeys = dynaClass.getPrimaryKeyProperties();

            if (primaryKeys.length == 0)
            {
                _log.warn("Cannot delete instances of type " + dynaClass + " because it has no primary keys");
                return;
            }

            String sql = createDeleteSql(model, dynaClass, primaryKeys, null);

            if (_log.isDebugEnabled())
            {
                _log.debug("About to execute SQL " + sql);
            }

            statement = connection.prepareStatement(sql);

            for (int idx = 0; idx < primaryKeys.length; idx++)
            {
                setObject(statement, idx + 1, dynaBean, primaryKeys[idx]);
            }

            int count = statement.executeUpdate();

            if (count != 1)
            {
                _log.warn("Attempted to delete a single row " + dynaBean +
                          " in table " + dynaClass.getTableName() +
                          " but changed " + count + " row(s).");
            }
        }
        catch (SQLException ex)
        {
            throw new DynaSqlException("Error while deleting from the database", ex);
        }
        finally
        {
            closeStatement(statement);
        }
    }

    /**
     * {@inheritDoc}
     */    
    public Database readModelFromDatabase() throws DynaSqlException
    {
        return readModelFromDatabase(borrowConnection());
    }

    /**
     * {@inheritDoc}
     */    
    public Database readModelFromDatabase(Connection connection) throws DynaSqlException
    {
        try
        {
            return getModelReader().getDatabase(connection);
        }
        catch (SQLException ex)
        {
            throw new DynaSqlException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Database readModelFromDatabase(String catalog, String schema, String[] tableTypes) throws DynaSqlException
    {
        return readModelFromDatabase(borrowConnection(), catalog, schema, tableTypes);
    }

    /**
     * {@inheritDoc}
     */
    public Database readModelFromDatabase(Connection connection, String catalog, String schema, String[] tableTypes) throws DynaSqlException
    {
        try
        {
            JdbcModelReader reader = getModelReader();
            
            return reader.getDatabase(connection, catalog, schema, tableTypes);
        }
        catch (SQLException ex)
        {
            throw new DynaSqlException(ex);
        }
    }

    /**
     * Derives the column values for the given dyna properties from the dyna bean.
     * 
     * @param properties The properties
     * @param bean       The bean
     * @return The values indexed by the column names
     */
    protected HashMap toColumnValues(SqlDynaProperty[] properties, DynaBean bean)
    {
        HashMap result = new HashMap();

        for (int idx = 0; idx < properties.length; idx++)
        {
            result.put(properties[idx].getName(),
                       bean == null ? null : bean.get(properties[idx].getName()));
        }
        return result;
    }

    /**
     * Sets a parameter of the prepared statement based on the type of the column of the property.
     * 
     * @param statement The statement
     * @param sqlIndex  The index of the parameter to set in the statement
     * @param dynaBean  The bean of which to take the value
     * @param property  The property of the bean, which also defines the corresponding column
     */
    protected void setObject(PreparedStatement statement, int sqlIndex, DynaBean dynaBean, SqlDynaProperty property) throws SQLException
    {
        int     typeCode = property.getColumn().getTypeCode();
        Object  value    = dynaBean.get(property.getName());

        if (value == null)
        {
            statement.setNull(sqlIndex, typeCode);
        }
        else if (value instanceof String)
        {
            statement.setString(sqlIndex, (String)value);
        }
        else
        {
            statement.setObject(sqlIndex, value, typeCode);
        }
    }

    /**
     * Creates an iterator over the given result set.
     *
     * @param model      The database model
     * @param resultSet  The result set to iterate over
     * @param queryHints The tables that were queried in the query that produced the
     *                   given result set (optional)
     * @return The iterator
     */
    protected DynaSqlIterator createResultSetIterator(Database model, ResultSet resultSet, Table[] queryHints)
    {
        return new DynaSqlIterator(getPlatformInfo(), model, resultSet, queryHints, true);
    }
}
