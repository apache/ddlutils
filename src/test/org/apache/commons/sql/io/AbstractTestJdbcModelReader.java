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
package org.apache.commons.sql.io;

import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Iterator;

import javax.sql.DataSource;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.sql.builder.SqlBuilder;
import org.apache.commons.sql.builder.SqlBuilderFactory;
import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.Database;
import org.apache.commons.sql.model.Table;
import org.apache.commons.sql.util.DDLExecutor;
import org.apache.commons.sql.util.DataSourceWrapper;

/**
 * Abstract base class for testing the JdbcModelReader against a number of
 * different databases.  Based on AbstractTestDynaSql.  This requires that the
 * SqlBuilder code be functioning to dynamically create the database so we can test it!
 *
 * @author <a href="mailto:dep4b@yahoo.com">Eric Pugh</a>
 * @version $Revision: 1.3 $
 */
public abstract class AbstractTestJdbcModelReader extends TestCase {
    /** The Log to which logging calls will be made. */
    private static final Log log =
        LogFactory.getLog(AbstractTestJdbcModelReader.class);

    private String baseDir;

    /** the database model to create*/
    private Database database;

    /** the database model to test*/
    private Database testDatabase;

    /** the database to connect to */
    private DataSource dataSource;

    /** for building the Metadata to create the database */
    private SqlBuilder sqlBuilder;

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    /**
     * A unit test suite for JUnit
     */
    public static Test suite() {
        return new TestSuite(AbstractTestJdbcModelReader.class);
    }

    /**
     * Constructor for the AbstractTestJdbcModelReader object
     *
     * @param testName
     */
    public AbstractTestJdbcModelReader(String testName) {
        super(testName);
    }

    // Test cases
    //-------------------------------------------------------------------------

    /**
     * Parse the databases metadata.
     */
    public void testParseMetadata() throws Exception {
        // first load up the metadata for this database.

        Connection connection = getConnection();
        try {

            JdbcModelReader modelReader = new JdbcModelReader(connection);
    
            testDatabase = modelReader.getDatabase();
        }
        finally {
            try {
                connection.close();
            }
            catch (Exception e) {
                log.error("Caught exception closing connection: " + e, e);
            }
        }

        assertTrue(
            "Src Database contains a table 'author'",
            database.findTable("author") != null);
        assertTrue(
            "Test Database contains a table 'book'",
            testDatabase.findTable("book") != null);

        assertTrue(
            "Test Database contains correct number of tables",
            testDatabase.getTables().size() == database.getTables().size());

        for (Iterator i = testDatabase.getTables().iterator(); i.hasNext();) {
            Table testTable = (Table) i.next();
            Table srcTable = database.findTable(testTable.getName());
            assertTrue("srcTable was found", srcTable != null);
            assertTrue(
                "srcTable matches testTable",
                srcTable.getName().equalsIgnoreCase(testTable.getName()));
            assertTrue(
                "Table columns match",
                testTable.getColumns().size() == srcTable.getColumns().size());
            //todo: not imple
            //assertTrue( "Table indexes match", testTable.getIndexes().size() == srcTable.getIndexes().size() );

            doImportForeignKeys(srcTable, testTable);
            doImportPrimaryKeyColumns(srcTable, testTable);

        }

        Table authorTable = testDatabase.findTable("author");
        assertTrue("found author table",authorTable!=null);
        Column authorColumn = authorTable.findColumn("organisation");
        assertTrue("found organisation column", authorColumn!=null);
        
        assertTrue("organisation column is not a primary key", !authorColumn.isPrimaryKey());
        assertTrue("organisation column is not auto inc", !authorColumn.isAutoIncrement());
        assertTrue("organisation column is not required", !authorColumn.isRequired());


        Table bookTable = testDatabase.findTable("book");
        Column bookColumn = bookTable.findColumn("book_id");
        
        if (supportsPrimaryKeyMetadata()) {
            assertTrue("book_id column is primary key", bookColumn.isPrimaryKey());
        }            
        
        if (supportsAutoIncrement()) {
            assertTrue("book_id column is auto inc", bookColumn.isAutoIncrement());
        }
            
        /** @todo uncomment when this works!        
         * 
        assertTrue("book_id column is required", bookColumn.isRequired());
        */
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * @return true if this database test case should test for primary key meta data
     */
    protected boolean supportsPrimaryKeyMetadata() {
        return true;
    }
    
    /**
     * @return true if we should test for auto-increment columns
     */
    protected boolean supportsAutoIncrement() {
        return false;
    }
    
    public abstract void doImportForeignKeys(Table srcTable, Table testTable);
    public abstract void doImportPrimaryKeyColumns(
        Table srcTable,
        Table testTable);

    /**
     * The JUnit setup method
     */
    protected void setUp() throws Exception {
        super.setUp();

        baseDir = System.getProperty("basedir", ".");
        String uri = baseDir + "/src/test-input/datamodel.xml";

        DatabaseReader reader = new DatabaseReader();
        database = (Database) reader.parse(new FileInputStream(uri));

        assertTrue("Loaded a valid database", database != null);

        dataSource = createDataSource();
        sqlBuilder = createSqlBuilder();

        executeDDL();

    }

    /**
     * @return the database connection to be used for the tests
     */
    protected Connection getConnection() throws Exception {
        return dataSource.getConnection();
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
    protected SqlBuilder createSqlBuilder() throws Exception {
        return SqlBuilderFactory.newSqlBuilder(getDatabaseType());
    }

    /**
     * Creates the database on the given data source with the given SQL builder
     */
    protected void executeDDL() throws Exception {
        DDLExecutor executor = new DDLExecutor(dataSource, sqlBuilder);
        executor.createDatabase(database, true);
    }

    /**
     * Creates a new DataSource for the given JDBC URI
     */
    protected DataSource createDataSource(String className, String connectURL)
        throws Exception {
        return createDataSource(className, connectURL, null, null);
    }

    /**
     * Creates a new DataSource for the given JDBC URI
     */
    protected DataSource createDataSource(
        String className,
        String connectURL,
        String userName,
        String password)
        throws Exception {

        DataSourceWrapper wrapper = new DataSourceWrapper();
        wrapper.setDriverClassName(className);
        wrapper.setJdbcURL(connectURL);
        wrapper.setUserName(userName);
        wrapper.setPassword(password);
        return wrapper;
    }
}
