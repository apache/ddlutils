package org.apache.ddlutils.io;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.dynabean.SqlDynaBean;
import org.apache.ddlutils.model.Database;

/**
 * Tests the {@link org.apache.ddlutils.io.DataReader} and {@link org.apache.ddlutils.io.DataWriter} classes.
 * 
 * @version $Revision: 289996 $
 */
public class TestDataReaderAndWriter extends TestCase
{
    // no need to call start/end as the don't do anything anyways
    private static class TestDataSink implements DataSink
    {
        private final ArrayList readObjects;

        private TestDataSink(ArrayList readObjects)
        {
            this.readObjects = readObjects;
        }

        public void start() throws DataSinkException
        {}

        public void addBean(DynaBean bean) throws DataSinkException
        {
            readObjects.add(bean);
        }

        public void end() throws DataSinkException
        {}
    }

    /**
     * Tests reading the data from XML.
     */
    public void testRead() throws Exception
    {
        final String testSchemaXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='bookstore'>\n"+
            "  <table name='author'>\n"+
            "    <column name='author_id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='name' type='VARCHAR' size='50' required='true'/>\n"+
            "    <column name='organisation' type='VARCHAR' size='50' required='false'/>\n"+
            "  </table>\n"+
            "  <table name='book'>\n"+
            "    <column name='book_id' type='INTEGER' required='true' primaryKey='true' autoIncrement='true'/>\n"+
            "    <column name='isbn' type='VARCHAR' size='15' required='true'/>\n"+
            "    <column name='author_id' type='INTEGER' required='true'/>\n"+
            "    <column name='title' type='VARCHAR' size='255' default='N/A' required='true'/>\n"+
            "    <column name='issue_date' type='DATE' required='false'/>\n"+
            "    <foreign-key foreignTable='author'>\n"+
            "      <reference local='author_id' foreign='author_id'/>\n"+
            "    </foreign-key>\n"+
            "    <index name='book_isbn'>\n"+
            "      <index-column name='isbn'/>\n"+
            "    </index>\n"+
            "  </table>\n"+
            "</database>";
        final String testDataXml =
            "<data>\n"+
            "  <author author_id='1' name='Ernest Hemingway'/>\n"+
            "  <author author_id='2' name='William Shakespeare'/>\n"+
            "  <book book_id='1' author_id='1'>\n"+
            "    <isbn>0684830493</isbn>\n"+
            "    <title>Old Man And The Sea</title>\n"+
            "    <issue_date>1952</issue_date>\n"+
            "  </book>\n"+
            "  <book book_id='2' author_id='2'>\n"+
            "    <isbn>0198321465</isbn>\n"+
            "    <title>Macbeth</title>\n"+
            "    <issue_date>1606</issue_date>\n"+
            "  </book>\n"+
            "  <book book_id='3' author_id='2'>\n"+
            "    <isbn>0140707026</isbn>\n"+
            "    <title>A Midsummer Night's Dream</title>\n"+
            "    <issue_date>1595</issue_date>\n"+
            "  </book>\n"+
            "</data>";

        DatabaseIO modelReader = new DatabaseIO();

        modelReader.setValidateXml(true);
        
        Database        model       = modelReader.read(new StringReader(testSchemaXml));
        final ArrayList readObjects = new ArrayList();
        DataReader      dataReader  = new DataReader();

        dataReader.setModel(model);
        dataReader.setSink(new TestDataSink(readObjects));
        dataReader.read(new StringReader(testDataXml));

        assertEquals(5, readObjects.size());

        DynaBean obj1 = (DynaBean)readObjects.get(0);
        DynaBean obj2 = (DynaBean)readObjects.get(1);
        DynaBean obj3 = (DynaBean)readObjects.get(2);
        DynaBean obj4 = (DynaBean)readObjects.get(3);
        DynaBean obj5 = (DynaBean)readObjects.get(4);

        assertEquals("author",
                     obj1.getDynaClass().getName());
        assertEquals("1",
                     obj1.get("author_id").toString());
        assertEquals("Ernest Hemingway",
                     obj1.get("name").toString());
        assertEquals("author",
                     obj2.getDynaClass().getName());
        assertEquals("2",
                     obj2.get("author_id").toString());
        assertEquals("William Shakespeare",
                     obj2.get("name").toString());
        assertEquals("book",
                     obj3.getDynaClass().getName());
        assertEquals("1",
                     obj3.get("book_id").toString());
        assertEquals("1",
                     obj3.get("author_id").toString());
        assertEquals("0684830493",
                     obj3.get("isbn").toString());
        assertEquals("Old Man And The Sea",
                     obj3.get("title").toString());
        assertEquals("1952-01-01",
                     obj3.get("issue_date").toString());    // parsed as a java.sql.Date
        assertEquals("book",
                     obj4.getDynaClass().getName());
        assertEquals("2",
                     obj4.get("book_id").toString());
        assertEquals("2",
                     obj4.get("author_id").toString());
        assertEquals("0198321465",
                     obj4.get("isbn").toString());
        assertEquals("Macbeth",
                     obj4.get("title").toString());
        assertEquals("1606-01-01",
                     obj4.get("issue_date").toString());    // parsed as a java.sql.Date
        assertEquals("book",
                     obj5.getDynaClass().getName());
        assertEquals("3",
                     obj5.get("book_id").toString());
        assertEquals("2",
                     obj5.get("author_id").toString());
        assertEquals("0140707026",
                     obj5.get("isbn").toString());
        assertEquals("A Midsummer Night's Dream",
                     obj5.get("title").toString());
        assertEquals("1595-01-01",
                     obj5.get("issue_date").toString());    // parsed as a java.sql.Date
    }

    /**
     * Tests reading the data from a file via the {#link {@link DataReader#read(String)} method.
     */
    public void testReadFromFile1() throws Exception
    {
        final String testSchemaXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String testDataXml =
            "<data>\n"+
            "  <test id='1' value='foo'/>\n"+
            "</data>";

        File tmpFile = File.createTempFile("data", ".xml");

        try
        {
            Writer writer = new BufferedWriter(new FileWriter(tmpFile));

            writer.write(testDataXml);
            writer.close();

            DatabaseIO modelReader = new DatabaseIO();

            modelReader.setValidateXml(true);
            
            Database        model       = modelReader.read(new StringReader(testSchemaXml));
            final ArrayList readObjects = new ArrayList();
            DataReader      dataReader  = new DataReader();
    
            dataReader.setModel(model);
            dataReader.setSink(new TestDataSink(readObjects));
            dataReader.read(tmpFile.getAbsolutePath());
    
            assertEquals(1, readObjects.size());
    
            DynaBean obj = (DynaBean)readObjects.get(0);
    
            assertEquals("test",
                         obj.getDynaClass().getName());
            assertEquals("1",
                         obj.get("id").toString());
            assertEquals("foo",
                         obj.get("value").toString());
        }
        finally
        {
            tmpFile.delete();
        }
    }

    /**
     * Tests reading the data from a file via the {#link {@link DataReader#read(File)} method.
     */
    public void testReadFromFile2() throws Exception
    {
        final String testSchemaXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String testDataXml =
            "<data>\n"+
            "  <test id='1' value='foo'/>\n"+
            "</data>";

        File tmpFile = File.createTempFile("data", ".xml");

        try
        {
            Writer writer = new BufferedWriter(new FileWriter(tmpFile));

            writer.write(testDataXml);
            writer.close();

            DatabaseIO modelReader = new DatabaseIO();

            modelReader.setValidateXml(true);
            
            Database        model       = modelReader.read(new StringReader(testSchemaXml));
            final ArrayList readObjects = new ArrayList();
            DataReader      dataReader  = new DataReader();
    
            dataReader.setModel(model);
            dataReader.setSink(new TestDataSink(readObjects));
            dataReader.read(tmpFile);
    
            assertEquals(1, readObjects.size());
    
            DynaBean obj = (DynaBean)readObjects.get(0);
    
            assertEquals("test",
                         obj.getDynaClass().getName());
            assertEquals("1",
                         obj.get("id").toString());
            assertEquals("foo",
                         obj.get("value").toString());
        }
        finally
        {
            tmpFile.delete();
        }
    }

    /**
     * Tests reading the data from a file via the {#link {@link DataReader#read(java.io.InputStream)} method.
     */
    public void testReadFromFile3() throws Exception
    {
        final String testSchemaXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String testDataXml =
            "<data>\n"+
            "  <test id='1' value='foo'/>\n"+
            "</data>";

        File tmpFile = File.createTempFile("data", ".xml");

        try
        {
            Writer writer = new BufferedWriter(new FileWriter(tmpFile));

            writer.write(testDataXml);
            writer.close();

            DatabaseIO modelReader = new DatabaseIO();

            modelReader.setValidateXml(true);
            
            Database        model       = modelReader.read(new StringReader(testSchemaXml));
            final ArrayList readObjects = new ArrayList();
            DataReader      dataReader  = new DataReader();
    
            dataReader.setModel(model);
            dataReader.setSink(new TestDataSink(readObjects));
            dataReader.read(new FileInputStream(tmpFile));
    
            assertEquals(1, readObjects.size());
    
            DynaBean obj = (DynaBean)readObjects.get(0);
    
            assertEquals("test",
                         obj.getDynaClass().getName());
            assertEquals("1",
                         obj.get("id").toString());
            assertEquals("foo",
                         obj.get("value").toString());
        }
        finally
        {
            tmpFile.delete();
        }
    }

    /**
     * Tests sub elements for columns.
     */
    public void testSubElements() throws Exception
    {
        final String testSchemaXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String testDataXml =
            "<data>\n"+
            "  <test id='1'>\n"+
            "    <value>foo</value>\n"+
            "  </test>\n"+
            "  <test id='2' value='foo'>\n"+
            "    <value>bar</value>\n"+
            "  </test>\n"+
            "  <test id='3' value='baz'>\n"+
            "  </test>\n"+
            "</data>";

        DatabaseIO modelReader = new DatabaseIO();

        modelReader.setValidateXml(true);
        
        Database        model       = modelReader.read(new StringReader(testSchemaXml));
        final ArrayList readObjects = new ArrayList();
        DataReader      dataReader  = new DataReader();

        dataReader.setModel(model);
        dataReader.setSink(new TestDataSink(readObjects));
        dataReader.read(new StringReader(testDataXml));

        assertEquals(3, readObjects.size());

        DynaBean obj = (DynaBean)readObjects.get(0);

        assertEquals("test",
                     obj.getDynaClass().getName());
        assertEquals("1",
                     obj.get("id").toString());
        assertEquals("foo",
                     obj.get("value").toString());

        obj = (DynaBean)readObjects.get(1);

        assertEquals("test",
                     obj.getDynaClass().getName());
        assertEquals("2",
                     obj.get("id").toString());
        assertEquals("bar",
                     obj.get("value").toString());

        obj = (DynaBean)readObjects.get(2);

        assertEquals("test",
                     obj.getDynaClass().getName());
        assertEquals("3",
                     obj.get("id").toString());
        assertEquals("baz",
                     obj.get("value").toString());
    }

    /**
     * Tests that the name of the root element does not matter.
     */
    public void testRootElementNameDoesntMatter() throws Exception
    {
        final String testSchemaXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String testDataXml =
            "<someRandomName>\n"+
            "  <test id='1' value='foo'/>\n"+
            "</someRandomName>";

        DatabaseIO modelReader = new DatabaseIO();

        modelReader.setValidateXml(true);
        
        Database        model       = modelReader.read(new StringReader(testSchemaXml));
        final ArrayList readObjects = new ArrayList();
        DataReader      dataReader  = new DataReader();

        dataReader.setModel(model);
        dataReader.setSink(new TestDataSink(readObjects));
        dataReader.read(new StringReader(testDataXml));

        assertEquals(1, readObjects.size());

        DynaBean obj = (DynaBean)readObjects.get(0);

        assertEquals("test",
                     obj.getDynaClass().getName());
        assertEquals("1",
                     obj.get("id").toString());
        assertEquals("foo",
                     obj.get("value").toString());
    }

    /**
     * Tests that elements for undefined tables are ignored.
     */
    public void testElementForUndefinedTable() throws Exception
    {
        final String testSchemaXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String testDataXml =
            "<data>\n"+
            "  <test id='1' value='foo'/>\n"+
            "  <other id='2' value='bar'/>\n"+
            "  <test id='3' value='baz'/>\n"+
            "</data>";

        DatabaseIO modelReader = new DatabaseIO();

        modelReader.setValidateXml(true);
        
        Database        model       = modelReader.read(new StringReader(testSchemaXml));
        final ArrayList readObjects = new ArrayList();
        DataReader      dataReader  = new DataReader();

        dataReader.setModel(model);
        dataReader.setSink(new TestDataSink(readObjects));
        dataReader.read(new StringReader(testDataXml));

        assertEquals(2, readObjects.size());

        DynaBean obj = (DynaBean)readObjects.get(0);

        assertEquals("test",
                     obj.getDynaClass().getName());
        assertEquals("1",
                     obj.get("id").toString());
        assertEquals("foo",
                     obj.get("value").toString());

        obj = (DynaBean)readObjects.get(1);

        assertEquals("test",
                     obj.getDynaClass().getName());
        assertEquals("3",
                     obj.get("id").toString());
        assertEquals("baz",
                     obj.get("value").toString());
    }

    /**
     * Tests that attributes for which no column is defined, are ignored.
     */
    public void testAttributeForUndefinedColumn() throws Exception
    {
        final String testSchemaXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String testDataXml =
            "<data>\n"+
            "  <test id='1' value1='foo'/>\n"+
            "</data>";

        DatabaseIO modelReader = new DatabaseIO();

        modelReader.setValidateXml(true);
        
        Database        model       = modelReader.read(new StringReader(testSchemaXml));
        final ArrayList readObjects = new ArrayList();
        DataReader      dataReader  = new DataReader();

        dataReader.setModel(model);
        dataReader.setSink(new TestDataSink(readObjects));
        dataReader.read(new StringReader(testDataXml));

        assertEquals(1, readObjects.size());

        DynaBean obj = (DynaBean)readObjects.get(0);

        assertEquals("test",
                     obj.getDynaClass().getName());
        assertEquals("1",
                     obj.get("id").toString());
        assertNull(obj.get("value"));
    }

    /**
     * Tests that sub elements for which no column is defined, are ignored.
     */
    public void testSubElementForUndefinedColumn() throws Exception
    {
        final String testSchemaXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String testDataXml =
            "<data>\n"+
            "  <test id='1'>\n"+
            "    <value2>foo</value2>\n"+
            "  </test>\n"+
            "</data>";

        DatabaseIO modelReader = new DatabaseIO();

        modelReader.setValidateXml(true);
        
        Database        model       = modelReader.read(new StringReader(testSchemaXml));
        final ArrayList readObjects = new ArrayList();
        DataReader      dataReader  = new DataReader();

        dataReader.setModel(model);
        dataReader.setSink(new TestDataSink(readObjects));
        dataReader.read(new StringReader(testDataXml));

        assertEquals(1, readObjects.size());

        DynaBean obj = (DynaBean)readObjects.get(0);

        assertEquals("test",
                     obj.getDynaClass().getName());
        assertEquals("1",
                     obj.get("id").toString());
        assertNull(obj.get("value"));
    }

    /**
     * Tests parsing when case sensitivity is turned on.
     */
    public void testCaseSensitivityTurnedOn() throws Exception
    {
        final String testSchemaXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='Test'>\n"+
            "    <column name='Id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='Value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String testDataXml =
            "<data>\n"+
            "  <test Id='1' Value='foo'/>\n"+
            "  <Test Id='2' value='baz'/>\n"+
            "</data>";

        DatabaseIO modelReader = new DatabaseIO();

        modelReader.setValidateXml(true);
        
        Database        model       = modelReader.read(new StringReader(testSchemaXml));
        final ArrayList readObjects = new ArrayList();
        DataReader      dataReader  = new DataReader();

        dataReader.setCaseSensitive(true);
        dataReader.setModel(model);
        dataReader.setSink(new TestDataSink(readObjects));
        dataReader.read(new StringReader(testDataXml));

        assertEquals(1, readObjects.size());

        DynaBean obj = (DynaBean)readObjects.get(0);

        assertEquals("Test",
                     obj.getDynaClass().getName());
        assertEquals("2",
                     obj.get("Id").toString());
        assertNull(obj.get("Value"));
    }

    /**
     * Tests parsing when case sensitivity is turned off.
     */
    public void testCaseSensitivityTurnedOff() throws Exception
    {
        final String testSchemaXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='Test'>\n"+
            "    <column name='Id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='Value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String testDataXml =
            "<data>\n"+
            "  <test Id='1' Value='foo'/>\n"+
            "  <Test Id='2' value='bar'/>\n"+
            "  <Test id='3' Value='baz'/>\n"+
            "</data>";

        DatabaseIO modelReader = new DatabaseIO();

        modelReader.setValidateXml(true);
        
        Database        model       = modelReader.read(new StringReader(testSchemaXml));
        final ArrayList readObjects = new ArrayList();
        DataReader      dataReader  = new DataReader();

        dataReader.setCaseSensitive(false);
        dataReader.setModel(model);
        dataReader.setSink(new TestDataSink(readObjects));
        dataReader.read(new StringReader(testDataXml));

        assertEquals(3, readObjects.size());

        DynaBean obj = (DynaBean)readObjects.get(0);

        assertEquals("Test",
                     obj.getDynaClass().getName());
        assertEquals("1",
                     obj.get("Id").toString());
        assertEquals("foo",
                     obj.get("Value").toString());

        obj = (DynaBean)readObjects.get(1);

        assertEquals("Test",
                     obj.getDynaClass().getName());
        assertEquals("2",
                     obj.get("Id").toString());
        assertEquals("bar",
                     obj.get("Value").toString());

        obj = (DynaBean)readObjects.get(2);

        assertEquals("Test",
                     obj.getDynaClass().getName());
        assertEquals("3",
                     obj.get("Id").toString());
        assertEquals("baz",
                     obj.get("Value").toString());
    }

    /**
     * Tests special characters in the data XML (for DDLUTILS-63).
     */
    public void testSpecialCharacters() throws Exception
    {
        final String testSchemaXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String testedValue = "Some Special Characters: \u0001\u0009\u0010";

        DatabaseIO modelIO = new DatabaseIO();

        modelIO.setValidateXml(true);
        
        Database     model      = modelIO.read(new StringReader(testSchemaXml));
        StringWriter output     = new StringWriter();
        DataWriter   dataWriter = new DataWriter(output, "UTF-8");
        SqlDynaBean  bean       = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set("value", testedValue);
        dataWriter.writeDocumentStart();
        dataWriter.write(bean);
        dataWriter.writeDocumentEnd();

        String dataXml = output.toString();

        final ArrayList readObjects = new ArrayList();
        DataReader      dataReader  = new DataReader();

        dataReader.setModel(model);
        dataReader.setSink(new TestDataSink(readObjects));
        dataReader.read(new StringReader(dataXml));

        assertEquals(1, readObjects.size());

        DynaBean obj = (DynaBean)readObjects.get(0);

        assertEquals("test",
                     obj.getDynaClass().getName());
        assertEquals("1",
                     obj.get("id").toString());
        assertEquals(testedValue,
                     obj.get("value").toString());
    }

    /**
     * Tests a cdata section (see DDLUTILS-174).
     */
    public void testCData() throws Exception
    {
        final String testSchemaXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value1' type='VARCHAR' size='50' required='true'/>\n"+
            "    <column name='value2' type='VARCHAR' size='4000' required='true'/>\n"+
            "    <column name='value3' type='LONGVARCHAR' size='4000' required='true'/>\n"+
            "    <column name='value4' type='LONGVARCHAR' size='4000' required='true'/>\n"+
            "    <column name='value5' type='LONGVARCHAR' size='4000' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String testedValue1 = "<?xml version='1.0' encoding='ISO-8859-1'?><test><![CDATA[some text]]></test>";
        final String testedValue2 = StringUtils.repeat("a ", 1000) + testedValue1;
        final String testedValue3 = "<div>\n<h1><![CDATA[WfMOpen]]></h1>\n" + StringUtils.repeat("Make it longer\n", 99) +  "</div>";
        final String testedValue4 = "<![CDATA[" + StringUtils.repeat("b \n", 1000) +  "]]>";
        final String testedValue5 = "<<![CDATA[" + StringUtils.repeat("b \n", 500) +  "]]>><![CDATA[" + StringUtils.repeat("c \n", 500) +  "]]>";

        DatabaseIO modelIO = new DatabaseIO();

        modelIO.setValidateXml(true);

        Database     model      = modelIO.read(new StringReader(testSchemaXml));
        StringWriter output     = new StringWriter();
        DataWriter   dataWriter = new DataWriter(output, "UTF-8");
        SqlDynaBean  bean       = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set("value1", testedValue1);
        bean.set("value2", testedValue2);
        bean.set("value3", testedValue3);
        bean.set("value4", testedValue4);
        bean.set("value5", testedValue5);
        dataWriter.writeDocumentStart();
        dataWriter.write(bean);
        dataWriter.writeDocumentEnd();

        String dataXml = output.toString();

        final ArrayList readObjects = new ArrayList();
        DataReader      dataReader  = new DataReader();

        dataReader.setModel(model);
        dataReader.setSink(new TestDataSink(readObjects));
        dataReader.read(new StringReader(dataXml));

        assertEquals(1, readObjects.size());

        DynaBean obj = (DynaBean)readObjects.get(0);

        assertEquals("test",
                     obj.getDynaClass().getName());
        assertEquals("1",
                     obj.get("id").toString());
        assertEquals(testedValue1,
                     obj.get("value1").toString());
        assertEquals(testedValue2,
                     obj.get("value2").toString());
        assertEquals(testedValue3,
                     obj.get("value3").toString());
        assertEquals(testedValue4,
                     obj.get("value4").toString());
        assertEquals(testedValue5,
                     obj.get("value5").toString());
    }

    /**
     * Tests the reader & writer behavior when the table name is not a valid XML identifier.
     */
    public void testTableNameNotAValidXmlIdentifier() throws Exception
    {
        final String testSchemaXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String testedValue = "Some Special Characters: \u0001\u0009\u0010";

        DatabaseIO modelIO = new DatabaseIO();

        modelIO.setValidateXml(true);
        
        Database     model      = modelIO.read(new StringReader(testSchemaXml));
        StringWriter output     = new StringWriter();
        DataWriter   dataWriter = new DataWriter(output, "UTF-8");
        SqlDynaBean  bean       = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set("value", testedValue);
        dataWriter.writeDocumentStart();
        dataWriter.write(bean);
        dataWriter.writeDocumentEnd();

        String dataXml = output.toString();

        final ArrayList readObjects = new ArrayList();
        DataReader      dataReader  = new DataReader();

        dataReader.setModel(model);
        dataReader.setSink(new TestDataSink(readObjects));
        dataReader.read(new StringReader(dataXml));

        assertEquals(1, readObjects.size());

        DynaBean obj = (DynaBean)readObjects.get(0);

        assertEquals("test",
                     obj.getDynaClass().getName());
        assertEquals("1",
                     obj.get("id").toString());
        assertEquals(testedValue,
                     obj.get("value").toString());
    }

    // TODO: additional tests
    // - table name with illegal-for-XML characters, e.g space, &, ... (write)
    // - column name with illegal-for-XML characters, e.g space, &, ... (write)
}
