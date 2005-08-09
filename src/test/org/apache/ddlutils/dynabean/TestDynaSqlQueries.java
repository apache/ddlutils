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
            "  <table name='testtable'>\n"+
            "    <column name='id'   type='INTEGER' primaryKey='true'/>\n"+
            "    <column name='text' type='VARCHAR' size='15'/>\n"+
            "  </table>\n"+
            "</database>");

        insertData(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<data>\n"+
            "  <testtable id='1' text='Text 1'/>\n"+
            "  <testtable id='2' text='Text 2'/>\n"+
            "  <testtable id='3' text='Text 3'/>"+
            "</data>");

        DynaSql         dynaSql = new DynaSql(getBuilder(), getDataSource(), getModel());
        DynaSqlIterator it      = (DynaSqlIterator)dynaSql.query("SELECT * FROM testtable");
        DynaBean        bean    = null;

        assertTrue(it.hasNext());
        // we call the method a second time to assert that the result set does not get advanced twice
        assertTrue(it.hasNext());

        bean = (DynaBean)it.next();

        assertEquals(new Integer(1),
                     bean.get("id"));
        assertEquals("Text 1",
                     bean.get("text"));
        
        assertTrue(it.hasNext());

        bean = (DynaBean)it.next();

        assertEquals(new Integer(2),
                     bean.get("id"));
        assertEquals("Text 2",
                     bean.get("text"));

        assertTrue(it.hasNext());

        bean = (DynaBean)it.next();

        assertEquals(new Integer(3),
                     bean.get("id"));
        assertEquals("Text 3",
                     bean.get("text"));

        assertFalse(it.hasNext());
        assertFalse(it.isConnectionOpen());
    }

    public void testSimpleFetch() throws Exception
    {
        createDatabase(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='ddlutils'>\n"+
            "  <table name='testtable'>\n"+
            "    <column name='id'   type='INTEGER' primaryKey='true'/>\n"+
            "    <column name='text' type='VARCHAR' size='15'/>\n"+
            "  </table>\n"+
            "</database>");

        insertData(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<data>\n"+
            "  <testtable id='1' text='Text 1'/>\n"+
            "  <testtable id='2' text='Text 2'/>\n"+
            "  <testtable id='3' text='Text 3'/>"+
            "</data>");

        DynaSql  dynaSql = new DynaSql(getBuilder(), getDataSource(), getModel());
        List     beans   = dynaSql.fetch("SELECT * FROM testtable");

        assertEquals(3,
                     beans.size());

        DynaBean bean = (DynaBean)beans.get(0);

        assertEquals(new Integer(1),
                     bean.get("id"));
        assertEquals("Text 1",
                     bean.get("text"));
        
        bean = (DynaBean)beans.get(1);

        assertEquals(new Integer(2),
                     bean.get("id"));
        assertEquals("Text 2",
                     bean.get("text"));

        bean = (DynaBean)beans.get(2);

        assertEquals(new Integer(3),
                     bean.get("id"));
        assertEquals("Text 3",
                     bean.get("text"));
    }

    public void testJoinQuery() throws Exception
    {
        createDatabase(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='ddlutils'>\n"+
            "  <table name='testtable1'>\n"+
            "    <column name='id1' type='INTEGER' primaryKey='true'/>\n"+
            "    <column name='id2' type='INTEGER'/>\n"+
            "  </table>\n"+
            "  <table name='testtable2'>\n"+
            "    <column name='id'   type='INTEGER' primaryKey='true'/>\n"+
            "    <column name='text' type='VARCHAR' size='15'/>\n"+
            "  </table>\n"+
            "</database>");

        insertData(
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<data>\n"+
            "  <testtable1 id1='1'/>\n"+
            "  <testtable1 id1='2' id2='3'/>\n"+
            "  <testtable2 id='1' text='Text 1'/>\n"+
            "  <testtable2 id='2' text='Text 2'/>\n"+
            "  <testtable2 id='3' text='Text 3'/>"+
            "</data>");

        DynaSql         dynaSql = new DynaSql(getBuilder(), getDataSource(), getModel());
        DynaSqlIterator it      = (DynaSqlIterator)dynaSql.query("SELECT id1, text FROM testtable1, testtable2 WHERE id2 = id");
        DynaBean        bean    = null;

        assertTrue(it.hasNext());

        bean = (DynaBean)it.next();

        assertEquals(new Integer(2),
                     bean.get("id1"));
        assertEquals("Text 3",
                     bean.get("text"));

        assertFalse(it.hasNext());
        assertFalse(it.isConnectionOpen());
    }

}
