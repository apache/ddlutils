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
import org.apache.ddlutils.DynaSqlException;

/**
 * JdbcSupport is an abstract base class for objects which need to 
 * perform JDBC operations. It contains a number of useful methods 
 * for implementation inheritence..
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision$
 */
public abstract class JdbcSupport
{

    /** The Log to which logging calls will be made. */
    private final Log _log = LogFactory.getLog(getClass());
    /** The data source */
    private DataSource _dataSource;

    /**
     * Creates a new instance without a data source.
     */
    public JdbcSupport()
    {
    }

    /**
     * Creates a new instance that uses the given data source for talking to
     * the database.
     * 
     * @param dataSource The data source
     */
    public JdbcSupport(DataSource dataSource)
    {
        _dataSource = dataSource;
    }

    // Properties
    //-------------------------------------------------------------------------                
    
    /**
     * Returns the data source used for communicating with the database.
     * 
     * @return The data source
     */
    public DataSource getDataSource()
    {
        return _dataSource;
    }

    /**
     * Sets the DataSource used for communicating with the database.
     * 
     * @param dataSource The data source
     */
    public void setDataSource(DataSource dataSource)
    {
        _dataSource = dataSource;
    }

    // Implementation methods    
    //-------------------------------------------------------------------------                

    /**
     * Returns a (new) JDBC connection from the data source.
     * 
     * @return The connection
     */
    public Connection borrowConnection() throws DynaSqlException
    {
        try
        {
            return getDataSource().getConnection();
        }
        catch (SQLException ex)
        {
            throw new DynaSqlException("Could not get a connection from the datasource", ex);
        }
    }
    
    /**
     * Closes the given JDBC connection (returns it back to the pool if the datasource is poolable).
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
            _log.warn("Caught exception while returning connection to pool", e);
        }
    }

    /**
     * Closes the given statement (which also closes all result sets for this statement) and the
     * connection it belongs to.
     * 
     * @param statement The statement
     */
    public void closeStatement(Statement statement)
    {
        if (statement != null)
        {
            try
            {
                Connection conn = statement.getConnection();

                if ((conn != null) && !conn.isClosed())
                {
                    statement.close();
                }
            }
            catch (Exception e)
            {
                _log.warn("Ignoring exception closing statement", e);
            }
        }
    }
}
