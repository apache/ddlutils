package org.apache.commons.sql.builder;

/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 *
 * $Id: TestProjectRoundTrip.java,v 1.3 2002/03/10 20:16:03 jvanzyl Exp $
 */
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.SimpleLog;

import org.apache.commons.sql.builder.*;
import org.apache.commons.sql.model.*;
import org.apache.commons.sql.io.DatabaseReader;

/**
 * Test harness for the SqlBuilder for various databases.
 *
 * @version $Revision: 1.3 $
 */
public class TestBuilder extends TestCase
{
    private Database database;
    private String baseDir;
 
    /**
     * A unit test suite for JUnit
     */
    public static Test suite()
    {
        return new TestSuite(TestBuilder.class);
    }

    /**
     * Constructor for the TestBuilder object
     *
     * @param testName
     */
    public TestBuilder(String testName)
    {
        super(testName);
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
    }

    /**
     * A unit test for JUnit
     */
    public void testBuilders()
        throws Exception
    {
        testBuilder( new SybaseBuilder(), "sybase.sql" );
        testBuilder( new OracleBuilder(), "oracle.sql" );
        testBuilder( new MySqlBuilder(), "mysql.sql" );
        testBuilder( new MSSqlBuilder(), "mssql.sql" );        
    }
    
    protected void testBuilder(SqlBuilder builder, String fileName) throws Exception 
    {
        String name = baseDir + "/target/" + fileName;
        
        FileWriter writer = new FileWriter( name );
        builder.setWriter( writer );
        builder.createDatabase( database );
        writer.close();
    }
}

