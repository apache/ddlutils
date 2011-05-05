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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.dynabean.SqlDynaBean;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

/**
 * Tests the {@link org.apache.ddlutils.io.DataReader} and {@link org.apache.ddlutils.io.DataWriter} classes.
 * 
 * @version $Revision: 289996 $
 */
public class TestDataReaderAndWriter extends TestCase
{
    /**
     * A test data sink. There is no need to call start/end as the don't do anything anyways in this class.
     */
    private static class TestDataSink implements DataSink
    {
        /** Stores the read objects. */
        private final ArrayList readObjects;

        /**
         * Creates a new test data sink using the given list as the backing store.
         * 
         * @param readObjects The list to store the read object
         */
        private TestDataSink(ArrayList readObjects)
        {
            this.readObjects = readObjects;
        }

        /**
         * {@inheritDoc}
         */
        public void start() throws DataSinkException
        {}

        /**
         * {@inheritDoc}
         */
        public void addBean(DynaBean bean) throws DataSinkException
        {
            readObjects.add(bean);
        }

        /**
         * {@inheritDoc}
         */
        public void end() throws DataSinkException
        {}
    }

    /**
     * Reads the given schema xml into a {@link Database} object.
     * 
     * @param schemaXml The schema xml
     * @return The database model object
     */
    private Database readModel(String schemaXml)
    {
        DatabaseIO modelIO = new DatabaseIO();

        modelIO.setValidateXml(true);
        
        return modelIO.read(new StringReader(schemaXml));
    }

    /**
     * Writes the given dyna bean via a {@link DataWriter} and returns the raw xml output.
     * 
     * @param model    The database model to use
     * @param bean     The bean to write
     * @param encoding The encoding in which to write the xml
     * @return The xml output as raw bytes
     */
    private byte[] writeBean(Database model, SqlDynaBean bean, String encoding)
    {
        ByteArrayOutputStream output     = new ByteArrayOutputStream();
        DataWriter            dataWriter = new DataWriter(output, encoding);

        dataWriter.writeDocumentStart();
        dataWriter.write(bean);
        dataWriter.writeDocumentEnd();

        return output.toByteArray();
    }

    /**
     * Uses a {@link DataReader} with default settings to read dyna beans from the given xml data.
     * 
     * @param model   The database model to use
     * @param dataXml The raw xml data
     * @return The read dyna beans
     */
    private List readBeans(Database model, byte[] dataXml)
    {
        ArrayList  beans      = new ArrayList();
        DataReader dataReader = new DataReader();

        dataReader.setModel(model);
        dataReader.setSink(new TestDataSink(beans));
        dataReader.read(new ByteArrayInputStream(dataXml));
        return beans;
    }

    /**
     * Uses a {@link DataReader} with default settings to read dyna beans from the given xml data.
     * 
     * @param model   The database model to use
     * @param dataXml The xml data
     * @return The read dyna beans
     */
    private List readBeans(Database model, String dataXml)
    {
        ArrayList  beans      = new ArrayList();
        DataReader dataReader = new DataReader();

        dataReader.setModel(model);
        dataReader.setSink(new TestDataSink(beans));
        dataReader.read(new StringReader(dataXml));
        return beans;
    }

    /**
     * Helper method to perform a test that writes a bean and then reads it back.
     * 
     * @param model           The database model to use
     * @param bean            The bean to write and read back
     * @param encoding        The encoding to use for the data xml
     * @param expectedDataXml The expected xml generated for the bean
     */
    private void roundtripTest(Database model, SqlDynaBean bean, String encoding, String expectedDataXml) throws UnsupportedEncodingException
    {
        byte[] xmlData = writeBean(model, bean, encoding);

        assertEquals(expectedDataXml, new String(xmlData, encoding));

        List beans = readBeans(model, xmlData);

        assertEquals(1, beans.size());
        assertEquals(bean, beans.get(0));
    }

    /**
     * Tests reading the data from XML.
     */
    public void testRead() throws Exception
    {
        Database model = readModel( 
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
            "</database>");
        List beans = readBeans(
            model,
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
            "</data>");

        assertEquals(5, beans.size());

        DynaBean obj1 = (DynaBean)beans.get(0);
        DynaBean obj2 = (DynaBean)beans.get(1);
        DynaBean obj3 = (DynaBean)beans.get(2);
        DynaBean obj4 = (DynaBean)beans.get(3);
        DynaBean obj5 = (DynaBean)beans.get(4);

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
        Database model = readModel(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testDataXml =
            "<data>\n"+
            "  <test id='1' value='foo'/>\n"+
            "</data>";

        File tmpFile = File.createTempFile("data", ".xml");

        try
        {
            Writer writer = new BufferedWriter(new FileWriter(tmpFile));

            writer.write(testDataXml);
            writer.close();

            ArrayList  beans      = new ArrayList();
            DataReader dataReader = new DataReader();
    
            dataReader.setModel(model);
            dataReader.setSink(new TestDataSink(beans));
            dataReader.read(tmpFile.getAbsolutePath());
    
            assertEquals(1, beans.size());
    
            DynaBean obj = (DynaBean)beans.get(0);
    
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
        Database model = readModel(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testDataXml =
            "<data>\n"+
            "  <test id='1' value='foo'/>\n"+
            "</data>";

        File tmpFile = File.createTempFile("data", ".xml");

        try
        {
            Writer writer = new BufferedWriter(new FileWriter(tmpFile));

            writer.write(testDataXml);
            writer.close();

            ArrayList  beans      = new ArrayList();
            DataReader dataReader = new DataReader();
    
            dataReader.setModel(model);
            dataReader.setSink(new TestDataSink(beans));
            dataReader.read(tmpFile);
    
            assertEquals(1, beans.size());
    
            DynaBean obj = (DynaBean)beans.get(0);
    
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
        Database model = readModel(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testDataXml =
            "<data>\n"+
            "  <test id='1' value='foo'/>\n"+
            "</data>";

        File tmpFile = File.createTempFile("data", ".xml");

        try
        {
            Writer writer = new BufferedWriter(new FileWriter(tmpFile));

            writer.write(testDataXml);
            writer.close();

            ArrayList  beans      = new ArrayList();
            DataReader dataReader = new DataReader();
    
            dataReader.setModel(model);
            dataReader.setSink(new TestDataSink(beans));
            dataReader.read(new FileInputStream(tmpFile));
    
            assertEquals(1, beans.size());
    
            DynaBean obj = (DynaBean)beans.get(0);
    
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
        Database model = readModel(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        List beans = readBeans(
            model, 
            "<data>\n"+
            "  <test id='1'>\n"+
            "    <value>foo</value>\n"+
            "  </test>\n"+
            "  <test id='2' value='foo'>\n"+
            "    <value>bar</value>\n"+
            "  </test>\n"+
            "  <test id='3' value='baz'>\n"+
            "  </test>\n"+
            "</data>");

        assertEquals(3, beans.size());

        DynaBean obj = (DynaBean)beans.get(0);

        assertEquals("test",
                     obj.getDynaClass().getName());
        assertEquals("1",
                     obj.get("id").toString());
        assertEquals("foo",
                     obj.get("value").toString());

        obj = (DynaBean)beans.get(1);

        assertEquals("test",
                     obj.getDynaClass().getName());
        assertEquals("2",
                     obj.get("id").toString());
        assertEquals("bar",
                     obj.get("value").toString());

        obj = (DynaBean)beans.get(2);

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
        Database model = readModel(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        List beans = readBeans(
            model,
            "<someRandomName>\n"+
            "  <test id='1' value='foo'/>\n"+
            "</someRandomName>");

        assertEquals(1, beans.size());

        DynaBean obj = (DynaBean)beans.get(0);

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
        Database model = readModel(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        List beans = readBeans(
            model,
            "<data>\n"+
            "  <test id='1' value='foo'/>\n"+
            "  <other id='2' value='bar'/>\n"+
            "  <test id='3' value='baz'/>\n"+
            "</data>");

        assertEquals(2, beans.size());

        DynaBean obj = (DynaBean)beans.get(0);

        assertEquals("test",
                     obj.getDynaClass().getName());
        assertEquals("1",
                     obj.get("id").toString());
        assertEquals("foo",
                     obj.get("value").toString());

        obj = (DynaBean)beans.get(1);

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
        Database model = readModel(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        List beans = readBeans(
            model,
            "<data>\n"+
            "  <test id='1' value1='foo'/>\n"+
            "</data>");

        assertEquals(1, beans.size());

        DynaBean obj = (DynaBean)beans.get(0);

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
        Database model = readModel(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        List beans = readBeans(
            model,
            "<data>\n"+
            "  <test id='1'>\n"+
            "    <value2>foo</value2>\n"+
            "  </test>\n"+
            "</data>");

        assertEquals(1, beans.size());

        DynaBean obj = (DynaBean)beans.get(0);

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
        Database model = readModel(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='Test'>\n"+
            "    <column name='Id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='Value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testDataXml =
            "<data>\n"+
            "  <test Id='1' Value='foo'/>\n"+
            "  <Test Id='2' value='baz'/>\n"+
            "</data>";

        ArrayList  beans      = new ArrayList();
        DataReader dataReader = new DataReader();

        dataReader.setCaseSensitive(true);
        dataReader.setModel(model);
        dataReader.setSink(new TestDataSink(beans));
        dataReader.read(new StringReader(testDataXml));

        assertEquals(1, beans.size());

        DynaBean obj = (DynaBean)beans.get(0);

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
        Database model = readModel(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='Test'>\n"+
            "    <column name='Id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='Value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testDataXml =
            "<data>\n"+
            "  <test Id='1' Value='foo'/>\n"+
            "  <Test Id='2' value='bar'/>\n"+
            "  <Test id='3' Value='baz'/>\n"+
            "</data>";

        ArrayList  beans      = new ArrayList();
        DataReader dataReader = new DataReader();

        dataReader.setCaseSensitive(false);
        dataReader.setModel(model);
        dataReader.setSink(new TestDataSink(beans));
        dataReader.read(new StringReader(testDataXml));

        assertEquals(3, beans.size());

        DynaBean obj = (DynaBean)beans.get(0);

        assertEquals("Test",
                     obj.getDynaClass().getName());
        assertEquals("1",
                     obj.get("Id").toString());
        assertEquals("foo",
                     obj.get("Value").toString());

        obj = (DynaBean)beans.get(1);

        assertEquals("Test",
                     obj.getDynaClass().getName());
        assertEquals("2",
                     obj.get("Id").toString());
        assertEquals("bar",
                     obj.get("Value").toString());

        obj = (DynaBean)beans.get(2);

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
        Database model = readModel(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testedValue = "Some Special Characters: \u0001\u0009\u0010";

        SqlDynaBean bean = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set("value", testedValue);

        roundtripTest(model, bean, "ISO-8859-1",
                      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
                      "<data>\n" +
                      "  <test id=\"1\">\n" +
                      "    <value " + DatabaseIO.BASE64_ATTR_NAME + "=\"true\">" + new String(Base64.encodeBase64(testedValue.getBytes("UTF-8")), "ISO-8859-1") + "</value>\n" +
                      "  </test>\n" +
                      "</data>\n");
    }

    /**
     * Tests special characters in the data XML (for DDLUTILS-233).
     */
    public void testSpecialCharactersUTF8() throws Exception
    {
        Database model = readModel(
            "<?xml version='1.0' encoding='UTF-8'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testedValue = "Some Special Characters: \u0001\u0009\u0010";

        SqlDynaBean bean = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set("value", testedValue);

        roundtripTest(model, bean, "ISO-8859-1",
                      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
                      "<data>\n" +
                      "  <test id=\"1\">\n" +
                      "    <value " + DatabaseIO.BASE64_ATTR_NAME + "=\"true\">" + new String(Base64.encodeBase64(testedValue.getBytes("UTF-8")), "UTF-8") + "</value>\n" +
                      "  </test>\n" +
                      "</data>\n");
    }

    /**
     * Tests a cdata section (see DDLUTILS-174).
     */
    public void testCData() throws Exception
    {
        Database model = readModel(
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
            "</database>");
        String testedValue1 = "<?xml version='1.0' encoding='ISO-8859-1'?><test><![CDATA[some text]]></test>";
        String testedValue2 = StringUtils.repeat("a ", 1000) + testedValue1;
        String testedValue3 = "<div>\n<h1><![CDATA[WfMOpen]]></h1>\n" + StringUtils.repeat("Make it longer\n", 99) +  "</div>";
        String testedValue4 = "<![CDATA[" + StringUtils.repeat("b \n", 1000) +  "]]>";
        String testedValue5 = "<<![CDATA[" + StringUtils.repeat("b \n", 500) +  "]]>><![CDATA[" + StringUtils.repeat("c \n", 500) +  "]]>";

        SqlDynaBean  bean = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set("value1", testedValue1);
        bean.set("value2", testedValue2);
        bean.set("value3", testedValue3);
        bean.set("value4", testedValue4);
        bean.set("value5", testedValue5);

        byte[] xmlData = writeBean(model, bean, "UTF-8");
        List   beans   = readBeans(model, xmlData);

        assertEquals(1, beans.size());
        assertEquals(bean, beans.get(0));
    }

    /**
     * Tests the reader & writer behavior when the table name is not a valid XML identifier.
     */
    public void testTableNameLong() throws Exception
    {
        String   tableName = StringUtils.repeat("test", 100);
        Database model     = readModel(
            "<?xml version='1.0' encoding='UTF-8'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='" + tableName + "'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testedValue = "Some Text";

        SqlDynaBean bean = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set("value", testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <table id=\"1\" value=\"" + testedValue + "\">\n" +
                      "    <table-name>" + tableName + "</table-name>\n" +
                      "  </table>\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when the table name is not a valid XML identifier.
     */
    public void testTableNameNotAValidXmlIdentifier() throws Exception
    {
        Database model = readModel(
            "<?xml version='1.0' encoding='UTF-8'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test$'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testedValue = "Some Text";

        SqlDynaBean bean = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set("value", testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <table table-name=\"test$\" id=\"1\" value=\"" + testedValue + "\" />\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when the table name is not a valid XML identifier and too long.
     */
    public void testTableNameInvalidAndLong() throws Exception
    {
        String   tableName = StringUtils.repeat("table name", 50);
        Database model     = readModel(
            "<?xml version='1.0' encoding='UTF-8'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='" + tableName + "'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testedValue = "Some Text";

        SqlDynaBean bean = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set("value", testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <table id=\"1\" value=\"" + testedValue + "\">\n" +
                      "    <table-name>" + tableName + "</table-name>\n" +
                      "  </table>\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when the table name contains a '&' character.
     */
    public void testTableNameContainsAmpersand() throws Exception
    {
        String   tableName   = "test&table";
        Database model       = new Database("test");
        Table    table       = new Table();
        Column   idColumn    = new Column();
        Column   valueColumn = new Column();

        idColumn.setName("id");
        idColumn.setType("INTEGER");
        idColumn.setPrimaryKey(true);
        idColumn.setRequired(true);
        valueColumn.setName("value");
        valueColumn.setType("VARCHAR");
        valueColumn.setSize("50");
        valueColumn.setRequired(true);
        table.setName(tableName);
        table.addColumn(idColumn);
        table.addColumn(valueColumn);
        model.addTable(table);

        SqlDynaBean bean        = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));
        String      testedValue = "Some Text";

        bean.set("id", new Integer(1));
        bean.set("value", testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <table table-name=\"test&amp;table\" id=\"1\" value=\"" + testedValue + "\" />\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when the table name contains a '<' character.
     */
    public void testTableNameContainsLessCharacter() throws Exception
    {
        String   tableName   = "test<table";
        Database model       = new Database("test");
        Table    table       = new Table();
        Column   idColumn    = new Column();
        Column   valueColumn = new Column();

        idColumn.setName("id");
        idColumn.setType("INTEGER");
        idColumn.setPrimaryKey(true);
        idColumn.setRequired(true);
        valueColumn.setName("value");
        valueColumn.setType("VARCHAR");
        valueColumn.setSize("50");
        valueColumn.setRequired(true);
        table.setName(tableName);
        table.addColumn(idColumn);
        table.addColumn(valueColumn);
        model.addTable(table);

        SqlDynaBean bean        = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));
        String      testedValue = "Some Text";

        bean.set("id", new Integer(1));
        bean.set("value", testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <table table-name=\"test&lt;table\" id=\"1\" value=\"" + testedValue + "\" />\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when the table name contains a '>' character.
     */
    public void testTableNameContainsMoreCharacter() throws Exception
    {
        String   tableName   = "test>table";
        Database model       = new Database("test");
        Table    table       = new Table();
        Column   idColumn    = new Column();
        Column   valueColumn = new Column();

        idColumn.setName("id");
        idColumn.setType("INTEGER");
        idColumn.setPrimaryKey(true);
        idColumn.setRequired(true);
        valueColumn.setName("value");
        valueColumn.setType("VARCHAR");
        valueColumn.setSize("50");
        valueColumn.setRequired(true);
        table.setName(tableName);
        table.addColumn(idColumn);
        table.addColumn(valueColumn);
        model.addTable(table);

        SqlDynaBean bean        = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));
        String      testedValue = "Some Text";

        bean.set("id", new Integer(1));
        bean.set("value", testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <table table-name=\"test>table\" id=\"1\" value=\"" + testedValue + "\" />\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when the table name contains characters not allowed in XML.
     */
    public void testTableNameContainsInvalidCharacters() throws Exception
    {
        String   tableName   = "test\u0000table";
        Database model       = new Database("test");
        Table    table       = new Table();
        Column   idColumn    = new Column();
        Column   valueColumn = new Column();

        idColumn.setName("id");
        idColumn.setType("INTEGER");
        idColumn.setPrimaryKey(true);
        idColumn.setRequired(true);
        valueColumn.setName("value");
        valueColumn.setType("VARCHAR");
        valueColumn.setSize("50");
        valueColumn.setRequired(true);
        table.setName(tableName);
        table.addColumn(idColumn);
        table.addColumn(valueColumn);
        model.addTable(table);

        SqlDynaBean bean        = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));
        String      testedValue = "Some Text";

        bean.set("id", new Integer(1));
        bean.set("value", testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <table id=\"1\" value=\"" + testedValue + "\">\n" +
                      "    <table-name " + DatabaseIO.BASE64_ATTR_NAME + "=\"true\">" + new String(Base64.encodeBase64(tableName.getBytes("UTF-8")), "UTF-8") + "</table-name>\n" +
                      "  </table>\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when the table name is 'table'.
     */
    public void testTableNameIsTable() throws Exception
    {
        Database model = readModel(
            "<?xml version='1.0' encoding='UTF-8'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='table'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testedValue = "Some Text";

        DatabaseIO modelIO = new DatabaseIO();

        modelIO.setValidateXml(true);

        SqlDynaBean bean = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set("value", testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <table table-name=\"table\" id=\"1\" value=\"" + testedValue + "\" />\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when a column name is a normal valid tag,
     * and both column name and value are shorter than 255 characters.
     */
    public void testColumnNameAndValueShort() throws Exception
    {
        Database model = readModel(
            "<?xml version='1.0' encoding='UTF-8'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testedValue = "Some Text";

        SqlDynaBean bean = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set("value", testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <test id=\"1\" value=\"" + testedValue + "\" />\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when a column name is a normal valid tag,
     * and the column name is shorter than 255 characters but the value is longer.
     */
    public void testColumnNameShortAndValueLong() throws Exception
    {
        Database model = readModel(
            "<?xml version='1.0' encoding='UTF-8'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='400' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testedValue = StringUtils.repeat("Some Text", 40);

        SqlDynaBean bean = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set("value", testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <test id=\"1\">\n" +
                      "    <value>" + testedValue + "</value>\n" +
                      "  </test>\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when a column name is not a valid XML identifier.
     */
    public void testColumnNameShortAndInvalidAndValueShort() throws Exception
    {
        Database model = readModel(
            "<?xml version='1.0' encoding='UTF-8'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='the value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testedValue = "Some Text";

        SqlDynaBean bean = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set("the value", testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <test id=\"1\">\n" +
                      "    <column column-name=\"the value\">" + testedValue + "</column>\n" +
                      "  </test>\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when a column name is not a valid tag,
     * and the column name is shorter than 255 characters and the value is longer.
     */
    public void testColumnNameShortAndInvalidAndValueLong() throws Exception
    {
        Database model = readModel(
            "<?xml version='1.0' encoding='UTF-8'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='the value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testedValue = StringUtils.repeat("Some Text", 40);

        SqlDynaBean bean = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set("the value", testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <test id=\"1\">\n" +
                      "    <column column-name=\"the value\">" + testedValue + "</column>\n" +
                      "  </test>\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when a column name is a valid tag,
     * and the column name is longer than 255 characters and the value is shorter.
     */
    public void testColumnNameLongAndValueShort() throws Exception
    {
        String columnName = StringUtils.repeat("value", 100);
        Database model = readModel(
            "<?xml version='1.0' encoding='UTF-8'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='" + columnName + "' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testedValue = "Some Text";

        SqlDynaBean bean = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set(columnName, testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <test id=\"1\">\n" +
                      "    <column>\n" +
                      "      <column-name>" + columnName + "</column-name>\n" +
                      "      <column-value>" + testedValue + "</column-value>\n" +
                      "    </column>\n" +
                      "  </test>\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when a column name is a valid tag,
     * and both the column name and value are longer than 255 characters.
     */
    public void testColumnNameLongAndValueLong() throws Exception
    {
        String columnName = StringUtils.repeat("value", 100);
        Database model = readModel(
            "<?xml version='1.0' encoding='UTF-8'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='" + columnName + "' type='VARCHAR' size='500' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testedValue = StringUtils.repeat("Some Text", 40);

        SqlDynaBean bean = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set(columnName, testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <test id=\"1\">\n" +
                      "    <column>\n" +
                      "      <column-name>" + columnName + "</column-name>\n" +
                      "      <column-value>" + testedValue + "</column-value>\n" +
                      "    </column>\n" +
                      "  </test>\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when a column name is a valid tag,
     * and the column name is longer than 255 characters and the value is shorter.
     */
    public void testColumnNameAndValueLong() throws Exception
    {
        String columnName = StringUtils.repeat("value", 100);
        Database model = readModel(
            "<?xml version='1.0' encoding='UTF-8'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='" + columnName + "' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testedValue = "Some Text";

        SqlDynaBean bean = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set(columnName, testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <test id=\"1\">\n" +
                      "    <column>\n" +
                      "      <column-name>" + columnName + "</column-name>\n" +
                      "      <column-value>" + testedValue + "</column-value>\n" +
                      "    </column>\n" +
                      "  </test>\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when a column name is not a valid tag,
     * and the value is invalid, and both are short.
     */
    public void testColumnNameAndValueShortAndInvalid() throws Exception
    {
        Database model = readModel(
            "<?xml version='1.0' encoding='UTF-8'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='the value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testedValue = "the\u0000value";

        SqlDynaBean bean = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set("the value", testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <test id=\"1\">\n" +
                      "    <column column-name=\"the value\" " + DatabaseIO.BASE64_ATTR_NAME + "=\"true\">" + new String(Base64.encodeBase64(testedValue.getBytes("UTF-8")), "UTF-8") + "</column>\n" +
                      "  </test>\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when a column name is a valid tag and longer,
     * than 255 characters, and the value is invalid and shorter than 255 characters.
     */
    public void testColumnNameLongAndValueInvalidAndShort() throws Exception
    {
        String columnName = StringUtils.repeat("value", 100);
        Database model = readModel(
            "<?xml version='1.0' encoding='UTF-8'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='" + columnName + "' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testedValue = "the\u0000value";

        SqlDynaBean bean = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set(columnName, testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <test id=\"1\">\n" +
                      "    <column>\n" +
                      "      <column-name>" + columnName + "</column-name>\n" +
                      "      <column-value " + DatabaseIO.BASE64_ATTR_NAME + "=\"true\">" + new String(Base64.encodeBase64(testedValue.getBytes("UTF-8")), "UTF-8") + "</column-value>\n" +
                      "    </column>\n" +
                      "  </test>\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when a column name is not a valid tag,
     * and the value is invalid, and both are short.
     */
    public void testColumnNameAndValueLongAndInvalid() throws Exception
    {
        Database model       = new Database("test");
        Table    table       = new Table();
        Column   idColumn    = new Column();
        Column   valueColumn = new Column();
        String   columnName  = StringUtils.repeat("the\u0000name", 100);

        idColumn.setName("id");
        idColumn.setType("INTEGER");
        idColumn.setPrimaryKey(true);
        idColumn.setRequired(true);
        valueColumn.setName(columnName);
        valueColumn.setType("VARCHAR");
        valueColumn.setSize("50");
        valueColumn.setRequired(true);
        table.setName("test");
        table.addColumn(idColumn);
        table.addColumn(valueColumn);
        model.addTable(table);

        SqlDynaBean bean        = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));
        String      testedValue = StringUtils.repeat("the\u0000value", 40);

        bean.set("id", new Integer(1));
        bean.set(columnName, testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <test id=\"1\">\n" +
                      "    <column>\n" +
                      "      <column-name " + DatabaseIO.BASE64_ATTR_NAME + "=\"true\">" + new String(Base64.encodeBase64(columnName.getBytes("UTF-8")), "UTF-8") + "</column-name>\n" +
                      "      <column-value " + DatabaseIO.BASE64_ATTR_NAME + "=\"true\">" + new String(Base64.encodeBase64(testedValue.getBytes("UTF-8")), "UTF-8") + "</column-value>\n" +
                      "    </column>\n" +
                      "  </test>\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when a column name contains an invalid character.
     */
    public void testColumnNameContainsInvalidCharacters() throws Exception
    {
        Database model = readModel(
            "<?xml version='1.0' encoding='UTF-8'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String  testedValue = "the\u0000value";

        SqlDynaBean bean = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set("value", testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <test id=\"1\">\n" +
                      "    <value " + DatabaseIO.BASE64_ATTR_NAME + "=\"true\">" + new String(Base64.encodeBase64(testedValue.getBytes("UTF-8")), "UTF-8") + "</value>\n" +
                      "  </test>\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when a column value contains an invalid character.
     */
    public void testColumnValueContainsInvalidCharacters() throws Exception
    {
        Database model       = new Database("test");
        Table    table       = new Table();
        Column   idColumn    = new Column();
        Column   valueColumn = new Column();
        String   columnName  = "the\u0000value";

        idColumn.setName("id");
        idColumn.setType("INTEGER");
        idColumn.setPrimaryKey(true);
        idColumn.setRequired(true);
        valueColumn.setName(columnName);
        valueColumn.setType("VARCHAR");
        valueColumn.setSize("50");
        valueColumn.setRequired(true);
        table.setName("test");
        table.addColumn(idColumn);
        table.addColumn(valueColumn);
        model.addTable(table);

        SqlDynaBean bean        = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));
        String      testedValue = "Some Text";

        bean.set("id", new Integer(1));
        bean.set(columnName, testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <test id=\"1\">\n" +
                      "    <column>\n" +
                      "      <column-name " + DatabaseIO.BASE64_ATTR_NAME + "=\"true\">" + new String(Base64.encodeBase64(columnName.getBytes("UTF-8")), "UTF-8") + "</column-name>\n" +
                      "      <column-value>" + testedValue + "</column-value>\n" +
                      "    </column>\n" +
                      "  </test>\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when a column value contains the '&' character.
     */
    public void testColumnValueContainsAmpersand() throws Exception
    {
        Database model       = new Database("test");
        Table    table       = new Table();
        Column   idColumn    = new Column();
        Column   valueColumn = new Column();
        String   columnName  = "foo&bar";

        idColumn.setName("id");
        idColumn.setType("INTEGER");
        idColumn.setPrimaryKey(true);
        idColumn.setRequired(true);
        valueColumn.setName(columnName);
        valueColumn.setType("VARCHAR");
        valueColumn.setSize("50");
        valueColumn.setRequired(true);
        table.setName("test");
        table.addColumn(idColumn);
        table.addColumn(valueColumn);
        model.addTable(table);

        SqlDynaBean bean        = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));
        String      testedValue = "Some Text";

        bean.set("id", new Integer(1));
        bean.set(columnName, testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <test id=\"1\">\n" +
                      "    <column column-name=\"foo&amp;bar\">" + testedValue + "</column>\n" +
                      "  </test>\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when a column value contains the '<' character.
     */
    public void testColumnValueContainsLessCharacter() throws Exception
    {
        Database model       = new Database("test");
        Table    table       = new Table();
        Column   idColumn    = new Column();
        Column   valueColumn = new Column();
        String   columnName  = "foo<bar";

        idColumn.setName("id");
        idColumn.setType("INTEGER");
        idColumn.setPrimaryKey(true);
        idColumn.setRequired(true);
        valueColumn.setName(columnName);
        valueColumn.setType("VARCHAR");
        valueColumn.setSize("50");
        valueColumn.setRequired(true);
        table.setName("test");
        table.addColumn(idColumn);
        table.addColumn(valueColumn);
        model.addTable(table);

        SqlDynaBean bean        = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));
        String      testedValue = "Some Text";

        bean.set("id", new Integer(1));
        bean.set(columnName, testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <test id=\"1\">\n" +
                      "    <column column-name=\"foo&lt;bar\">" + testedValue + "</column>\n" +
                      "  </test>\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when a column value contains the '>' character.
     */
    public void testColumnValueContainsMoreCharacter() throws Exception
    {
        Database model       = new Database("test");
        Table    table       = new Table();
        Column   idColumn    = new Column();
        Column   valueColumn = new Column();
        String   columnName  = "foo>bar";

        idColumn.setName("id");
        idColumn.setType("INTEGER");
        idColumn.setPrimaryKey(true);
        idColumn.setRequired(true);
        valueColumn.setName(columnName);
        valueColumn.setType("VARCHAR");
        valueColumn.setSize("50");
        valueColumn.setRequired(true);
        table.setName("test");
        table.addColumn(idColumn);
        table.addColumn(valueColumn);
        model.addTable(table);

        SqlDynaBean bean        = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));
        String      testedValue = "Some Text";

        bean.set("id", new Integer(1));
        bean.set(columnName, testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <test id=\"1\">\n" +
                      "    <column column-name=\"foo>bar\">" + testedValue + "</column>\n" +
                      "  </test>\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when a column name is 'column'.
     */
    public void testColumnNameIsColumn() throws Exception
    {
        Database model = readModel(
            "<?xml version='1.0' encoding='UTF-8'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='column' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testedValue = "Some Text";

        SqlDynaBean bean = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set("column", testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <test id=\"1\" column=\"" + testedValue + "\" />\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when a column name is 'column-name'.
     */
    public void testColumnNameIsColumnName() throws Exception
    {
        Database model = readModel(
            "<?xml version='1.0' encoding='UTF-8'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='column-name' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testedValue = "Some Text";

        SqlDynaBean bean = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set("column-name", testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <test id=\"1\" column-name=\"" + testedValue + "\" />\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when a column name is 'table-name'.
     */
    public void testColumnNameIsTableName() throws Exception
    {
        Database model = readModel(
            "<?xml version='1.0' encoding='UTF-8'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='table-name' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testedValue = "Some Text";

        SqlDynaBean bean = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set("table-name", testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <test id=\"1\">\n" +
                      "    <column column-name=\"table-name\">" + testedValue + "</column>\n" +
                      "  </test>\n" +
                      "</data>\n");
    }

    /**
     * Tests the reader & writer behavior when a column name is 'base64'.
     */
    public void testColumnNameIsBase64() throws Exception
    {
        Database model = readModel(
            "<?xml version='1.0' encoding='UTF-8'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='test'>\n"+
            "    <column name='id' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='" + DatabaseIO.BASE64_ATTR_NAME + "' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>");
        String testedValue = "Some Text";

        SqlDynaBean bean = (SqlDynaBean)model.createDynaBeanFor(model.getTable(0));

        bean.set("id", new Integer(1));
        bean.set(DatabaseIO.BASE64_ATTR_NAME, testedValue);

        roundtripTest(model, bean, "UTF-8",
                      "<?xml version='1.0' encoding='UTF-8'?>\n" +
                      "<data>\n" +
                      "  <test id=\"1\">\n" +
                      "    <column column-name=\"" + DatabaseIO.BASE64_ATTR_NAME + "\">" + testedValue + "</column>\n" +
                      "  </test>\n" +
                      "</data>\n");
    }
}
