package org.apache.commons.sql;

/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 *
 * $Id$
 */
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.sql.io.DatabaseReader;
import org.apache.commons.sql.io.DatabaseWriter;
import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.Database;
import org.apache.commons.sql.model.ForeignKey;
import org.apache.commons.sql.model.Index;
import org.apache.commons.sql.model.IndexColumn;
import org.apache.commons.sql.model.Reference;
import org.apache.commons.sql.model.Table;

/**
 * Test harness for the IO package
 *
 * @author <a href="mailto:jason@zenplex.com">Jason van Zyl</a>
 * @version $Revision$
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
            
            assertTrue("More that one table should be found", 
                        database.getTables().size() > 1 );
            
            // Test our first table which is the 'book' table
            Table t1 = database.getTable(1);
            assertEquals("book", t1.getName());
            
            assertTrue("book table does not have primary", t1.hasPrimaryKey());

            Index idx1 = (Index)t1.getIndex(0);
            assertTrue("Did not find an index", idx1 != null);

            ForeignKey key = (ForeignKey) t1.getForeignKey(0);
            assertTrue("Did not find a foreign key", key != null);

            Column c0 = t1.getColumn(0);
            assertEquals("book_id", c0.getName());
            assertTrue("book_id should be required", c0.isRequired());
            assertTrue("book_id should be primary key", c0.isPrimaryKey());
            
            Column c1 = t1.getColumn(1);
            assertEquals("isbn", c1.getName());
            assertTrue("isbn should be required", c1.isRequired());
            assertTrue("isbn should not be primary key but is", 
                        ! c1.isPrimaryKey());

            List keyList1 = t1.getForeignKeys();
            assertEquals( "Foreign key count", 1, keyList1.size() );
            
            ForeignKey key0 = (ForeignKey) keyList1.get(0);
            assertEquals("foreignTable value correct", "author", 
                        key0.getForeignTable());
            
            List refList1 = key0.getReferences();
            assertEquals( "Reference count not correct", 1, refList1.size() );
            
            Reference r1 = (Reference) refList1.get(0);
            assertTrue("Could not find a reference", r1 != null);
                        
            assertEquals("local reference is incorrect", "author_id", 
                         r1.getLocal());
            assertEquals("foreign reference is incorrect", "author_id", 
                         r1.getForeign());
            
            List idxList = t1.getIndexes();
            assertEquals( "Index count", 1, idxList.size() );


            Index idx2 = (Index)idxList.get(0);
            assertTrue("Did not find an index", idx2 != null);
            assertEquals("Index name is incorrect", "book_isbn", idx2.getName());

            List idxColumns = idx2.getIndexColumns();
            IndexColumn idxColumn = (IndexColumn) idxColumns.get(0);
            assertTrue("Did not find an index column", idxColumn != null);
            assertEquals("Index column name is incorrect", "isbn", 
                          idxColumn.getName());

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

