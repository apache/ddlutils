package org.apache.ddlutils.io;

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

import java.io.StringReader;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.apache.ddlutils.model.Database;

/**
 * Tests the {@link org.apache.ddlutils.io.DataReader} class.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision:$
 */
public class TestDataReader extends TestCase
{
    private static final String TEST_SCHEMA = 
        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
        "<database name=\"bookstore\">\n"+
        "  <table name=\"author\">\n"+
        "    <column name=\"author_id\" type=\"INTEGER\" primaryKey=\"true\" required=\"true\"/>\n"+
        "    <column name=\"name\" type=\"VARCHAR\" size=\"50\" required=\"true\"/>\n"+
        "    <column name=\"organisation\" type=\"VARCHAR\" size=\"50\" required=\"false\"/>\n"+
        "  </table>\n"+
        "  <table name=\"book\">\n"+
        "    <column name=\"book_id\" type=\"INTEGER\" required=\"true\" primaryKey=\"true\" autoIncrement=\"true\"/>\n"+
        "    <column name=\"isbn\" type=\"VARCHAR\" size=\"15\" required=\"true\"/>\n"+
        "    <column name=\"author_id\" type=\"INTEGER\" required=\"true\"/>\n"+
        "    <column name=\"title\" type=\"VARCHAR\" size=\"255\" defaultValue=\"N/A\" required=\"true\"/>\n"+
        "    <column name=\"issue_date\" type=\"DATE\" required=\"false\"/>\n"+
        "    <foreign-key foreignTable=\"author\">\n"+
        "      <reference local=\"author_id\" foreign=\"author_id\"/>\n"+
        "    </foreign-key>\n"+
        "    <index name=\"book_isbn\">\n"+
        "      <index-column name=\"isbn\"/>\n"+
        "    </index>\n"+
        "  </table>\n"+
        "</database>";

    private static final String TEST_DATA =
        "<data>\n"+
        "  <author author_id=\"1\" name=\"Ernest Hemingway\"/>\n"+
        "  <author author_id=\"2\" name=\"William Shakespeare\"/>\n"+
        "  <book book_id=\"1\" author_id=\"1\">\n"+
        "    <isbn>0684830493</isbn>\n"+
        "    <title>Old Man And The Sea</title>\n"+
        "    <issue_date>1952</issue_date>\n"+
        "  </book>\n"+
        "  <book book_id=\"2\" author_id=\"2\">\n"+
        "    <isbn>0198321465</isbn>\n"+
        "    <title>Macbeth</title>\n"+
        "    <issue_date>1606</issue_date>\n"+
        "  </book>\n"+
        "  <book book_id=\"3\" author_id=\"2\">\n"+
        "    <isbn>0140707026</isbn>\n"+
        "    <title>A Midsummer Night's Dream</title>\n"+
        "    <issue_date>1595</issue_date>\n"+
        "  </book>\n"+
        "</data>";

    public void testRead() throws Exception
    {
        DatabaseReader  modelReader = new DatabaseReader();
        Database        model       = (Database)modelReader.parse(new StringReader(TEST_SCHEMA));
        final ArrayList readObjects = new ArrayList();
        DataReader      dataReader  = new DataReader(model, new DataSink() {
            public void addBean(DynaBean bean)
            {
                readObjects.add(bean);
            }
        });

        dataReader.parse(new StringReader(TEST_DATA));

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
        assertEquals("1952",
                     obj3.get("issue_date").toString());
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
        assertEquals("1606",
                     obj4.get("issue_date").toString());
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
        assertEquals("1595",
                     obj5.get("issue_date").toString());
    }
}
