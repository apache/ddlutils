package org.apache.ddlutils.dynabean;

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

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.builder.SqlBuilder;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.util.JdbcSupport;

/**
 * DynaSql provides simple access to relational data
 * using a Database to implement the persistent model and
 * DynaBean instances for each row of a table.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision$
 */
public class DynaSql extends JdbcSupport
{
    /** The Log to which logging calls will be made. */
    private final Log _log = LogFactory.getLog(DynaSql.class);

    /** The sql builder */
    private SqlBuilder _builder;

    /** The current database model */
    private Database _database;

    /** A cache of the SqlDynaClasses per table name */
    private Map _dynaClassCache = new HashMap();

    public DynaSql(SqlBuilder builder)
    {
        _builder = builder;
    }
    

    public DynaSql(SqlBuilder builder, DataSource dataSource, Database database)
    {
        super(dataSource);
        _builder = builder;
        _database = database;
    }

    /**
     * Creates a new DynaBean instance for the given table name.
     * @return the new empty DynaBean for the given tableName or null if the
     *  table does not exist in the current Database model.
     */
    public DynaBean newInstance(String tableName) throws IllegalAccessException, InstantiationException
    {
        SqlDynaClass dynaClass = getDynaClass(tableName);

        return dynaClass != null ? dynaClass.newInstance() : null;
    }

    /**
     * <p>
     * Creates a new DynaBean instance of the given table name and copies the values from the
     * given source object. The source object can be a bean, a Map or a DynaBean.
     * </p>
     * <p>
     * This method is useful when iterating through an arbitrary DynaBean
     * result set after performing a query, then creating a copy as a DynaBean
     * which is bound to a specific table.
     * This new DynaBean can be kept around, changed and stored back into the database.
     * </p>
     *
     * @param tableName is the name of the table to which the new DynaBean will be bound
     * @param source is either a bean, a Map or a DynaBean that will be used to populate
     *      returned DynaBean.
     * @return a DynaBean bound to the given table name and containing all the properties from
     *  the given source object
     */
    public DynaBean copy(String tableName, Object source) throws IllegalAccessException, InstantiationException, InvocationTargetException
    {
        DynaBean answer = newInstance(tableName);

        // copy all the properties from the source
        BeanUtils.copyProperties(answer, source);

        return answer;
    }

    /**
     * Performs the given SQL query returning an iterator over the results.
     * 
     * @param sql The sql query to perform
     * @return An iterator for the dyna beans resulting from the query
     */
    public Iterator query(String sql) throws SQLException, IllegalAccessException, InstantiationException
    {
        Connection connection = borrowConnection();
        Statement  statement  = null;
        ResultSet  resultSet  = null;
        Iterator   answer     = null;

        try
        {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            answer    = createResultSetIterator(resultSet);
            return answer;
        }
        finally
        {
            // if any exceptions are thrown, close things down
            // otherwise we're leaving it open for the iterator
            if (answer == null)
            {
                closeResources(connection, statement, resultSet);
            }
        }
    }

    /**
     * Performs the given parameterized SQL query returning an iterator over the results.
     *
     * @param sql        The sql query to perform
     * @param parameters The parameter values
     * @return An iterator for the dyna beans resulting from the query
     */
    public Iterator query(String sql, Collection parameters) throws SQLException, IllegalAccessException, InstantiationException
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
            answer    = createResultSetIterator(resultSet);
            return answer;
        }
        finally
        {
            // if any exceptions are thrown, close things down
            if (answer == null)
            {
                closeResources(connection, statement, resultSet);
            }
        }
    }

    /**
     * Queries for a list of dyna beans representing rows of the given query.
     * In contrast to the {@link #query(String)} method all beans will be
     * materialized and the connection will be closed before returning the beans. 
     * 
     * @param sql The sql query
     * @return The dyna beans resulting from the query
     */
    public List fetch(String sql) throws SQLException, IllegalAccessException, InstantiationException
    {
        return fetch(sql, 0, -1);
    }

    /**
     * Queries for a list of dyna beans representing rows of the given query.
     * In contrast to the {@link #query(String, Collection)} method all beans will be
     * materialized and the connection will be closed before returning the beans. 
     * 
     * @param sql        The parameterized query
     * @param parameters The parameter values
     * @return The dyna beans resulting from the query
     */
    public List fetch(String sql, Collection parameters) throws SQLException, IllegalAccessException, InstantiationException
    {
        return fetch(sql, parameters, 0, -1);
    }

    /**
     * Queries for a list of dyna beans representing rows of the given query.
     * In contrast to the {@link #query(String)} method all beans will be
     * materialized and the connection will be closed before returning the beans.
     * Also, the two int parameters specify which rows of the result set to use.
     * If there are more rows than desired, they will be ignored (and not read
     * from the database).
     * 
     * @param sql   The sql query
     * @param start Row number to start from (0 for first row)
     * @param end   Row number to stop at (inclusively; -1 for last row)
     * @return The dyna beans resulting from the query
     */
    public List fetch(String sql, int start, int end) throws SQLException, IllegalAccessException, InstantiationException
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

            for (Iterator it = createResultSetIterator(resultSet); (rowIdx <= end) && it.hasNext(); rowIdx++)
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
            closeResources(connection, statement, resultSet);
        }
        return result;
    }

    /**
     * Queries for a list of dyna beans representing rows of the given query.
     * In contrast to the {@link #query(String, Collection)} method all beans will be
     * materialized and the connection will be closed before returning the beans.
     * Also, the two int parameters specify which rows of the result set to use.
     * If there are more rows than desired, they will be ignored (and not read
     * from the database).
     * 
     * @param sql        The parameterized sql query
     * @param parameters The parameter values
     * @param start      Row number to start from (0 for first row)
     * @param end        Row number to stop at (inclusively; -1 for last row)
     * @return The dyna beans resulting from the query
     */
    public List fetch(String sql, Collection parameters, int start, int end) throws SQLException, IllegalAccessException, InstantiationException
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

            for (Iterator it = createResultSetIterator(resultSet); (rowIdx <= end) && it.hasNext(); rowIdx++)
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
            closeResources(connection, statement, resultSet);
        }
        return result;
    }

    /**
     * Returns the {@link SqlDynaClass} for the given table name. If the it does not
     * exist yet, a new one will be created based on the Table definition.
     * 
     * @return The <code>SqlDynaClass</code> for the indicated table
     */
    public SqlDynaClass getDynaClass(String tableName)
    {
        SqlDynaClass answer = (SqlDynaClass)_dynaClassCache.get(tableName);

        if (answer == null)
        {
            Table table = getDatabase().findTable(tableName);

            if (table != null)
            {
                answer = createSqlDynaClass(table);
                _dynaClassCache.put(tableName, answer);
            }
            else
            {
                _log.warn("Could not find table " + tableName);
            }
        }
        return answer;
    }


    /**
     * Stores the given DynaBean in the database, inserting it if there is no primary key
     * otherwise the bean is updated in the database.
     */
    public void store(DynaBean dynaBean) throws SQLException
    {
        Connection connection = borrowConnection();

        try
        {
            if (exists(dynaBean, connection))
            {
                update(dynaBean, connection);
            }
            else
            {
                insert(dynaBean, connection);
            }
        }
        finally
        {
            returnConnection(connection);
        }
    }


    /**
     * Returns the sql for inserting the bean.
     * 
     * @param dynaBean The bean
     * @return The sql
     */
    public String getInsertSql(DynaBean dynaBean)
    {
        SqlDynaClass      dynaClass  = getSqlDynaClass(dynaBean);
        SqlDynaProperty[] properties = dynaClass.getSqlDynaProperties();

        if (properties.length == 0)
        {
            _log.info("Cannot insert type " + dynaClass + " as there are no properties");
            return null;
        }

        return createInsertSql(dynaClass, properties, dynaBean);
    }

    /**
     * Inserts the given DynaBean in the database, assuming the primary key values are specified.
     */
    public void insert(DynaBean dynaBean) throws SQLException
    {
        Connection connection = borrowConnection();

        try
        {
            insert(dynaBean, connection);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * Returns the sql for updating the given bean in the database.
     * 
     * @param dynaBean The bean
     * @return The sql
     */
    public String getUpdateSql(DynaBean dynaBean)
    {
        SqlDynaClass      dynaClass   = getSqlDynaClass(dynaBean);
        SqlDynaProperty[] primaryKeys = dynaClass.getPrimaryKeyProperties();

        if (primaryKeys.length == 0)
        {
            _log.info("Cannot update type " + dynaClass + " as there are no primary keys to update");
            return null;
        }

        return createUpdateSql(dynaClass, primaryKeys, dynaClass.getNonPrimaryKeyProperties(), dynaBean);
    }

    /**
     * Updates the given dyna bean in the database, assuming the primary key values are specified.
     * 
     * @param dynaBean The bean
     */
    public void update(DynaBean dynaBean) throws SQLException
    {
        Connection connection = borrowConnection();

        try
        {
            update(dynaBean, connection);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * Deletes the given dyna bean from the database, assuming the primary key values are specified.
     * 
     * @param dynaBean The dyna bean
     */
    public void delete(DynaBean dynaBean) throws SQLException
    {
        Connection        connection = borrowConnection();
        PreparedStatement statement  = null;

        try
        {
            SqlDynaClass      dynaClass   = getSqlDynaClass(dynaBean);
            SqlDynaProperty[] primaryKeys = dynaClass.getPrimaryKeyProperties();

            if (primaryKeys.length == 0)
            {
                _log.warn("Cannot delete type " + dynaClass + " as there are no primary keys");
                return;
            }

            String sql = createDeleteSql(dynaClass, primaryKeys, null);

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
        finally
        {
            closeStatement(statement);
            returnConnection(connection);
        }
    }

    /**
     * Returns the sql for deleting the given DynaBean from the database.
     * 
     * @param dynaBean The bean
     * @return The sql
     */
    public String getDeleteSql(DynaBean dynaBean)
    {
        SqlDynaClass      dynaClass   = getSqlDynaClass(dynaBean);
        SqlDynaProperty[] primaryKeys = dynaClass.getPrimaryKeyProperties();

        if (primaryKeys.length == 0)
        {
            _log.warn("Cannot delete type " + dynaClass + " as there are no primary keys");
            return null;
        }

        return createDeleteSql(dynaClass, primaryKeys, dynaBean);
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * Returns the database.
     * 
     * @return The model
     */
    public Database getDatabase()
    {
        return _database;
    }

    /**
     * Sets the database model.
     * 
     * @param database The model
     */
    public void setDatabase(Database database)
    {
        _database = database;
        _dynaClassCache.clear();
    }

    /**
     * Returns the builder (if any).
     * 
     * @return The sql builder
     */
    public SqlBuilder getSqlBuilder()
    {
        return _builder;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Determines whether this dynaBean has a primary key.
     * 
     * @todo Doesn't do anything yet
     * @return <code>true</code> if this dynaBean has a primary key
     */
    protected boolean exists(DynaBean dynaBean, Connection connection)
    {
        return false;
    }

    /**
     * Inserts the bean. If one of the columns is an auto-incremented column, then the
     * dyna bean will also be updated with the column value generated by the database.
     * 
     * @param dynaBean   The bean
     * @param connection The database connection
     */
    public void insert(DynaBean dynaBean, Connection connection) throws SQLException
    {
        SqlDynaClass      dynaClass  = getSqlDynaClass(dynaBean);
        SqlDynaProperty[] properties = dynaClass.getSqlDynaProperties();

        if (properties.length == 0)
        {
            _log.info("Cannot insert type " + dynaClass + " as there are no properties");
            return;
        }

        Column autoIdColumn = _database.findTable(dynaClass.getTableName()).getAutoIncrementColumn();

        if (autoIdColumn != null)
        {
            // We have to remove the auto-increment column as some databases won't like
            // it being present in the insert command
            SqlDynaProperty[] newProperties = new SqlDynaProperty[properties.length - 1];
            int               newIdx        = 0;

            for (int idx = 0; idx < properties.length; idx++)
            {
                if (properties[idx].getColumn() != autoIdColumn)
                {
                    newProperties[newIdx++] = properties[idx];
                }
            }
            properties = newProperties;
        }
        
        String            insertSql    = createInsertSql(dynaClass, properties, null);
        String            queryIdSql   = autoIdColumn != null ? createSelectLastInsertIdSql(dynaClass) : null;
        PreparedStatement statement    = null;

        if (_log.isDebugEnabled())
        {
            _log.debug("About to execute SQL: " + insertSql);
        }
        if ((autoIdColumn != null) && (queryIdSql == null))
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

                Object value = lastInsertedIds.getObject(autoIdColumn.getName());

                PropertyUtils.setProperty(dynaBean, autoIdColumn.getName(), value);
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
            finally
            {
                if (lastInsertedIds != null)
                {
                    lastInsertedIds.close();
                }
                closeStatement(queryStmt);
            }
        }
    }

    /**
     * Updates the row which maps to the given dynabean.
     * 
     * @param dynaBean   The bean
     * @param connection The database connection
     */
    public void update(DynaBean dynaBean, Connection connection) throws SQLException
    {
        SqlDynaClass      dynaClass   = getSqlDynaClass(dynaBean);
        SqlDynaProperty[] primaryKeys = dynaClass.getPrimaryKeyProperties();

        if (primaryKeys.length == 0)
        {
            _log.info("Cannot update type " + dynaClass + " as there are no primary keys to update");
            return;
        }

        SqlDynaProperty[] properties = dynaClass.getNonPrimaryKeyProperties();
        String            sql        = createUpdateSql(dynaClass, primaryKeys, properties, null);
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
                         " in table " + dynaClass.getTableName() +
                         " but changed " + count + " row(s)");
            }
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
    private HashMap toColumnValues(SqlDynaProperty[] properties, DynaBean bean)
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
     * Creates the SQL for inserting an object of the given type. If a concrete bean is given,
     * then a concrete insert statement is created, otherwise an insert statement usable in a
     * prepared statement is build. 
     * 
     * @param dynaClass  The type
     * @param properties The properties to write
     * @param bean       Optionally the concrete bean to insert
     * @return The SQL required to insert an instance of the class
     */
    protected String createInsertSql(SqlDynaClass dynaClass, SqlDynaProperty[] properties, DynaBean bean)
    {
        Table   table        = _database.findTable(dynaClass.getTableName());
        HashMap columnValues = toColumnValues(properties, bean);

        return _builder.getInsertSql(table, columnValues, bean == null);
    }

    /**
     * Creates the SQL for querying for the id generated by the last insert of an object of the given type.
     * 
     * @param dynaClass The type
     * @return The SQL required for querying for the id, or <code>null</code> if the database does not
     *         support this
     */
    protected String createSelectLastInsertIdSql(SqlDynaClass dynaClass)
    {
        Table table = _database.findTable(dynaClass.getTableName());

        return _builder.getSelectLastInsertId(table);
    }

    /**
     * Creates the SQL for updating an object of the given type. If a concrete bean is given,
     * then a concrete update statement is created, otherwise an update statement usable in a
     * prepared statement is build.
     * 
     * @param dynaClass   The type
     * @param primaryKeys The primary keys
     * @param properties  The properties to write
     * @param bean        Optionally the concrete bean to update
     * @return The SQL required to update an instance of the class
     */
    protected String createUpdateSql(SqlDynaClass dynaClass, SqlDynaProperty[] primaryKeys, SqlDynaProperty[] properties, DynaBean bean)
    {
        Table   table        = _database.findTable(dynaClass.getTableName());
        HashMap columnValues = toColumnValues(properties, bean);

        return _builder.getUpdateSql(table, columnValues, bean == null);
    }

    /**
     * Creates the SQL for deleting an object of the given type. If a concrete bean is given,
     * then a concrete delete statement is created, otherwise a delete statement usable in a
     * prepared statement is build.
     * 
     * @param dynaClass   The type
     * @param primaryKeys The primary keys
     * @param bean        Optionally the concrete bean to update
     * @return The SQL required to update an instance of the class
     */
    protected String createDeleteSql(SqlDynaClass dynaClass, SqlDynaProperty[] primaryKeys, DynaBean bean)
    {
        Table   table    = _database.findTable(dynaClass.getTableName());
        HashMap pkValues = toColumnValues(primaryKeys, bean);

        return _builder.getDeleteSql(table, pkValues, bean == null);
    }

    /**
     * Returns the {@link SqlDynaClass} for the given bean.
     * 
     * @return The dyna bean class
     */
    public SqlDynaClass getSqlDynaClass(DynaBean dynaBean)
    {
        DynaClass dynaClass = dynaBean.getDynaClass();

        if (dynaClass instanceof SqlDynaClass)
        {
            return (SqlDynaClass) dynaClass;
        }
        else
        {
            // TODO: we could autogenerate an SqlDynaClass here ?
            throw new IllegalArgumentException("The dynaBean is not an instance of an SqlDynaClass");
        }
    }

    /**
     * Creates a new {@link SqlDynaClass} instance for the given table based on the table definition.
     * 
     * @param table The table
     */
    protected SqlDynaClass createSqlDynaClass(Table table)
    {
        return SqlDynaClass.newInstance(table);
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
     * Tries to convert the given object into an instance of the specified target class. This is mostly
     * useful to convert between numeric types.
     * 
     * @param source      The source object
     * @param targetClass The target class
     * @return The converted object
     */
    private Object convert(Object source, Class targetClass)
    {
        if (source == null)
        {
            return null;
        }
        
        Class sourceClass  = source.getClass();

        if (sourceClass.equals(targetClass))
        {
            return source;
        }
        if (targetClass.equals(String.class))
        {
            return source.toString();
        }

        long    longValue    = 0;
        double  doubleValue  = 0;
        boolean toShort      = false;
        boolean toInteger    = false;
        boolean toLong       = false;
        boolean toFloat      = false;

        if (sourceClass.equals(Byte.class))
        {
            longValue   = ((Byte)source).longValue();
            doubleValue = longValue;
            toShort     = true;
            toInteger   = true;
            toLong      = true;
            toFloat     = true;
        }
        else if (sourceClass.equals(Short.class))
        {
            longValue   = ((Short)source).longValue();
            doubleValue = longValue;
            toShort     = true;
            toInteger   = true;
            toLong      = true;
            toFloat     = true;
        }
        else if (sourceClass.equals(Integer.class))
        {
            longValue   = ((Integer)source).longValue();
            doubleValue = longValue;
            toShort     = (longValue >= (long)Short.MIN_VALUE) && (longValue <= (long)Short.MAX_VALUE);
            toInteger   = true;
            toLong      = true;
            toFloat     = true;
        }
        else if (sourceClass.equals(Long.class))
        {
            longValue   = ((Long)source).longValue();
            doubleValue = longValue;
            toShort     = (longValue >= (long)Short.MIN_VALUE) && (longValue <= (long)Short.MAX_VALUE);
            toInteger   = (longValue >= (long)Integer.MIN_VALUE) && (longValue <= (long)Integer.MAX_VALUE);
            toLong      = true;
            toFloat     = true;
        }
        else if (sourceClass.equals(Float.class))
        {
            doubleValue = ((Float)source).doubleValue();
            toFloat     = true;
        }
        else if (sourceClass.equals(Double.class))
        {
            doubleValue = ((Double)source).doubleValue();
            toFloat     = (doubleValue >= (double)Float.MIN_VALUE) && (doubleValue <= (double)Float.MAX_VALUE);
        }
        else
        {
            throw new IllegalArgumentException("Cannot convert from "+sourceClass.getName()+" to "+targetClass.getName());
        }

        if (targetClass.equals(Short.class))
        {
            if (toShort)
            {
                return new Short((short)longValue);
            }
        }
        else if (targetClass.equals(Integer.class))
        {
            if (toInteger)
            {
                return new Integer((int)longValue);
            }
        }
        else if (targetClass.equals(Long.class))
        {
            if (toLong)
            {
                return new Long(longValue);
            }
        }
        else if (targetClass.equals(Float.class))
        {
            if (toFloat)
            {
                return new Float((float)doubleValue);
            }
        }
        else if (targetClass.equals(Double.class))
        {
            return new Double(doubleValue);
        }
        else if (targetClass.equals(BigDecimal.class))
        {
            return (toLong ? new BigDecimal(longValue) : new BigDecimal(doubleValue));
        }
        throw new IllegalArgumentException("Cannot convert from "+sourceClass.getName()+" to "+targetClass.getName());
    }

    /**
     * Creates an iterator over the given result set.
     *
     * @param resultSet The result set to iterate over
     */
    protected DynaSqlIterator createResultSetIterator(ResultSet resultSet)
    {
        return new DynaSqlIterator(this, resultSet, true);
    }
}
