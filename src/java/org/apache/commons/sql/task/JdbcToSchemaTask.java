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

package org.apache.commons.sql.task;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Hashtable;

import org.apache.commons.sql.io.DatabaseWriter;
import org.apache.commons.sql.io.JdbcModelReader;
import org.apache.commons.sql.model.Database;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * This class generates an XML schema of an existing database from
 * JDBC metadata.
 *
 * @author <a href="mailto:drfish@cox.net">J. Russell Smyth</a>
 * @version $Id: $
 */
public class JdbcToSchemaTask extends Task
{
    /** Name of database schema file produced. */
    protected String outputFile;

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

    /** Hashtable of columns that have primary keys. */
    protected Hashtable primaryKeys;

    /** Hashtable to track what table a column belongs to. */
    protected Hashtable columnTableMap;
    
    protected boolean useTypeNames = false;

    
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

    public void setOutputFile (String v)
    {
        outputFile = v;
    }
    public void setUseTypeNames(boolean b)
    {
        useTypeNames = b;
    }
        

    /**
     * Default constructor.
     */
    public void execute() throws BuildException
    {
        System.err.println("Commons-Sql JdbcToSchema starting\n");
        System.err.println("Your DB settings are:");
        System.err.println("driver : " + dbDriver);
        System.err.println("URL : " + dbUrl);
        System.err.println("user : " + dbUser);
        System.err.println("password : " + dbPassword);
        System.err.println("schema : " + dbSchema);

        Database db = null;
        try{
            db = getDbFromJdbc(); 
            DatabaseWriter w = new DatabaseWriter(new FileWriter(outputFile));
           // w.setWriteIDs(true);
            w.write(db);
            w.close();
        }catch(Exception e){
            System.out.println("exception during load:"+e.getMessage());
        }
    }

    /**
     */
    public Database getDbFromJdbc() throws Exception
    {
        // Load the database Driver.
        Class.forName(dbDriver);
        System.err.println("DB driver sucessfuly instantiated");

        // Attemtp to connect to a database.
        Connection con = DriverManager.getConnection(dbUrl,
                                                     dbUser,
                                                     dbPassword);
        System.err.println("DB connection established");

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