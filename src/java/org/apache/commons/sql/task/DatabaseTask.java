/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons-sandbox//sql/src/java/org/apache/commons/sql/task/DatabaseTask.java,v 1.1 2003/12/16 16:03:07 matth Exp $
 * $Revision: 1.1 $
 * $Date: 2003/12/16 16:03:07 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
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
 */

package org.apache.commons.sql.task;

import java.sql.Connection;
import java.sql.DriverManager;
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
 * @version $Id: DatabaseTask.java,v 1.1 2003/12/16 16:03:07 matth Exp $
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