package org.apache.ddlutils.io;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import java.sql.Types;
import java.util.List;

import junit.framework.Test;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.NonUniqueIndex;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.UniqueIndex;

/**
 * Performs tests for the alteration of databases.
 * 
 * @author Thomas Dudziak
 * @version $Revision: $
 */
public class TestAlteration extends RoundtripTestBase
{
    /**
     * Parameterized test case pattern.
     * 
     * @return The tests
     */
    public static Test suite() throws Exception
    {
        return getTests(TestAlteration.class);
    }

    /**
     * Tests the alteration of a column datatype.
     */
    public void testChangeDatatype1()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='false'/>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip", new Object[] { new Integer(1), new Integer(2) });

    	model.getTable(0).getColumn(1).setTypeCode(Types.DOUBLE);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip");

        assertEquals(new Double(2.0), beans.get(0), "avalue");
    }

    /**
     * Tests the alteration of a column datatype.
     */
    public void testChangeDatatype2()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='SMALLINT' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip", new Object[] { new Integer(1), new Short((short)2) });

    	model.getTable(0).getColumn(1).setTypeCode(Types.VARCHAR);
    	model.getTable(0).getColumn(1).setSize("20");

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip");

        assertEquals((Object)"2", beans.get(0), "avalue");
    }

    /**
     * Tests the alteration of a column null constraint.
     */
    public void testChangeNull()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip", new Object[] { new Integer(1), new Integer(2) });

    	model.getTable(0).getColumn(1).setRequired(false);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip");

        assertEquals(new Integer(2), beans.get(0), "avalue");
    }

    /**
     * Tests the addition of a column default value.
     */
    public void testAddDefault()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='DOUBLE'/>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip", new Object[] { new Integer(1), new Double(2.0) });

    	model.getTable(0).getColumn(1).setDefaultValue("1.0");

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip");

        assertEquals(new Double(2.0), beans.get(0), "avalue");
    }

    /**
     * Tests the change of a column default value.
     */
    public void testChangeDefault()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' default='1'/>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip", new Object[] { new Integer(1), new Integer(2) });

    	model.getTable(0).getColumn(1).setDefaultValue("20");

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip");

        assertEquals(new Integer(2), beans.get(0), "avalue");
    }

    /**
     * Tests the dropping of a column default value.
     */
    public void testDropDefault()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='20' default='test'/>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip", new Object[] { new Integer(1) });

    	model.getTable(0).getColumn(1).setDefaultValue(null);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip");

        assertEquals((Object)"test", beans.get(0), "avalue");
    }

    /**
     * Tests the making a column auto-increment.
     */
    public void testMakeAutoIncrement()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip", new Object[] { new Integer(1), new Integer(2) });

    	model.getTable(0).getColumn(1).setAutoIncrement(true);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip");

        assertEquals(new Integer(2), beans.get(0), "avalue");
    }

    /**
     * Tests the dropping the column auto-increment status.
     */
    public void testDropAutoIncrement()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' autoIncrement='true'/>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip", new Object[] { new Integer(1) });

    	model.getTable(0).getColumn(1).setAutoIncrement(false);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip");

        assertEquals(new Integer(1), beans.get(0), "avalue");
    }

    /**
     * Tests the adding a column.
     */
    public void testAddColumn()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip", new Object[] { new Integer(1) });

    	Column newColumn = new Column();

    	newColumn.setName("avalue");
    	newColumn.setTypeCode(Types.INTEGER);
    	newColumn.setDefaultValue("2");
    	newColumn.setRequired(true);
    	model.getTable(0).addColumn(newColumn);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip");

        assertEquals(new Integer(2), beans.get(0), "avalue");
    }

    /**
     * Tests the dropping a column.
     */
    public void testDropColumn()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='50'/>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip", new Object[] { new Integer(1), "test" });

    	model.getTable(0).removeColumn(1);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip");

        assertEquals(new Integer(1), beans.get(0), "pk");
    }

    /**
     * Tests the adding a column to the pk.
     */
    public void testAddColumnToPK()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip", new Object[] { new Integer(1), "test" });

    	model.getTable(0).getColumn(1).setPrimaryKey(true);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip");

        assertEquals((Object)"test", beans.get(0), "avalue");
    }

    /**
     * Tests the removing a column from the pk.
     */
    public void testRemoveColumnFromPK()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='50' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip", new Object[] { new Integer(1), "test" });

    	model.getTable(0).getColumn(1).setPrimaryKey(false);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip");

        assertEquals((Object)"test", beans.get(0), "avalue");
    }

    /**
     * Tests the adding a pk column.
     */
    public void testAddPKColumn()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip", new Object[] { new Integer(1) });

    	Column newColumn = new Column();

    	newColumn.setName("avalue");
    	newColumn.setTypeCode(Types.INTEGER);
    	newColumn.setPrimaryKey(true);
    	newColumn.setRequired(true);
        newColumn.setDefaultValue("0");
    	model.getTable(0).addColumn(newColumn);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip");

        assertEquals(new Integer(0), beans.get(0), "avalue");
    }

    /**
     * Tests the dropping of a pk column.
     */
    public void testDropPKColumn()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='50' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip", new Object[] { new Integer(1), "test" });

    	model.getTable(0).removeColumn(1);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip");

        assertEquals(new Integer(1), beans.get(0), "pk");
    }

    /**
     * Tests the adding of an index.
     */
    public void testAddIndex()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='VARCHAR' size='50'/>\n"+
            "    <column name='avalue2' type='INTEGER' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip", new Object[] { new Integer(1), null, new Integer(2) });

    	Index newIndex = new NonUniqueIndex(); 
    	
    	newIndex.setName("test");
    	newIndex.addColumn(new IndexColumn(model.getTable(0).getColumn(1).getName()));
    	newIndex.addColumn(new IndexColumn(model.getTable(0).getColumn(2).getName()));

    	model.getTable(0).addIndex(newIndex);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip");

        assertEquals((Object)null, beans.get(0), "avalue1");
        assertEquals(new Integer(2), beans.get(0), "avalue2");
    }

    /**
     * Tests the adding of an unique index.
     */
    public void testAddUniqueIndex()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip", new Object[] { new Integer(1), new Integer(2) });

    	Index newIndex = new UniqueIndex(); 
    	
    	newIndex.setName("test");
    	newIndex.addColumn(new IndexColumn(model.getTable(0).getColumn(1).getName()));

    	model.getTable(0).addIndex(newIndex);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip");

        assertEquals(new Integer(2), beans.get(0), "avalue");
    }

    /**
     * Tests the dropping of an unique index.
     */
    public void testDropUniqueIndex()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='DOUBLE'/>\n"+
            "    <column name='avalue2' type='VARCHAR' size='50'/>\n"+
            "    <unique name='test_index'>\n"+
            "      <unique-column name='avalue2'/>\n"+
            "      <unique-column name='avalue1'/>\n"+
            "    </unique>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip", new Object[] { new Integer(1), new Double(2.0), "test" });

    	model.getTable(0).removeIndex(0);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip");

        assertEquals(new Double(2.0), beans.get(0), "avalue1");
        assertEquals((Object)"test", beans.get(0), "avalue2");
    }

    /**
     * Tests the adding of a column to an index.
     */
    public void testAddColumnToIndex()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='DOUBLE'/>\n"+
            "    <column name='avalue2' type='VARCHAR' size='40'/>\n"+
            "    <index name='test_index'>\n"+
            "      <index-column name='avalue1'/>\n"+
            "    </index>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip", new Object[] { new Integer(1), new Double(2.0), "test" });

    	model.getTable(0).getIndex(0).addColumn(new IndexColumn(model.getTable(0).getColumn(2).getName()));

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip");

        assertEquals(new Double(2.0), beans.get(0), "avalue1");
        assertEquals((Object)"test", beans.get(0), "avalue2");
    }

    /**
     * Tests the removing of a column from an index.
     */
    public void testRemoveColumnFromUniqueIndex()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='DOUBLE'/>\n"+
            "    <column name='avalue2' type='INTEGER'/>\n"+
            "    <unique name='test_index'>\n"+
            "      <unique-column name='avalue1'/>\n"+
            "      <unique-column name='avalue2'/>\n"+
            "    </unique>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip", new Object[] { new Integer(1), new Double(2.0), new Integer(3) });

    	model.getTable(0).getIndex(0).removeColumn(1);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip");

        assertEquals(new Double(2.0), beans.get(0), "avalue1");
        assertEquals(new Integer(3), beans.get(0), "avalue2");
    }

    /**
     * Tests the adding a foreign key.
     */
    public void testAddFK()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip1", new Object[] { new Integer(1) });
        insertRow("roundtrip2", new Object[] { "2", new Integer(1) });

    	ForeignKey newFk = new ForeignKey("test");
    	
    	newFk.setForeignTable(model.getTable(0));
    	newFk.addReference(new Reference(model.getTable(1).getColumn(1), model.getTable(0).getColumn(0)));
    	model.getTable(1).addForeignKey(newFk);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans1 = getRows("roundtrip1");
        List beans2 = getRows("roundtrip2");

        assertEquals(new Integer(1), beans1.get(0), "pk");
        assertEquals((Object)"2", beans2.get(0), "pk");
        assertEquals(new Integer(1), beans2.get(0), "avalue");
    }

    /**
     * Tests the dropping of a foreign key.
     */
    public void testDropFK()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='pk2' type='DOUBLE' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='DOUBLE' required='true'/>\n"+
            "    <column name='avalue2' type='INTEGER' required='true'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue2' foreign='pk1'/>\n"+
            "      <reference local='avalue1' foreign='pk2'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip1", new Object[] { new Integer(1), new Double(2.0) });
        insertRow("roundtrip2", new Object[] { new Integer(2), new Double(2.0), new Integer(1) });

    	model.getTable(1).removeForeignKey(0);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans1 = getRows("roundtrip1");
        List beans2 = getRows("roundtrip2");

        assertEquals(new Integer(1), beans1.get(0), "pk1");
        assertEquals(new Double(2.0), beans1.get(0), "pk2");
        assertEquals(new Integer(2), beans2.get(0), "pk");
        assertEquals(new Double(2.0), beans2.get(0), "avalue1");
        assertEquals(new Integer(1), beans2.get(0), "avalue2");
    }

    /**
     * Tests the adding a reference to a foreign key.
     */
    public void testAddReferenceToFK()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='INTEGER' required='true'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue1' foreign='pk1'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip1", new Object[] { new Integer(1) });
        insertRow("roundtrip2", new Object[] { new Integer(2), new Integer(1) });

    	Column newPkColumn = new Column();

    	newPkColumn.setName("pk2");
    	newPkColumn.setTypeCode(Types.DOUBLE);
    	newPkColumn.setPrimaryKey(true);
    	newPkColumn.setRequired(true);
        newPkColumn.setDefaultValue("0.0");
    	model.getTable(0).addColumn(newPkColumn);

    	Column newFkColumn = new Column();

    	newFkColumn.setName("avalue2");
    	newFkColumn.setTypeCode(Types.DOUBLE);
    	newFkColumn.setRequired(true);
        newFkColumn.setDefaultValue("0.0");
    	model.getTable(1).addColumn(newFkColumn);

    	model.getTable(1).getForeignKey(0).addReference(new Reference(newFkColumn, newPkColumn));

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans1 = getRows("roundtrip1");
        List beans2 = getRows("roundtrip2");

        assertEquals(new Integer(1), beans1.get(0), "pk1");
        assertEquals(new Double(0.0), beans1.get(0), "pk2");
        assertEquals(new Integer(2), beans2.get(0), "pk");
        assertEquals(new Integer(1), beans2.get(0), "avalue1");
        assertEquals(new Double(0.0), beans2.get(0), "avalue2");
    }

    /**
     * Tests the removing a reference from a foreign key.
     */
    public void testRemoveReferenceFromFK()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='pk2' type='VARCHAR' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='VARCHAR' required='true'/>\n"+
            "    <column name='avalue2' type='INTEGER' required='true'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue2' foreign='pk1'/>\n"+
            "      <reference local='avalue1' foreign='pk2'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip1", new Object[] { new Integer(1), "test" });
        insertRow("roundtrip2", new Object[] { new Integer(2), "test", new Integer(1) });

    	model.getTable(0).removeColumn(1);
    	model.getTable(1).removeColumn(1);
    	model.getTable(1).getForeignKey(0).removeReference(1);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans1 = getRows("roundtrip1");
        List beans2 = getRows("roundtrip2");

        assertEquals(new Integer(1), beans1.get(0), "pk1");
        assertEquals(new Integer(2), beans2.get(0), "pk");
        assertEquals(new Integer(1), beans2.get(0), "avalue2");
    }

    /**
     * Tests the adding a table.
     */
    public void testAddTable1()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
           "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip1", new Object[] { new Integer(1) });

    	Table      newTable    = new Table();
    	Column     newPkColumn = new Column();
    	Column     newFkColumn = new Column();
    	ForeignKey newFk       = new ForeignKey("test");

    	newPkColumn.setName("pk");
    	newPkColumn.setTypeCode(Types.VARCHAR);
    	newPkColumn.setSize("20");
    	newPkColumn.setPrimaryKey(true);
    	newPkColumn.setRequired(true);
    	newFkColumn.setName("avalue");
    	newFkColumn.setTypeCode(Types.INTEGER);
    	newFk.setForeignTable(model.getTable(0));
    	newFk.addReference(new Reference(newFkColumn, model.getTable(0).getColumn(0)));
    	newTable.setName("roundtrip2");
    	newTable.addColumn(newPkColumn);
    	newTable.addColumn(newFkColumn);
    	newTable.addForeignKey(newFk);
    	model.addTable(newTable);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip1");

        assertEquals(new Integer(1), beans.get(0), "pk");
    }

    /**
     * Tests the adding a table.
     */
    public void testAddTable2()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32' required='true'/>\n"+
            "  </table>\n"+
           "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip1", new Object[] { new Integer(1), "test" });

    	Table  newTable    = new Table();
    	Column newPkColumn = new Column();

    	newPkColumn.setName("pk");
    	newPkColumn.setTypeCode(Types.VARCHAR);
    	newPkColumn.setSize("20");
    	newPkColumn.setPrimaryKey(true);
    	newPkColumn.setRequired(true);
    	newTable.setName("roundtrip2");
    	newTable.addColumn(newPkColumn);
    	model.addTable(newTable);

        alterDatabase(model);

        // note that we have to split the alteration because we can only add the foreign key if
        // there is a corresponding row in the new table

        insertRow("roundtrip2", new Object[] { "test" });
        
        ForeignKey newFk = new ForeignKey("test");

    	newFk.setForeignTable(newTable);
    	newFk.addReference(new Reference(model.getTable(0).getColumn(1), newPkColumn));
    	model.getTable(0).addForeignKey(newFk);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans1 = getRows("roundtrip1");
        List beans2 = getRows("roundtrip2");

        assertEquals(new Integer(1), beans1.get(0), "pk");
        assertEquals((Object)"test", beans1.get(0), "avalue");
        assertEquals((Object)"test", beans2.get(0), "pk");
    }

    /**
     * Tests the removing a table.
     */
    public void testRemoveTable1()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='DOUBLE' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip1", new Object[] { new Integer(1) });
        insertRow("roundtrip2", new Object[] { new Integer(2), new Double(2.0) });

    	model.removeTable(1);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip1");

        assertEquals(new Integer(1), beans.get(0), "pk");
    }

    /**
     * Tests the removing a table.
     */
    public void testRemoveTable2()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='VARCHAR' size='20' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='20' required='true'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        Database model = createDatabase(modelXml);

        insertRow("roundtrip1", new Object[] { "test" });
        insertRow("roundtrip2", new Object[] { new Integer(1), "test" });

    	model.getTable(1).removeForeignKey(0);
    	model.removeTable(0);

    	alterDatabase(model);

        Database modelFromDb = readModelFromDatabase("roundtriptest");

        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        List beans = getRows("roundtrip2");

        assertEquals(new Integer(1), beans.get(0), "pk");
        assertEquals((Object)"test", beans.get(0), "avalue");
    }
}
