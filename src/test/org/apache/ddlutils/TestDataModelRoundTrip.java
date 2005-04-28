package org.apache.ddlutils;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.ddlutils.io.DatabaseReader;
import org.apache.ddlutils.io.DatabaseWriter;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;

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

