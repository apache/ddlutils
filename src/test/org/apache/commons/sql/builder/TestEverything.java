package org.apache.commons.sql.builder;

/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 *
 * $Id: TestEverything.java,v 1.3 2004/08/14 19:51:38 tomdz Exp $
 */
import java.beans.IntrospectionException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.Properties;

import javax.sql.DataSource;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.sql.dynabean.DynaSql;
import org.apache.commons.sql.io.DatabaseReader;
import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.Database;
import org.apache.commons.sql.model.Table;
import org.apache.commons.sql.util.DDLExecutor;
import org.apache.commons.sql.util.DataSourceWrapper;
import org.xml.sax.SAXException;

/**
 * Test harness for the SqlBuilder for various databases.
 * Tests: create table, create index, drop index, pk create, add column,
 * drop column, modify column, drop table, default values, unique indexes
 *
 * @author John Marshall/Connectria
 * @version $Revision: 1.3 $
 */
public class TestEverything extends TestCase
{
    private String baseDir;
    private Properties props;
    private DataSource dataSource;

    /**
     * A unit test suite for JUnit
     */
    public static Test suite()
    {
        return new TestSuite(TestEverything.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestEverything.class);
    }

    /**
     * Constructor for the TestEverything object
     *
     * @param testName
     */
    public TestEverything(String testName)
    {
        super(testName);
    }

    /**
     * The JUnit setup method
     */

     // TODO: Uncomment after other tests are added
    /*
    protected void setUp() throws Exception
    {
        super.setUp();

        baseDir = System.getProperty("basedir", ".");
        String uri = baseDir + "/src/test-input/test.properties";

        props = new Properties();
        props.load( new FileInputStream(uri) );

        assertTrue("Loaded valid properties", !props.isEmpty() );

        dataSource = getDataSource();
    }
    */

    /**
     * Gets a DataSource from the configuration in properties
     */
    private DataSource getDataSource() throws ClassNotFoundException {
        String driver = props.getProperty( "dbDriver" );
        String url = props.getProperty( "dbUrl" );
        String user = props.getProperty( "dbUser" );
        String password = props.getProperty( "dbPassword" );

        return new DataSourceWrapper( driver, url, user, password );
    }

    /**
     * Gets a Database from the filesystem
     */
    private Database getDatabase( String name ) throws IntrospectionException, FileNotFoundException, SAXException, IOException {
        String uri = baseDir + "/src/test-input/" + name + ".xml";

        DatabaseReader reader = new DatabaseReader();
        Database database = (Database) reader.parse( new FileInputStream(uri) );
        assertTrue("Loaded a valid database", database != null);

        return database;
    }

    // TODO: Remove after other tests have been added.
    // This class needs at least one test or it fails
    public void testFake() {}

    /**
     * A unit test for JUnit
     */
    public void XtestEverything()
        throws Exception
    {
        // TODO: Reinsert test after env has been configured
        String tableName = "test_table";

        Integer testId = new Integer(1);
        String testName = "UniqueName";

        Database db = getDatabase( "test-start" );
        assertEquals( "Correct version", "1.0", db.getVersion() );

        updateDatabase( db, true );
        Table table = db.findTable( tableName );

        DynaSql dynaSql = new DynaSql(SqlBuilderFactory.newSqlBuilder("hsqldb"), dataSource, db );
        DynaBean test = dynaSql.newInstance(tableName);

        assertTrue("Test not null", test != null);

        test.set("id", testId);
        test.set("name", testName);
        dynaSql.insert(test);

        //test inserted
        //test default value
        Iterator iter = dynaSql.query( "select * from " + tableName + " where id = 1" );
        assertTrue("Found at least one row", iter.hasNext());

        test = (DynaBean) iter.next();

        assertTrue("Found a dynaBean row", test != null);

        assertEquals( "bean has correct id", testId, test.get("id") );
        assertEquals( "bean has correct name", testName, test.get("name") );
//dynasql forces null into column
//        assertEquals( "bean has correct default number", new Integer(25), test.get("number") );

        //test unique index
        test = dynaSql.newInstance(tableName);
        test.set("id", new Integer(2));
        test.set("name", testName);
        boolean insertError = false;
        try {
            dynaSql.insert(test);
        } catch ( SQLException e ) {
            insertError = true;
        }

        assertTrue( "Unique index violation on insert", insertError );

        //test pk
        test = dynaSql.newInstance(tableName);
        test.set("id", testId);
        test.set("name", "another name");
        insertError = false;
        try {
            dynaSql.insert(test);
        } catch ( SQLException e ) {
            insertError = true;
        }

        assertTrue( "PK violation on insert", insertError );


        //check adding column with default
        Column newCol = new Column("defaulted", "defaulted", Types.INTEGER, "11", true, false, false, "50");
        table.addColumn( newCol );
        updateDatabase( db, false );


        iter = dynaSql.query( "select * from " + tableName + " where id = 1" );
        assertTrue("Found at least one row", iter.hasNext());
        test = (DynaBean) iter.next();
        assertTrue("Found a dynaBean row", test != null);
        assertEquals( "bean has correct id", testId, test.get("id") );
        assertEquals( "bean has correct name", testName, test.get("name") );
        assertEquals( "bean has correct default number", new Integer(50), test.get("defaulted") );

        //change column type
        table.getColumns().remove( newCol );
        newCol.setTypeCode( Types.VARCHAR );
        table.addColumn( newCol );
        updateDatabase( db, false );

        iter = dynaSql.query( "select * from " + tableName + " where id = 1" );
        assertTrue("Found at least one row", iter.hasNext());
        test = (DynaBean) iter.next();
        assertTrue("Found a dynaBean row", test != null);
        assertEquals( "bean has correct id", testId, test.get("id") );
        assertEquals( "bean has correct name", testName, test.get("name") );
        //the following statement does fail with new Integer(50) as the expected value, so type check is right
        assertEquals( "bean has correct type", "50", test.get("defaulted") );


        //try drop column
        table.getColumns().remove( newCol );
        updateDatabase( db, false );

        iter = dynaSql.query( "select * from " + tableName + " where id = 1" );
        assertTrue("Found at least one row", iter.hasNext());
        test = (DynaBean) iter.next();
        assertTrue("Found a dynaBean row", test != null);
        assertEquals( "bean has correct id", testId, test.get("id") );
        assertEquals( "bean has correct name", testName, test.get("name") );
        assertTrue( "bean has no property", test.getDynaClass().getDynaProperty("defaulted") == null );


        //try drop index
        table.getIndexes().clear();
        assertEquals( "No table indexes", 0, table.getIndexes().size() );
        updateDatabase( db, false );
//org.apache.commons.sql.io.DatabaseWriter writer = new org.apache.commons.sql.io.DatabaseWriter(System.err);
//writer.write(db);

        test = dynaSql.newInstance(tableName);
        test.set("id", new Integer(5));
        test.set("name", testName);
        insertError = false;
        try {
            dynaSql.insert(test);
        } catch ( SQLException e ) {
            e.printStackTrace();
            insertError = true;
        }

        assertFalse( "No Unique index violation on insert", insertError );

        //try drop table
        db.getTables().remove(table);
        updateDatabase( db, false );
        boolean error = false;
        try {
            iter = dynaSql.query( "select * from " + tableName + " where id = 1" );
        } catch ( SQLException e ) {
            error = true;
        }

        assertTrue( "Had no table error", error );
    }

    /**
     * Updates the current database to match the desired schema
     *
     * @param create true if drop/create, false if alter
     */
    private void updateDatabase( Database db, boolean create ) throws InstantiationException, SQLException, IllegalAccessException, IOException {
        StringWriter writer = new StringWriter();
        SqlBuilder builder = SqlBuilderFactory.newSqlBuilder(props.getProperty( "dbType" ) );
        builder.setWriter(writer);
        if ( create ) {
            builder.createDatabase( db, true );
        } else {
            builder.alterDatabase( db, dataSource.getConnection(), true, true);
        }

        System.err.println( writer.toString() );

        DDLExecutor exec = new DDLExecutor(dataSource);
        exec.setContinueOnError(true);
        exec.evaluateBatch(writer.toString());
    }
}

