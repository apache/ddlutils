package org.apache.ddlutils.dynabean;

/*
 * Copyright 1999-2006 The Apache Software Foundation.
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

import java.util.List;

import org.apache.commons.beanutils.DynaBean;
import org.apache.ddlutils.TestDatabaseWriterBase;
import org.apache.ddlutils.platform.ModelBasedResultSetIterator;

/**
 * Tests the sql querying.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class TestDynaSqlQueries extends TestDatabaseWriterBase
{
    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        getPlatform().setDelimitedIdentifierModeOn(false);
    }

    /**
     * Tests a simple SELECT query.
     */
    public void testSimpleQuery() throws Exception
    {
        createDatabase(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='ddlutils'>\n"+
            "  <table name='TestTable'>\n"+
            "    <column name='Id'   type='INTEGER' primaryKey='true'/>\n"+
            "    <column name='Text' type='VARCHAR' size='15'/>\n"+
            "  </table>\n"+
            "</database>");

        insertData(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<data>\n"+
            "  <TestTable Id='1' Text='Text 1'/>\n"+
            "  <TestTable Id='2' Text='Text 2'/>\n"+
            "  <TestTable Id='3' Text='Text 3'/>"+
            "</data>");

        ModelBasedResultSetIterator it   = (ModelBasedResultSetIterator)getPlatform().query(getModel(), "SELECT * FROM TestTable");
        DynaBean        bean = null;

        assertTrue(it.hasNext());
        // we call the method a second time to assert that the result set does not get advanced twice
        assertTrue(it.hasNext());

        bean = (DynaBean)it.next();

        assertEquals(new Integer(1),
                     getPropertyValue(bean, "Id"));
        assertEquals("Text 1",
                     getPropertyValue(bean, "Text"));
        
        assertTrue(it.hasNext());

        bean = (DynaBean)it.next();

        assertEquals(new Integer(2),
                     getPropertyValue(bean, "Id"));
        assertEquals("Text 2",
                     getPropertyValue(bean, "Text"));

        assertTrue(it.hasNext());

        bean = (DynaBean)it.next();

        assertEquals(new Integer(3),
                     getPropertyValue(bean, "Id"));
        assertEquals("Text 3",
                     getPropertyValue(bean, "Text"));

        assertFalse(it.hasNext());
        assertFalse(it.isConnectionOpen());
    }

    /**
     * Tests a simple SELECT fetch.
     */
    public void testSimpleFetch() throws Exception
    {
        createDatabase(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='ddlutils'>\n"+
            "  <table name='TestTable'>\n"+
            "    <column name='Id'   type='INTEGER' primaryKey='true'/>\n"+
            "    <column name='Text' type='VARCHAR' size='15'/>\n"+
            "  </table>\n"+
            "</database>");

        insertData(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<data>\n"+
            "  <TestTable Id='1' Text='Text 1'/>\n"+
            "  <TestTable Id='2' Text='Text 2'/>\n"+
            "  <TestTable Id='3' Text='Text 3'/>"+
            "</data>");

        List beans = getPlatform().fetch(getModel(), "SELECT * FROM TestTable");

        assertEquals(3,
                     beans.size());

        DynaBean bean = (DynaBean)beans.get(0);

        assertEquals(new Integer(1),
                     getPropertyValue(bean, "Id"));
        assertEquals("Text 1",
                     getPropertyValue(bean, "Text"));
        
        bean = (DynaBean)beans.get(1);

        assertEquals(new Integer(2),
                     getPropertyValue(bean, "Id"));
        assertEquals("Text 2",
                     getPropertyValue(bean, "Text"));

        bean = (DynaBean)beans.get(2);

        assertEquals(new Integer(3),
                     getPropertyValue(bean, "Id"));
        assertEquals("Text 3",
                     getPropertyValue(bean, "Text"));
    }

    /**
     * Tests a more complicated SELECT query that leads to a JOIN in the database.
     */
    public void testJoinQuery() throws Exception
    {
        createDatabase(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='ddlutils'>\n"+
            "  <table name='TestTable1'>\n"+
            "    <column name='Id1' type='INTEGER' primaryKey='true'/>\n"+
            "    <column name='Id2' type='INTEGER'/>\n"+
            "  </table>\n"+
            "  <table name='TestTable2'>\n"+
            "    <column name='Id' type='INTEGER' primaryKey='true'/>\n"+
            "    <column name='Avalue' type='VARCHAR' size='15'/>\n"+
            "  </table>\n"+
            "</database>");

        insertData(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<data>\n"+
            "  <TestTable1 Id1='1'/>\n"+
            "  <TestTable1 Id1='2' Id2='3'/>\n"+
            "  <TestTable2 Id='1' Avalue='Text 1'/>\n"+
            "  <TestTable2 Id='2' Avalue='Text 2'/>\n"+
            "  <TestTable2 Id='3' Avalue='Text 3'/>"+
            "</data>");

        ModelBasedResultSetIterator it   = (ModelBasedResultSetIterator)getPlatform().query(getModel(), "SELECT Id1, Avalue FROM TestTable1, TestTable2 WHERE Id2 = Id");
        DynaBean        bean = null;

        assertTrue(it.hasNext());

        bean = (DynaBean)it.next();

        assertEquals(new Integer(2),
                     getPropertyValue(bean, "Id1"));
        assertEquals("Text 3",
                     getPropertyValue(bean, "Avalue"));

        assertFalse(it.hasNext());
        assertFalse(it.isConnectionOpen());
    }
}
