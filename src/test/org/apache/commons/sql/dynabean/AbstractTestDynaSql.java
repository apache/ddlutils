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

import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.sql.builder.*;
import org.apache.commons.sql.dynabean.DynaSql;
import org.apache.commons.sql.io.DatabaseReader;
import org.apache.commons.sql.model.*;
import org.apache.commons.sql.util.DataSourceWrapper;
import org.apache.commons.sql.util.DDLExecutor;

/**
 * Abstract base class for testing the DynaSql against a number of 
 * different databases
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.3 $
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
        
        dynaSql = new DynaSql(dataSource, database);
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

