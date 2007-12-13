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

import java.math.BigDecimal;
import java.util.List;

import junit.framework.Test;

import org.apache.commons.beanutils.DynaBean;
import org.apache.ddlutils.platform.sybase.SybasePlatform;

/**
 * Tests changing columns, e.g. changing the data type or size.
 *
 * @version $Revision: $
 */
public class TestChangeColumn extends RoundtripTestBase
{
    /**
     * Parameterized test case pattern.
     * 
     * @return The tests
     */
    public static Test suite() throws Exception
    {
        return getTests(TestChangeColumn.class);
    }

    /**
     * Tests the alteration of a column datatype.
     */
    public void testChangeDatatype1()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='false'/>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='DOUBLE' required='false'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { new Integer(1), new Integer(2) });

    	alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip");

        assertEquals(new Double(2.0), beans.get(0), "avalue");
    }

    /**
     * Tests the alteration of a column datatype.
     */
    public void testChangeDatatype2()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='SMALLINT' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='20' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { new Integer(1), new Short((short)2) });

    	alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List     beans = getRows("roundtrip");
        DynaBean bean  = (DynaBean)beans.get(0); 

        // Some databases (e.g. DB2) pad the string for some reason, so we manually trim it
        if (bean.get("avalue") instanceof String)
        {
            bean.set("avalue", ((String)bean.get("avalue")).trim());
        }
        assertEquals((Object)"2", beans.get(0), "avalue");
    }

    /**
     * Tests the alteration of the datatypes of PK and FK columns.
     */
    public void testChangePKAndFKDatatypes()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='fk' type='INTEGER' required='false'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='fk' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='VARCHAR' size='128' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='fk' type='VARCHAR' size='128' required='false'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='fk' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip1", new Object[] { new Integer(1) });
        insertRow("roundtrip2", new Object[] { new Integer(1), new Integer(1) });

        alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List     beans = getRows("roundtrip2");
        DynaBean bean  = (DynaBean)beans.get(0);

        // Some databases (e.g. DB2) pad the string for some reason, so we manually trim it
        if (bean.get("fk") instanceof String)
        {
            bean.set("fk", ((String)bean.get("fk")).trim());
        }
        assertEquals((Object)"1", bean, "fk");
    }

    /**
     * Tests the alteration of the sizes of PK and FK columns.
     */
    public void testChangePKAndFKSizes()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='fk' type='VARCHAR' size='32' required='false'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='fk' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='VARCHAR' size='128' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='fk' type='VARCHAR' size='128' required='false'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='fk' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip1", new Object[] { "test" });
        insertRow("roundtrip2", new Object[] { new Integer(1), "test" });

        alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List     beans = getRows("roundtrip2");
        DynaBean bean  = (DynaBean)beans.get(0);

        assertEquals((Object)"test", bean, "fk");
    }

    /**
     * Tests the alteration of the datatypes of columns of a PK and FK that
     * will be dropped.
     */
    public void testChangeDroppedPKAndFKDatatypes()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='fk' type='INTEGER' required='false'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='fk' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='VARCHAR' primaryKey='false' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='fk' type='VARCHAR' required='false'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip1", new Object[] { new Integer(1) });
        insertRow("roundtrip2", new Object[] { new Integer(1), new Integer(1) });

        alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List     beans = getRows("roundtrip2");
        DynaBean bean  = (DynaBean)beans.get(0);

        // Some databases (e.g. DB2) pad the string for some reason, so we manually trim it
        if (bean.get("fk") instanceof String)
        {
            bean.set("fk", ((String)bean.get("fk")).trim());
        }
        assertEquals((Object)"1", bean, "fk");
    }
    
    /**
     * Tests the alteration of the datatypes of a column that is indexed.
     */
    public void testChangeIndexColumnDatatype()
    {
        if (!getPlatformInfo().isIndicesSupported())
        {
            return;
        }

        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='NUMERIC' size='8' required='false'/>\n"+
            "    <index name='avalue_index'>\n"+
            "      <index-column name='avalue'/>\n"+
            "    </index>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='false'/>\n"+
            "    <index name='avalue_index'>\n"+
            "      <index-column name='avalue'/>\n"+
            "    </index>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { new Integer(1), new Integer(1) });
        insertRow("roundtrip", new Object[] { new Integer(2), new Integer(10) });

        alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip");

        assertEquals(new Integer(1), beans.get(0), "avalue");
        assertEquals(new Integer(10), beans.get(1), "avalue");
    }

    /**
     * Tests the alteration of the datatypes of an indexed column where
     * the index will be dropped.
     */
    public void testChangeDroppedIndexColumnDatatype()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='NUMERIC' size='8' required='false'/>\n"+
            "    <index name='avalue_index'>\n"+
            "      <index-column name='avalue'/>\n"+
            "    </index>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='false'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { new Integer(1), new Integer(1) });
        insertRow("roundtrip", new Object[] { new Integer(2), new Integer(10) });

        alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip");

        assertEquals(new Integer(1), beans.get(0), "avalue");
        assertEquals(new Integer(10), beans.get(1), "avalue");
    }

    /**
     * Tests the alteration of a column size.
     */
    public void testChangeSize()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='20' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { new Integer(1), "test" });

        alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip");

        assertEquals((Object)"test", beans.get(0), "avalue");
    }

    /**
     * Tests the alteration of a column's datatype and size.
     */
    public void testChangeDatatypeAndSize()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='CHAR' size='4' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { new Integer(1), "test" });

        alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip");

        assertEquals((Object)"test", beans.get(0), "avalue");
    }

    /**
     * Tests the alteration of a column null constraint.
     */
    public void testChangeNull()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='false'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { new Integer(1), new Integer(2) });

    	alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip");

        assertEquals(new Integer(2), beans.get(0), "avalue");
    }

    /**
     * Tests the addition of a column's default value.
     */
    public void testAddDefault()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='DOUBLE'/>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='DOUBLE' default='2.0'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { new Integer(1), new Double(2.0) });

    	alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip");

        assertEquals(new Double(2.0), beans.get(0), "avalue");
    }

    /**
     * Tests the change of a column default value.
     */
    public void testChangeDefault()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' default='1'/>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' default='20'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { new Integer(1), new Integer(2) });

    	alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip");

        assertEquals(new Integer(2), beans.get(0), "avalue");
    }

    /**
     * Tests the removal of a column default value.
     */
    public void testDropDefault()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='20' default='test'/>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='20'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { new Integer(1) });

    	alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip");

        assertEquals((Object)"test", beans.get(0), "avalue");
    }

    /**
     * Tests the change of a column's auto-increment state.
     */
    public void testMakeAutoIncrement()
    {
        if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported())
        {
            return;
        }
        // Sybase does not like INTEGER auto-increment columns
        if (SybasePlatform.DATABASENAME.equals(getPlatform().getName()))
        {
            String  model1Xml = 
                "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                "<database name='roundtriptest'>\n"+
                "  <table name='roundtrip'>\n"+
                "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
                "    <column name='avalue' type='NUMERIC' size='12,0'/>\n"+
                "  </table>\n"+
                "</database>";
            String model2Xml =
                "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                "<database name='roundtriptest'>\n"+
                "  <table name='roundtrip'>\n"+
                "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
                "    <column name='avalue' type='NUMERIC' size='12,0' autoIncrement='true' required='true'/>\n"+
                "  </table>\n"+
                "</database>";

            createDatabase(model1Xml);

            insertRow("roundtrip", new Object[] { new Integer(1), new BigDecimal(2) });

            alterDatabase(model2Xml);

            assertEquals(getAdjustedModel(),
                         readModelFromDatabase("roundtriptest"));

            List beans = getRows("roundtrip");

            assertEquals(new BigDecimal(2), beans.get(0), "avalue");
        }
        else
        {
            String  model1Xml = 
                "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                "<database name='roundtriptest'>\n"+
                "  <table name='roundtrip'>\n"+
                "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
                "    <column name='avalue' type='INTEGER'/>\n"+
                "  </table>\n"+
                "</database>";
            String model2Xml=
                "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                "<database name='roundtriptest'>\n"+
                "  <table name='roundtrip'>\n"+
                "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
                "    <column name='avalue' type='INTEGER' autoIncrement='true' required='true'/>\n"+
                "  </table>\n"+
                "</database>";

            createDatabase(model1Xml);

            insertRow("roundtrip", new Object[] { new Integer(1), new Integer(2) });

            alterDatabase(model2Xml);

            assertEquals(getAdjustedModel(),
                         readModelFromDatabase("roundtriptest"));

            List beans = getRows("roundtrip");

            assertEquals(new Integer(2), beans.get(0), "avalue");
        }
    }

    /**
     * Tests the removal the column auto-increment status.
     */
    public void testDropAutoIncrement()
    {
        if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported())
        {
            return;
        }

        boolean      isSybase = SybasePlatform.DATABASENAME.equals(getPlatform().getName());
        final String model1Xml; 
        final String model2Xml;

        if (isSybase)
        {
            model1Xml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                        "<database name='roundtriptest'>\n"+
                        "  <table name='roundtrip'>\n"+
                        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
                        "    <column name='avalue' type='NUMERIC' size='12,0' required='true' autoIncrement='true'/>\n"+
                        "  </table>\n"+
                        "</database>";
            model2Xml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                        "<database name='roundtriptest'>\n"+
                        "  <table name='roundtrip'>\n"+
                        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
                        "    <column name='avalue' type='NUMERIC' size='12,0' required='true' autoIncrement='false'/>\n"+
                        "  </table>\n"+
                        "</database>";
        }
        else
        {
            model1Xml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                        "<database name='roundtriptest'>\n"+
                        "  <table name='roundtrip'>\n"+
                        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
                        "    <column name='avalue' type='INTEGER' autoIncrement='true'/>\n"+
                        "  </table>\n"+
                        "</database>";
            model2Xml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                        "<database name='roundtriptest'>\n"+
                        "  <table name='roundtrip'>\n"+
                        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
                        "    <column name='avalue' type='INTEGER' autoIncrement='false'/>\n"+
                        "  </table>\n"+
                        "</database>";
        }

        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { new Integer(1) });

    	alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip");

        if (isSybase)
        {
            assertEquals(new BigDecimal(1), beans.get(0), "avalue");
        }
        else
        {
            assertEquals(new Integer(1), beans.get(0), "avalue");
        }
    }
}
