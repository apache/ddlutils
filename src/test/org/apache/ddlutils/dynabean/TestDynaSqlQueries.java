package org.apache.ddlutils.dynabean;

import java.util.List;

import org.apache.commons.beanutils.DynaBean;
import org.apache.ddlutils.TestDatabaseWriterBase;

public class TestDynaSqlQueries extends TestDatabaseWriterBase
{
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

        DynaSqlIterator it   = (DynaSqlIterator)getPlatform().query(getModel(), "SELECT * FROM TestTable");
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
            "    <column name='Id'   type='INTEGER' primaryKey='true'/>\n"+
            "    <column name='Text' type='VARCHAR' size='15'/>\n"+
            "  </table>\n"+
            "</database>");

        insertData(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<data>\n"+
            "  <TestTable1 Id1='1'/>\n"+
            "  <TestTable1 Id1='2' Id2='3'/>\n"+
            "  <TestTable2 Id='1' Text='Text 1'/>\n"+
            "  <TestTable2 Id='2' Text='Text 2'/>\n"+
            "  <TestTable2 Id='3' Text='Text 3'/>"+
            "</data>");

        DynaSqlIterator it   = (DynaSqlIterator)getPlatform().query(getModel(), "SELECT Id1, Text FROM TestTable1, TestTable2 WHERE Id2 = Id");
        DynaBean        bean = null;

        assertTrue(it.hasNext());

        bean = (DynaBean)it.next();

        assertEquals(new Integer(2),
                     getPropertyValue(bean, "Id1"));
        assertEquals("Text 3",
                     getPropertyValue(bean, "Text"));

        assertFalse(it.hasNext());
        assertFalse(it.isConnectionOpen());
    }

}
