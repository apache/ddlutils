/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.commons.sql.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JdbcSupport is an abstract base class for objects which need to 
 * perform JDBC operations. It contains a number of useful methods 
 * for implementation inheritence..
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.14 $
 */
public abstract class JdbcSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog( JdbcSupport.class );
    
    private DataSource dataSource;
    
    public JdbcSupport() {
    }

    public JdbcSupport(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Properties
    //-------------------------------------------------------------------------                
    
    /**
     * Returns the DataSource used to pool JDBC Connections.
     * @return DataSource
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Sets the DataSource used to pool JDBC Connections.
     * @param dataSource The dataSource to set
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Implementation methods    
    //-------------------------------------------------------------------------                

    /**
     * @return a new JDBC connection from the pool
     */
    protected Connection borrowConnection() throws SQLException {
        return getDataSource().getConnection();
    }
    
    /**
     * Returns a JDBC connection back into the pool
     */
    protected void returnConnection(Connection connection) {
        try {
            connection.close();
        }
        catch (Exception e) {
            log.error( "Caught exception while returning connection to pool: " + e, e);
        }
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
     * A helper method to close down any resources used and return the JDBC connection
     * back to the pool
     */
    protected void closeResources(Connection connection, Statement statement, ResultSet resultSet) {
        closeResultSet(resultSet);
        closeStatement(statement);
        returnConnection(connection);
    }
}
