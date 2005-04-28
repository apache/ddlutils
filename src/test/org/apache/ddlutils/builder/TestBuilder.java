package org.apache.ddlutils.builder;

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
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.Writer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.ddlutils.builder.AxionBuilder;
import org.apache.ddlutils.builder.HsqlDbBuilder;
import org.apache.ddlutils.builder.MSSqlBuilder;
import org.apache.ddlutils.builder.MySqlBuilder;
import org.apache.ddlutils.builder.OracleBuilder;
import org.apache.ddlutils.builder.PostgreSqlBuilder;
import org.apache.ddlutils.builder.SqlBuilder;
import org.apache.ddlutils.builder.SybaseBuilder;
import org.apache.ddlutils.io.DatabaseReader;
import org.apache.ddlutils.model.Database;

/**
 * Test harness for the SqlBuilder for various databases.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision$
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

        testBuilder( new AxionBuilder(), "axion.sql" );
        testBuilder( new HsqlDbBuilder(), "hsqldb.sql" );
        testBuilder( new MSSqlBuilder(), "mssql.sql" );        
        testBuilder( new MySqlBuilder(), "mysql.sql" );
        testBuilder( new OracleBuilder(), "oracle.sql" );
        testBuilder( new PostgreSqlBuilder(), "postgres.sql" );
        testBuilder( new SybaseBuilder(), "sybase.sql" );

    }
    
    /**
     * A unit test for JUnit
     */
    public void testBaseBuilder()
        throws Exception
    {
    
        SqlBuilder builder = new HsqlDbBuilder();
        StringWriter sw = new StringWriter();
        builder.setWriter(sw);
        builder.dropDatabase(database);       

        String drop = sw.toString();
        int bookIdx = drop.indexOf("drop table book");
        int authIdx = drop.indexOf("drop table author");

        assertTrue("dropDatabase Failed to create proper drop statement for " +
                    "book table. Here is the statment created:\n" + drop, 
                    bookIdx > 0);
        
        assertTrue("dropDatabase Failed to create proper drop statement for " +
                    "author table. Here is the statment created:\n" + drop,
                     authIdx > 0);

        Writer wr = builder.getWriter();
        assertTrue("Couldnt find writer", wr != null);  


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

