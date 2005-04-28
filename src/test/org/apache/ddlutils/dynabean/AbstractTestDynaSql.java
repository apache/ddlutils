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

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.builder.SqlBuilder;
import org.apache.ddlutils.builder.SqlBuilderFactory;
import org.apache.ddlutils.dynabean.DynaSql;
import org.apache.ddlutils.io.DatabaseReader;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.util.DDLExecutor;
import org.apache.ddlutils.util.DataSourceWrapper;

/**
 * Abstract base class for testing the DynaSql against a number of 
 * different databases
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision$
 */
public abstract class AbstractTestDynaSql extends TestCase
{
    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog( AbstractTestDynaSql.class );
    
    private String baseDir;
    
    /** the database model */
    private Database database;
    
    /** the database to connect to */
    private DataSource dataSource;
    
    /** for building the DDL to create the database */
    private SqlBuilder sqlBuilder;

    /** the simple API to the database */
    protected DynaSql dynaSql;
    
 
    public static void main( String[] args ) 
    {
        TestRunner.run( suite() );
    }
    
    /**
     * A unit test suite for JUnit
     */
    public static Test suite()
    {
        return new TestSuite(AbstractTestDynaSql.class);
    }

    /**
     * Constructor for the AbstractTestDynaSql object
     *
     * @param testName
     */
    public AbstractTestDynaSql(String testName)
    {
        super(testName);
    }

    // Test cases
    //-------------------------------------------------------------------------                

    /**
     * Insert some data
     */    
    public void testInsert() throws Exception 
    {
        // first lets check that the tables are available in our database
        
        assertTrue( "Database contains a table 'author'", database.findTable("author") != null );
        assertTrue( "Database contains a table 'book'", database.findTable("book") != null );
        
        DynaBean author = dynaSql.newInstance("author");

        assertTrue("Found an author", author != null);
        
        author.set("author_id", new Integer(1));
        author.set("name", "Oscar Wilde");
        dynaSql.insert(author);        

        log.info( "Inserted author: " + author );

        author = dynaSql.newInstance("author");
        author.set("author_id", new Integer(2));
        author.set("name", "Ian Rankin");
        dynaSql.insert(author);
        
        log.info( "Inserted author: " + author );
        
        DynaBean book = dynaSql.newInstance("book");
        
        assertTrue("Found an book", book != null);
        
        book.set("author_id", new Integer(1));
        book.set("isbn", "ISBN-ABCDEF");
        book.set("title", "The Importance of being Earnest");
        dynaSql.insert(book);
        log.info( "Inserted book: " + book );
        
        book = dynaSql.newInstance("book");
        book.set("author_id", new Integer(2));
        book.set("isbn", "ISBN-XYZ");
        book.set("title", "The Hanging Garden");
        dynaSql.insert(book);
        
        log.info( "Inserted book: " + book );
        
        
        // now lets do some queries        
        doQuery();
        doQueryWithParameters();
    }



    // Implementation methods
    //-------------------------------------------------------------------------                


    /**
     * Test out some basic query operations
     */
    protected void doQuery() throws Exception {
        Iterator iter = dynaSql.query( "select * from book" );
        assertTrue("Found at least one row", iter.hasNext());

        DynaBean bean = (DynaBean) iter.next();
       
        assertTrue("Found a dynaBean row", bean != null);
        
        log.info( "Found book: " + bean.get("title") );
        
        assertEquals( "bean has corrrect isbn", "ISBN-ABCDEF", bean.get("isbn") );
        assertEquals( "bean has corrrect title", "The Importance of being Earnest", bean.get("title") );
    }

    /**
     * Test out some queries with parameters
     */
    protected void doQueryWithParameters() throws Exception {
        List params = new ArrayList();
        params.add("The Hanging Garden");
        
        Iterator iter = dynaSql.query( "select * from book where title = ?", params );
        assertTrue("Found at least one row", iter.hasNext());

        DynaBean bean = (DynaBean) iter.next();

        assertTrue("Found a dynaBean row", bean != null);
        
        log.info( "Found book: " + bean.get("title") );
               
        assertEquals( "bean has corrrect isbn", "ISBN-XYZ", bean.get("isbn") );
        assertEquals( "bean has corrrect title", "The Hanging Garden", bean.get("title") );
    }

    /**
     * The JUnit setup method
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        baseDir = System.getProperty("basedir", ".");
        String uri = baseDir + "/src/test-input/datamodel.xml";
        
        DatabaseReader reader = new DatabaseReader ();
        database = (Database) reader.parse(new FileInputStream(uri));
        
        assertTrue("Loaded a valid database", database != null);
        
        dataSource = createDataSource();
        sqlBuilder = createSqlBuilder();
        
        executeDDL();
        
        dynaSql = new DynaSql(sqlBuilder, dataSource, database);
    }

    /**
     * @return the name of the database type to use to create the DDL
     */
    protected abstract String getDatabaseType();    
    
    /**
     * Factory method to create a DataSource
     */
    protected abstract DataSource createDataSource() throws Exception;

    /**
     * Creates an SqlBuilder based on the name of the database
     */
    protected SqlBuilder createSqlBuilder() throws Exception
    {
        return SqlBuilderFactory.newSqlBuilder(getDatabaseType());
    }

    /**
     * Creates the database on the given data source with the given SQL builder
     */    
    protected void executeDDL() throws Exception 
    {
        DDLExecutor executor = new DDLExecutor(dataSource, sqlBuilder);
        executor.createDatabase(database, true);
    }
    
    /**
     * Creates a new DataSource for the given JDBC URI
     */    
    protected DataSource createDataSource(String className, String connectURL) throws Exception 
    {
        return createDataSource(className, connectURL, null, null);
    }
    
    /**
     * Creates a new DataSource for the given JDBC URI
     */    
    protected DataSource createDataSource(String className, String connectURL, String userName, String password) throws Exception 
    {
        DataSourceWrapper wrapper = new DataSourceWrapper();
        wrapper.setDriverClassName(className);
        wrapper.setJdbcURL(connectURL);
        wrapper.setUserName(userName);
        wrapper.setPassword(password);
        return wrapper;
    }
}

