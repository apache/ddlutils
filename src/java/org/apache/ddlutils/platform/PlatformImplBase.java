package org.apache.ddlutils.platform;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.DynaSqlException;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.builder.SqlBuilder;
import org.apache.ddlutils.dynabean.DynaSqlIterator;
import org.apache.ddlutils.dynabean.SqlDynaClass;
import org.apache.ddlutils.dynabean.SqlDynaProperty;
import org.apache.ddlutils.io.JdbcModelReader;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.util.JdbcSupport;

/**
 * Base class for platform implementations.
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 231110 $
 */
public abstract class PlatformImplBase extends JdbcSupport implements Platform
{
    /** The log for this platform */
    private final Log _log = LogFactory.getLog(getClass());

    /** The sql builder for this platform */
    private SqlBuilder _builder;

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#getSqlBuilder()
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

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#getPlatformInfo()
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

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#evaluateBatch(java.lang.String, boolean)
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

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#evaluateBatch(java.sql.Connection, java.lang.String, boolean)
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

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#createTables(org.apache.ddlutils.model.Database, boolean, boolean)
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

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#createDatabase(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Map)
     */
    public void createDatabase(String jdbcDriverClassName, String connectionUrl, String username, String password, Map parameters) throws DynaSqlException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Database creation is not supported for the database platform "+getName());
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#dropDatabase(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void dropDatabase(String jdbcDriverClassName, String connectionUrl, String username, String password) throws DynaSqlException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Database deletion is not supported for the database platform "+getName());
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#createTables(java.sql.Connection, org.apache.ddlutils.model.Database, boolean, boolean)
     */
    public void createTables(Connection connection, Database model, boolean dropTablesFirst, boolean continueOnError) throws DynaSqlException
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
        evaluateBatch(connection, sql, continueOnError);
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#alterTables(org.apache.ddlutils.model.Database, boolean)
     */
    public void alterTables(Database desiredDb, boolean continueOnError) throws DynaSqlException
    {
        alterTables(desiredDb, false, false, continueOnError);
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#alterTables(org.apache.ddlutils.model.Database, boolean, boolean, boolean)
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

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#alterTables(java.sql.Connection, org.apache.ddlutils.model.Database, boolean, boolean, boolean)
     */
    public void alterTables(Connection connection, Database desiredModel, boolean doDrops, boolean modifyColumns, boolean continueOnError) throws DynaSqlException
    {
        String   sql          = null;
        Database currentModel = null;

        try
        {
            currentModel = new JdbcModelReader(connection).getDatabase();
        }
        catch (SQLException ex)
        {
            throw new DynaSqlException("Error while reading the model from the database", ex);
        }

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
        evaluateBatch(connection, sql, continueOnError);
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#alterTables(java.sql.Connection, org.apache.ddlutils.model.Database, boolean)
     */
    public void alterTables(Connection connection, Database desiredDb, boolean continueOnError) throws DynaSqlException
    {
        alterTables(connection, desiredDb, false, false, continueOnError);
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#dropTables(org.apache.ddlutils.model.Database, boolean)
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

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#dropTables(java.sql.Connection, org.apache.ddlutils.model.Database, boolean)
     */
    public void dropTables(Connection connection, Database model, boolean continueOnError) throws DynaSqlException 
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
        evaluateBatch(connection, sql, continueOnError);
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#query(org.apache.ddlutils.model.Database, java.lang.String)
     */
    public Iterator query(Database model, String sql) throws DynaSqlException
    {
        Connection connection = borrowConnection();
        Statement  statement  = null;
        ResultSet  resultSet  = null;
        Iterator   answer     = null;

        try
        {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            answer    = createResultSetIterator(model, resultSet);
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

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#query(org.apache.ddlutils.model.Database, java.lang.String, java.util.Collection)
     */
    public Iterator query(Database model, String sql, Collection parameters) throws DynaSqlException
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
            answer    = createResultSetIterator(model, resultSet);
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

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#fetch(org.apache.ddlutils.model.Database, java.lang.String)
     */
    public List fetch(Database model, String sql) throws DynaSqlException
    {
        return fetch(model, sql, 0, -1);
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#fetch(org.apache.ddlutils.model.Database, java.lang.String, int, int)
     */
    public List fetch(Database model, String sql, int start, int end) throws DynaSqlException
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

            for (Iterator it = createResultSetIterator(model, resultSet); ((end < 0) || (rowIdx <= end)) && it.hasNext(); rowIdx++)
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

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#fetch(org.apache.ddlutils.model.Database, java.lang.String, java.util.Collection)
     */
    public List fetch(Database model, String sql, Collection parameters) throws DynaSqlException
    {
        return fetch(model, sql, parameters, 0, -1);
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#fetch(org.apache.ddlutils.model.Database, java.lang.String, java.util.Collection, int, int)
     */
    public List fetch(Database model, String sql, Collection parameters, int start, int end) throws DynaSqlException
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

            for (Iterator it = createResultSetIterator(model, resultSet); ((end < 0) || (rowIdx <= end)) && it.hasNext(); rowIdx++)
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

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#getInsertSql(org.apache.ddlutils.model.Database, org.apache.commons.beanutils.DynaBean)
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

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#insert(org.apache.ddlutils.model.Database, org.apache.commons.beanutils.DynaBean, java.sql.Connection)
     */
    public void insert(Database model, DynaBean dynaBean, Connection connection) throws DynaSqlException
    {
        SqlDynaClass      dynaClass  = model.getDynaClassFor(dynaBean);
        SqlDynaProperty[] properties = dynaClass.getSqlDynaProperties();

        if (properties.length == 0)
        {
            _log.warn("Cannot insert instances of type " + dynaClass + " because it has no properties");
            return;
        }

        Column[] columns = model.findTable(dynaClass.getTableName()).getAutoIncrementColumn();

        if (columns.length > 0)
        {
            SqlDynaProperty[] newProperties = new SqlDynaProperty[properties.length - 1];
            int               newIdx        = 0;

            // We have to remove the auto-increment columns as some databases won't like
            // it being present in the insert command

            for (int propIdx = 0; propIdx < properties.length; propIdx++)
            {
                for (int autoIncrColumnIdx = 0; autoIncrColumnIdx < columns.length; autoIncrColumnIdx++)
                {
                    if (properties[propIdx].getColumn() != columns[autoIncrColumnIdx])
                    {
                        newProperties[newIdx++] = properties[propIdx];
                    }
                }
            }
            properties = newProperties;
        }
        
        String            insertSql    = createInsertSql(model, dynaClass, properties, null);
        String            queryIdSql   = columns.length > 0 ? createSelectLastInsertIdSql(model, dynaClass) : null;
        PreparedStatement statement    = null;

        if (_log.isDebugEnabled())
        {
            _log.debug("About to execute SQL: " + insertSql);
        }
        if ((columns.length > 0) && (queryIdSql == null))
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
                connection.commit();

                queryStmt       = connection.createStatement();
                lastInsertedIds = queryStmt.executeQuery(queryIdSql);

                lastInsertedIds.next();

                for (int idx = 0; idx < columns.length; idx++)
                {
                    Object value = lastInsertedIds.getObject(columns[idx].getName());
    
                    PropertyUtils.setProperty(dynaBean, columns[idx].getName(), value);
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

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#insert(org.apache.ddlutils.model.Database, org.apache.commons.beanutils.DynaBean)
     */
    public void insert(Database model, DynaBean dynaBean) throws DynaSqlException
    {
        Connection connection = borrowConnection();

        try
        {
            insert(model, dynaBean, connection);
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

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#getUpdateSql(org.apache.ddlutils.model.Database, org.apache.commons.beanutils.DynaBean)
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

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#update(org.apache.ddlutils.model.Database, org.apache.commons.beanutils.DynaBean, java.sql.Connection)
     */
    public void update(Database model, DynaBean dynaBean, Connection connection) throws DynaSqlException
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

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#update(org.apache.ddlutils.model.Database, org.apache.commons.beanutils.DynaBean)
     */
    public void update(Database model, DynaBean dynaBean) throws DynaSqlException
    {
        Connection connection = borrowConnection();

        try
        {
            update(model, dynaBean, connection);
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
    protected boolean exists(DynaBean dynaBean, Connection connection)
    {
        // TODO: check for the pk value, and if present, query against database
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#store(org.apache.ddlutils.model.Database, org.apache.commons.beanutils.DynaBean)
     */
    public void store(Database model, DynaBean dynaBean) throws DynaSqlException
    {
        Connection connection = borrowConnection();

        try
        {
            if (exists(dynaBean, connection))
            {
                update(model, dynaBean, connection);
            }
            else
            {
                insert(model, dynaBean, connection);
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

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#getDeleteSql(org.apache.ddlutils.model.Database, org.apache.commons.beanutils.DynaBean)
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

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#delete(org.apache.ddlutils.model.Database, org.apache.commons.beanutils.DynaBean)
     */
    public void delete(Database model, DynaBean dynaBean) throws DynaSqlException
    {
        Connection connection = borrowConnection();

        try
        {
            delete(model, dynaBean, connection);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#delete(org.apache.ddlutils.model.Database, org.apache.commons.beanutils.DynaBean, java.sql.Connection)
     */
    public void delete(Database model, DynaBean dynaBean, Connection connection) throws DynaSqlException
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
        int    typeCode = property.getColumn().getTypeCode();
        Object value    = dynaBean.get(property.getName());

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
     * @param model     The database model
     * @param resultSet The result set to iterate over
     */
    protected DynaSqlIterator createResultSetIterator(Database model, ResultSet resultSet)
    {
        return new DynaSqlIterator(getPlatformInfo(), model, resultSet, true);
    }
}
