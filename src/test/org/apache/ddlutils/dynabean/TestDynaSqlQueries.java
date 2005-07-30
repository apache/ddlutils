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
}
