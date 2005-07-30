package org.apache.ddlutils.util;

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

import java.sql.Connection;
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
 * @version $Revision$
 */
public abstract class JdbcSupport {

    /** The Log to which logging calls will be made. */
    private final Log log = LogFactory.getLog(getClass());
    
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
     * Returns a (new) JDBC connection from the data source.
     * 
     * @return A JDBC connection
     */
    public Connection borrowConnection() throws SQLException
    {
        return getDataSource().getConnection();
    }
    
    /**
     * Closes a JDBC connection (returns it back to pool if a poolable datasource).
     * 
     * @param connection The connection
     */
    public void returnConnection(Connection connection)
    {
        try
        {
            if ((connection != null) && !connection.isClosed())
            {
                connection.close();
            }
        }
        catch (Exception e)
        {
            log.error("Caught exception while returning connection to pool", e);
        }
    }

    /**
     * Closes the given statement (which also closes all result sets for this statement) and the
     * connection it belongs to.
     * 
     * @param statement The statement
     */
    public void closeStatementAndConnection(Statement statement)
    {
        if (statement != null)
        {
            try
            {
                Connection conn = statement.getConnection();

                if ((conn != null) && !conn.isClosed())
                {
                    statement.close();
                    // this might have closed the connection ?
                    if (!conn.isClosed())
                    {
                        returnConnection(conn);
                    }
                }
            }
            catch (Exception e)
            {
                log.warn("Ignoring exception closing statement", e);
            }
        }
    }
}
