/*
 * $Header: /home/cvs/jakarta-commons-sandbox/jelly/src/java/org/apache/commons/jelly/CompilableTag.java,v 1.5 2002/05/17 15:18:12 jstrachan Exp $
 * $Revision: 1.5 $
 * $Date: 2002/05/17 15:18:12 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 * 
 * $Id: CompilableTag.java,v 1.5 2002/05/17 15:18:12 jstrachan Exp $
 */
package org.apache.commons.sql.dynabean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.sql.model.Database;
import org.apache.commons.sql.model.Table;

/**
 * DynaSql provides simple access to relational data
 * using a Database to implement the persistent model and
 * DynaBean instances for each row of a table.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.14 $
 */
public abstract class DynaSql {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog( DynaSql.class );
    
    /** The current database model */
    private Database database;
    
    /** A cache of the SqlDynaClasses per table name */
    private Map dynaClassCache = new HashMap();
    
    public DynaSql() {
    }

    public DynaSql(Database database) {
        this.database = database;
    }

    /**
     * Creates a new DynaBean instance for the given table name.
     * @return the new empty DynaBean for the given tableName or null if the
     *  table does not exist in the current Database model.
     */
    public DynaBean newInstance(String tableName) {
        SqlDynaClass dynaClass = getSqlDynaClass(tableName);
        return null;
    }
    
    /**
     * Stores the given DyanBean in the database, inserting it if there is no primary key
     * otherwise the bean is updated in the database.
     */
    public void store(DynaBean dynaBean) throws SQLException {
        Connection connection = borrowConnection();
        try {
            if (exists(dynaBean, connection)) {
                update(dynaBean, connection);
            }
            else {
                insert(dynaBean, connection);
            }
        }
        finally {
            returnConnection(connection);
        }
    }
    
    /**
     * Inserts the given DyanBean in the database, assuming the primary key values are specified.
     */
    public void insert(DynaBean dynaBean) throws SQLException {
        Connection connection = borrowConnection();
        try {
            insert(dynaBean, connection);
        }
        finally {
            returnConnection(connection);
        }
    }
    
    /**
     * Updates the given DyanBean in the database, assuming the primary key values are specified.
     */
    public void update(DynaBean dynaBean) throws SQLException {
        Connection connection = borrowConnection();
        try {
            update(dynaBean, connection);
        }
        finally {
            returnConnection(connection);
        }
    }
    
    /**
     * Deletes the given DynaBean from the database, assuming the primary key values are specified.
     */
    public void delete(DynaBean dynaBean) throws SQLException {
        Connection connection = borrowConnection();
        PreparedStatement statement = null;
        try {
            SqlDynaClass dynaClass = getSqlDynaClass(dynaBean);
            SqlDynaProperty[] primaryKeys = dynaClass.getPrimaryKeyProperties();
            int size = primaryKeys.length;
            if (size == 0) {
                log.info( "Cannot update type: " + dynaClass + " as there are no primary keys to update" );        
                return;
            }
            String sql = createDeleteSql(dynaClass, primaryKeys);

            log.info( "About to execute SQL: " + sql );
            
            statement = connection.prepareStatement( sql );   
            
            for ( int i = 0; i < size; i++ ) {
                SqlDynaProperty primaryKey = primaryKeys[i];
                setObject(statement, 1+i, dynaBean, primaryKey);
            }
            int count = statement.executeUpdate();
            if ( count != 1 ) {
                log.warn( "Attempted to delete a single row : " + dynaBean 
                    + " in table: " + dynaClass.getTableName() 
                    + " but changed: " + count + " row(s)" 
                );
            }
        }
        finally {
            closeStatement(statement);
            returnConnection(connection);
        }
    }


    // Properties
    //-------------------------------------------------------------------------                

    /**
     * Returns the database.
     * @return Database
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Sets the database.
     * @param database The database to set
     */
    public void setDatabase(Database database) {
        this.database = database;
        this.dynaClassCache.clear();
    }


    // Implementation methods    
    //-------------------------------------------------------------------------                

    /**
     * @return a new JDBC connection from the pool
     */
    protected abstract Connection borrowConnection() throws SQLException;
    
    /**
     * Returns a JDBC connection back into the pool
     */
    protected abstract void returnConnection(Connection connection) throws SQLException;
    
    /**
     * @return true if this dynaBean has a primary key
     */
    protected boolean exists(DynaBean dynaBean, Connection connection) {
        return false;
    }

    /**
     * Closes the given result set down.
     */
    protected void closeResultSet(ResultSet resultSet) {
        if ( resultSet != null ) {
            try {
                resultSet.close();
            }
            catch (Exception e) {
                log.warn("Ignoring exception closing result set: " + e, e);
            }
        }
    }

    /**
     * Closes the given statement down.
     */
    protected void closeStatement(Statement statement) {
        if ( statement != null ) {
            try {
                statement.close();
            }
            catch (Exception e) {
                log.warn("Ignoring exception closing statement: " + e, e);
            }
        }
    }
    
     /**
     * Creates a new primary key value, inserts the bean and returns the new item.
     */
    protected void insert(DynaBean dynaBean, Connection connection) throws SQLException {
        SqlDynaClass dynaClass = getSqlDynaClass(dynaBean);
        SqlDynaProperty[] properties = dynaClass.getSqlDynaProperties();
        int size = properties.length;
        if (size == 0) {
            log.info( "Cannot insert type: " + dynaClass + " as there are no properties" );        
            return;
        }
        
        String sql = createInsertSql(dynaClass, properties);
        
        log.info( "About to execute SQL: " + sql );
        
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement( sql );   
            
            for ( int i = 0; i < size; i++ ) {
                SqlDynaProperty property = properties[i];
                setObject(statement, 1+i, dynaBean, property);
            }
            int count = statement.executeUpdate();
            if ( count != 1 ) {
                log.warn( "Attempted to insert a single row : " + dynaBean 
                    + " in table: " + dynaClass.getTableName() 
                    + " but changed: " + count + " row(s)" 
                );
            }
        }
        finally {
            closeStatement(statement);
        }
    }
    
    /**
     * Updates the row which maps to the given dynabean
     */
    protected void update(DynaBean dynaBean, Connection connection) throws SQLException {
        SqlDynaClass dynaClass = getSqlDynaClass(dynaBean);
        SqlDynaProperty[] primaryKeys = dynaClass.getPrimaryKeyProperties();
        if (primaryKeys.length == 0) {
            log.info( "Cannot update type: " + dynaClass + " as there are no primary keys to update" );        
            return;
        }

        SqlDynaProperty[] properties = dynaClass.getNonPrimaryKeyProperties();
        
        String sql = createUpdateSql(dynaClass, primaryKeys, properties);
        
        log.info( "About to execute SQL: " + sql );

        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement( sql );   
            
            int sqlIndex = 1;
            for ( int i = 0, size = properties.length; i < size; i++ ) {
                SqlDynaProperty property = properties[i];
                setObject(statement, sqlIndex++, dynaBean, property);
            }
            for ( int i = 0, size = primaryKeys.length; i < size; i++ ) {
                SqlDynaProperty primaryKey = primaryKeys[i];
                setObject(statement, sqlIndex++, dynaBean, primaryKey);
            }
            int count = statement.executeUpdate();
            if ( count != 1 ) {
                log.warn( "Attempted to insert a single row : " + dynaBean 
                    + " in table: " + dynaClass.getTableName() 
                    + " but changed: " + count + " row(s)" 
                );
            }
        }
        finally {
            closeStatement(statement);
        }
    }

        
    /** 
     * @return the SQL required to update the given item
     */
    protected String createInsertSql(SqlDynaClass dynaClass, SqlDynaProperty[] properties) {
        StringBuffer buffer = new StringBuffer( "insert into " );
        buffer.append( dynaClass.getTableName() );
        buffer.append( " (" );

        int size = properties.length;
        for ( int i = 0; i < size; i++ ) {
            SqlDynaProperty property = properties[i];
            if (i > 0) {
                buffer.append(", " );
            }
            buffer.append( property.getName() );
        }
        buffer.append( ") values (" );
        
        if ( size > 0) {
            buffer.append( "?" );
            for ( int i = 1; i < size; i++ ) {
                buffer.append( ", ?" );
            }
        }
        buffer.append( ")" );
        return buffer.toString();
    }

    /** 
     * @return the SQL required to update the given item
     */
    protected String createUpdateSql(SqlDynaClass dynaClass, SqlDynaProperty[] primaryKeys, SqlDynaProperty[] properties) {
        StringBuffer buffer = new StringBuffer( "update " );
        buffer.append( dynaClass.getTableName() );
        buffer.append( " set " );
        
        for ( int i = 0; i < properties.length; i++ ) {
            SqlDynaProperty property = properties[i];
            if (i > 0) {
                buffer.append(", " );
            }
            buffer.append( property.getName() );
            buffer.append( " = ?" );
        }
        buffer.append( " where " );

        for (int i = 0, size = primaryKeys.length; i < size; i++ ) {        
            SqlDynaProperty primaryKey = primaryKeys[i];
            if (i > 0) {
                buffer.append(", " );
            }
            buffer.append( primaryKey.getName() );
            buffer.append( " = ?" );
        }
        return buffer.toString();
    }

    /** 
     * @return the SQL required to update the given item
     */
    protected String createDeleteSql(SqlDynaClass dynaClass, SqlDynaProperty[] primaryKeys) {
        StringBuffer buffer = new StringBuffer( "delete " );
        buffer.append( dynaClass.getTableName() );
        buffer.append( " where " );

        int size = primaryKeys.length;
        for ( int i = 0; i < size; i++ ) {
            SqlDynaProperty primaryKey = primaryKeys[i];
            if (i > 0) {
                buffer.append(" and " );
            }
            buffer.append( primaryKey.getName() );
            buffer.append( " = ?" );
        }
        return buffer.toString();
    }

    /**
     * @return the SqlDynaClass for the given DynaBean or throws an exception if it could not be found
     */
    protected SqlDynaClass getSqlDynaClass(DynaBean dynaBean) {
        DynaClass dynaClass = dynaBean.getDynaClass();
        if (dynaClass instanceof SqlDynaClass) {
            return (SqlDynaClass) dynaClass;
        }
        else {
            // #### we could autogenerate an SqlDynaClass here
            throw new IllegalArgumentException( "The dynaBean is not an instance of an SqlDynaClass" );
        }
    }

    /**
     * @return the SqlDynaClass for the given table name. If the SqlDynaClass does not exist
     * then create a new one based on the Table definition
     */
    protected SqlDynaClass getSqlDynaClass(String tableName) {
        SqlDynaClass answer = (SqlDynaClass) dynaClassCache.get(tableName);
        if (answer == null) {
            Table table = getDatabase().findTable(tableName);
            if (table != null) {
                answer = createSqlDynaClass(table);
                dynaClassCache.put(tableName, dynaClassCache);
            }
        }
        return answer;
    }
    
    /**
     * A Factory method to create a new SqlDynaClass instance for the given table name If the SqlDynaClass does not exist
     * then create a new one based on the Table definition
     */
    protected SqlDynaClass createSqlDynaClass(Table table) {
        return SqlDynaClass.newInstance(table);
    }

    /**
     * Sets property value with the given prepared statement, doing any type specific conversions or swizzling.
     */
    protected void setObject(PreparedStatement statement, int sqlIndex, DynaBean dynaBean, SqlDynaProperty property) throws SQLException {
        Object value = dynaBean.get(property.getName());    
        statement.setObject(sqlIndex, value);
    }
}
