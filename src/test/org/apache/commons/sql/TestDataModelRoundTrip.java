package org.apache.commons.sql;

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
import java.io.StringWriter;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.SimpleLog;

import org.apache.commons.sql.model.*;
import org.apache.commons.sql.io.*;

/**
 * Test harness for the IO package
 *
 * @author <a href="mailto:jason@zenplex.com">Jason van Zyl</a>
 * @version $Revision: 1.3 $
 */
public class TestDataModelRoundTrip
     extends TestCase
{
    private String TEST_DOCUMENT;

    /**
     * A unit test suite for JUnit
     */
    public static Test suite()
    {
        return new TestSuite(TestDataModelRoundTrip.class);
    }

    /**
     * Constructor for the TestDataModelRoundTrip object
     *
     * @param testName
     */
    public TestDataModelRoundTrip(String testName)
    {
        super(testName);
    }

    /**
     * The JUnit setup method
     */
    protected void setUp()
        throws Exception
    {
        super.setUp();
        String baseDir = System.getProperty("basedir");
        assertNotNull("The system property basedir was not defined.", baseDir);
        String fs = System.getProperty("file.separator");
        assertNotNull("The system property file.separator was not defined.", fs);
        TEST_DOCUMENT = baseDir + "/src/test-input/datamodel.xml";
    }

    /**
     * A unit test for JUnit
     */
    public void testDatabaseReader()
        throws Exception
    {

        DatabaseReader reader = new DatabaseReader();
        InputStream in = getXMLInput();

        try
        {
            Database database = (Database) reader.parse(in);
            assertTrue("Parsed a Database object", database != null);
            assertEquals("bookstore", database.getName());
            
            assertTrue("more than one table found", database.getTables().size() > 0 );
            
            // Test our first table which is the 'book' table
            Table t0 = database.getTable(0);
            assertEquals("book", t0.getName());
            
            Column c0 = t0.getColumn(0);
            assertEquals("book_id", c0.getName());
            assertTrue("book_id is required", c0.isRequired());
            assertTrue("book_id is primary key", c0.isPrimaryKey());
            
            Column c1 = t0.getColumn(1);
            assertEquals("isbn", c1.getName());
            assertTrue("isbn is required", c1.isRequired());
            assertTrue("isbn is not primary key", ! c1.isPrimaryKey());

            List keyList0 = t0.getForeignKeys();
            assertEquals( "Foreign key count", 1, keyList0.size() );
            
            ForeignKey key0 = (ForeignKey) keyList0.get(0);
            assertEquals("foreignTable is correct", "author", key0.getForeignTable());
            
            List refList0 = key0.getReferences();
            assertEquals( "Reference count", 1, refList0.size() );
            
            Reference r0 = (Reference) refList0.get(0);
            assertTrue("Found a reference", r0 != null);
                        
            assertEquals("local is correct", "author_id", r0.getLocal());
            assertEquals("foreign is correct", "author_id", r0.getForeign());
            
            // Write out the bean
            //writeBean(database);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }
    }

    /**
     * Description of the Method
     */
    public void writeBean(Object bean)
        throws Exception
    {
        StringWriter buffer = new StringWriter();
        DatabaseWriter writer = new DatabaseWriter(buffer);
        writer.write(bean);
        String text = buffer.toString();
    }

    /**
     * @return the bean class to use as the root
     */
    public Class getBeanClass()
    {
        return Database.class;
    }

    /**
     * Gets the xMLInput attribute of the TestDataModelRoundTrip object
     */
    protected InputStream getXMLInput()
        throws IOException
    {
        //return getClass().getResourceAsStream("datamodel.xml");
        return new FileInputStream(TEST_DOCUMENT);
    }
}

