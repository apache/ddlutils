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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>A simple <code>DataSource</code> wrapper for the standard
 * <code>DriverManager</code> class.
 * 
 * @author Hans Bergsten
 */
public class DataSourceWrapper implements DataSource {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(DataSourceWrapper.class);

    private String driverClassName;
    private String jdbcURL;
    private String userName;
    private String password;

    /**
     * Default constructor
     */
    public DataSourceWrapper() {
    }

    public DataSourceWrapper(String driverClassName, String jdbcURL)
        throws ClassNotFoundException {

        this(driverClassName, jdbcURL, null, null);
    }

    public DataSourceWrapper(String driverClassName, String jdbcURL, String userName, String password)
        throws ClassNotFoundException {

        setDriverClassName(driverClassName);
        setJdbcURL(jdbcURL);
        setUserName(userName);
        setPassword(password);
    }

    public void setDriverClassName(String driverClassName)
        throws ClassNotFoundException {

        if (log.isDebugEnabled()) {
            log.debug("Loading JDBC driver: [" + driverClassName + "]");
        }

        this.driverClassName = driverClassName;
//changed to get rid of instantiation and access exceptions
//        getClass().getClassLoader().loadClass(driverClassName).newInstance();
        Class c = Class.forName(driverClassName, true, getClass().getClassLoader());
    }

    public void setJdbcURL(String jdbcURL) {
        this.jdbcURL = jdbcURL;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns a Connection using the DriverManager and all
     * set properties.
     */
    public Connection getConnection() throws SQLException {
        Connection conn = null;
        if (userName != null) {
            if (log.isDebugEnabled()) {
                log.debug(
                    "Creating connection from url: " + jdbcURL + " userName: " + userName);
            }

            conn = DriverManager.getConnection(jdbcURL, userName, password);
        }
        else {
            if (log.isDebugEnabled()) {
                log.debug("Creating connection from url: " + jdbcURL);
            }

            conn = DriverManager.getConnection(jdbcURL);
        }
        if (log.isDebugEnabled()) {
            log.debug(
                "Created connection: " + conn );
        }
        return conn;
    }

    /**
     * Always throws a SQLException. Username and password are set
     * in the constructor and can not be changed.
     */
    public Connection getConnection(String username, String password)
        throws SQLException {
        throw new SQLException("Not Supported");
    }

    /**
     * Always throws a SQLException. Not supported.
     */
    public int getLoginTimeout() throws SQLException {
        throw new SQLException("Not Supported");
    }

    /**
     * Always throws a SQLException. Not supported.
     */
    public PrintWriter getLogWriter() throws SQLException {
        throw new SQLException("Not Supported");
    }

    /**
     * Always throws a SQLException. Not supported.
     */
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new SQLException("Not Supported");
    }

    /**
     * Always throws a SQLException. Not supported.
     */
    public synchronized void setLogWriter(PrintWriter out) throws SQLException {
        throw new SQLException("Not Supported");
    }

}
