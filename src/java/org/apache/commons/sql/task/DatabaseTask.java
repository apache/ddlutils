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

package org.apache.commons.sql.task;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.sql.io.JdbcModelReader;
import org.apache.commons.sql.model.Database;
import org.apache.commons.sql.util.DataSourceWrapper;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * This is an abstract class for tasks that need to access a database through
 * a connection.
 *
 * @author <a href="mailto:drfish@cox.net">J. Russell Smyth</a>
 * @author John Marshall/Connectria
 * @version $Id$
 */
public abstract class DatabaseTask extends Task
{
    /** JDBC URL. */
    protected String dbUrl;

    /** JDBC driver. */
    protected String dbDriver;

    /** JDBC user name. */
    protected String dbUser;

    /** JDBC password. */
    protected String dbPassword;

    /** DB catalog to use. */
    protected String dbCatalog;

    /** DB schema to use. */
    protected String dbSchema;

    public String getDbSchema()
    {
        return dbSchema;
    }

    public void setDbCatalog(String dbCatalog)
    {
        this.dbCatalog = dbCatalog;
    }

    public void setDbSchema(String dbSchema)
    {
        this.dbSchema = dbSchema;
    }

    public void setDbUrl(String v)
    {
        dbUrl = v;
    }

    public String getDbUrl()
    {
        return dbUrl;
    }

    public void setDbDriver(String v)
    {
        dbDriver = v;
    }

    public void setDbUser(String v)
    {
        dbUser = v;
    }

    public void setDbPassword(String v)
    {
        dbPassword = v;
    }

    /**
     * Prints the connection settings to stderr.
     */
    protected void printDbSettings() throws BuildException
    {
        System.err.println("Your DB settings are:");
        System.err.println("driver : " + dbDriver);
        System.err.println("URL : " + dbUrl);
        System.err.println("user : " + dbUser);
        System.err.println("password : " + dbPassword);
        System.err.println("catalog : " + dbCatalog);
        System.err.println("schema : " + dbSchema);
    }

    /**
     * Gets a Connection as specified
     * 
     * @return a Connection to the database
     * 
     * @throws ClassNotFoundException if dbDriver cannot be loaded
     * @throws SQLException if the database cannot be connected to
     */
    protected DataSource getDataSource() throws ClassNotFoundException, SQLException
    {
        // Load the database Driver.
        DataSourceWrapper wrapper = new DataSourceWrapper(dbDriver, dbUrl, dbUser, dbPassword);
        return wrapper;
    }

    /**
     * Retrievs the database specification from a connection
     * @param con The database connection
     * @return the Database schema
     * 
     * @throws SQLException if the schema cannot be read
     */
    protected Database getDbFromConnection(Connection con) throws SQLException
    {
        JdbcModelReader reader = new JdbcModelReader(con);
        if ( dbCatalog!=null ) {
            reader.setCatalog(dbCatalog);
        }
        if ( dbSchema!=null ) {
            reader.setSchema(dbSchema);
        }

        Database db = reader.getDatabase();
        return db;
    } 
}