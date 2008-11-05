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

import java.util.List;
import java.util.Properties;

import junit.framework.Test;

import org.apache.ddlutils.TestAgainstLiveDatabaseBase;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.platform.firebird.FirebirdPlatform;
import org.apache.ddlutils.platform.mckoi.MckoiPlatform;
import org.apache.ddlutils.platform.mysql.MySql50Platform;
import org.apache.ddlutils.platform.mysql.MySqlPlatform;
import org.apache.ddlutils.platform.sybase.SybasePlatform;

/**
 * Performs tests for the alteration of databases.
 *
 * TODO: add more tests, esp. combining multiple changes
 *       - change datatype/size and add to/remove from pk
 *       - change datatype/size and add/remove pk that uses the column
 *       - change type of column in index and foreign key
 *       - drop index with columns in a foreign key
 *       - ...
 * @version $Revision: $
 */
public class TestAlteration extends TestAgainstLiveDatabaseBase
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
     * Tests the change of the order of the columns of a table.
     */
    public void testChangeColumnOrder()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='VARCHAR' size='32'/>\n"+
            "    <column name='avalue4' type='VARCHAR' size='5'/>\n"+
            "    <column name='avalue3' type='DOUBLE' default='1.0'/>\n"+
            "    <column name='avalue2' type='INTEGER'/>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='VARCHAR' size='32'/>\n"+
            "    <column name='avalue2' type='INTEGER'/>\n"+
            "    <column name='avalue3' type='DOUBLE' default='1.0'/>\n"+
            "    <column name='avalue4' type='VARCHAR' size='5'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { new Integer(1), "test", "value", null, null });

        alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip");

        assertEquals((Object)"test", beans.get(0), "avalue1");
        assertEquals((Object)null, beans.get(0), "avalue2");
        assertEquals(new Double(1.0), beans.get(0), "avalue3");
        assertEquals((Object)"value", beans.get(0), "avalue4");
    }

    /**
     * Test for DDLUTILS-208.
     */
    public void testChangeColumnOrderWithAutoIncrementPK()
    {
        final String model1Xml; 
        final String model2Xml; 

        if (SybasePlatform.DATABASENAME.equals(getPlatform().getName()))
        {
            model1Xml = 
                "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
                "  <table name='roundtrip'>\n"+
                "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true' autoIncrement='true'/>\n"+
                "    <column name='avalue1' type='VARCHAR' size='32'/>\n"+
                "    <column name='avalue4' type='VARCHAR' size='5'/>\n"+
                "    <column name='avalue3' type='DOUBLE' default='1.0'/>\n"+
                "    <column name='avalue2' type='INTEGER'/>\n"+
                "  </table>\n"+
                "</database>";
            model2Xml = 
                "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
                "  <table name='roundtrip'>\n"+
                "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true' autoIncrement='true'/>\n"+
                "    <column name='avalue1' type='VARCHAR' size='32'/>\n"+
                "    <column name='avalue2' type='INTEGER'/>\n"+
                "    <column name='avalue3' type='DOUBLE' default='1.0'/>\n"+
                "    <column name='avalue4' type='VARCHAR' size='5'/>\n"+
                "  </table>\n"+
                "</database>";
        }
        else
        {
            model1Xml = 
                "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
                "  <table name='roundtrip'>\n"+
                "    <column name='pk' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n"+
                "    <column name='avalue1' type='VARCHAR' size='32'/>\n"+
                "    <column name='avalue4' type='VARCHAR' size='5'/>\n"+
                "    <column name='avalue3' type='DOUBLE' default='1.0'/>\n"+
                "    <column name='avalue2' type='INTEGER'/>\n"+
                "  </table>\n"+
                "</database>";
            model2Xml = 
                "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
                "  <table name='roundtrip'>\n"+
                "    <column name='pk' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n"+
                "    <column name='avalue1' type='VARCHAR' size='32'/>\n"+
                "    <column name='avalue2' type='INTEGER'/>\n"+
                "    <column name='avalue3' type='DOUBLE' default='1.0'/>\n"+
                "    <column name='avalue4' type='VARCHAR' size='5'/>\n"+
                "  </table>\n"+
                "</database>";
        }

        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { null, "test", "value", null, null });

        alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip");

        assertEquals((Object)"test", beans.get(0), "avalue1");
        assertEquals((Object)null, beans.get(0), "avalue2");
        assertEquals(new Double(1.0), beans.get(0), "avalue3");
        assertEquals((Object)"value", beans.get(0), "avalue4");
    }

    /**
     * Test for DDLUTILS-208.
     */
    public void testChangeColumnOrderWithAutoIncrementColumn()
    {
        if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported() ||
            !getPlatformInfo().isMultipleIdentityColumnsSupported())
        {
            return;
        }

        final String model1Xml; 
        final String model2Xml; 

        if (SybasePlatform.DATABASENAME.equals(getPlatform().getName()))
        {
            model1Xml = 
                "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
                "  <table name='roundtrip'>\n"+
                "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
                "    <column name='avalue1' type='VARCHAR' size='32'/>\n"+
                "    <column name='avalue4' type='VARCHAR' size='5'/>\n"+
                "    <column name='avalue3' type='DOUBLE' default='1.0'/>\n"+
                "    <column name='avalue2' type='NUMERIC' size='12,0' required='true' autoIncrement='true'/>\n"+
                "  </table>\n"+
                "</database>";
            model2Xml = 
                "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
                "  <table name='roundtrip'>\n"+
                "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
                "    <column name='avalue1' type='VARCHAR' size='32'/>\n"+
                "    <column name='avalue2' type='NUMERIC' size='12,0' required='true' autoIncrement='true'/>\n"+
                "    <column name='avalue3' type='DOUBLE' default='1.0'/>\n"+
                "    <column name='avalue4' type='VARCHAR' size='5'/>\n"+
                "  </table>\n"+
                "</database>";
        }
        else
        {
            model1Xml = 
                "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
                "  <table name='roundtrip'>\n"+
                "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
                "    <column name='avalue1' type='VARCHAR' size='32'/>\n"+
                "    <column name='avalue4' type='VARCHAR' size='5'/>\n"+
                "    <column name='avalue3' type='DOUBLE' default='1.0'/>\n"+
                "    <column name='avalue2' type='INTEGER' required='true' autoIncrement='true'/>\n"+
                "  </table>\n"+
                "</database>";
            model2Xml = 
                "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
                "  <table name='roundtrip'>\n"+
                "    <column name='pk' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n"+
                "    <column name='avalue1' type='VARCHAR' size='32'/>\n"+
                "    <column name='avalue2' type='INTEGER' required='true' autoIncrement='true'/>\n"+
                "    <column name='avalue3' type='DOUBLE' default='1.0'/>\n"+
                "    <column name='avalue4' type='VARCHAR' size='5'/>\n"+
                "  </table>\n"+
                "</database>";
        }

        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { new Integer(1), "test", "value", null, null });

        alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip");

        assertEquals((Object)"test",  beans.get(0), "avalue1");
        assertEquals(new Integer(1),  beans.get(0), "avalue2");
        assertEquals(new Double(1.0), beans.get(0), "avalue3");
        assertEquals((Object)"value", beans.get(0), "avalue4");
    }

    /**
     * Tests the removal of a column.
     */
    public void testDropColumn()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='50'/>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { new Integer(1), "test" });

    	alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip");

        assertEquals(new Integer(1), beans.get(0), "pk");
    }

    /**
     * Tests the removal of an auto-increment column.
     */
    public void testDropAutoIncrementColumn()
    {
        if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported())
        {
            return;
        }

        boolean      isSybase  = SybasePlatform.DATABASENAME.equals(getPlatform().getName());
        final String model1Xml;
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        if (isSybase)
        {
            model1Xml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
                        "  <table name='roundtrip'>\n"+
                        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
                        "    <column name='avalue' type='NUMERIC' size='12,0' required='true' autoIncrement='true'/>\n"+
                        "  </table>\n"+
                        "</database>";
        }
        else
        {
            model1Xml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
                        "  <table name='roundtrip'>\n"+
                        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
                        "    <column name='avalue' type='INTEGER' autoIncrement='true'/>\n"+
                        "  </table>\n"+
                        "</database>";
        }
        
        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { new Integer(1) });

    	alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip");

        assertEquals(new Integer(1), beans.get(0), "pk");
    }

    /**
     * Tests the addition of a column to the pk.
     */
    public void testAddColumnToPK()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='50' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='50' primaryKey='true' required='true'/>\n"+
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
     * Tests the removal of a column from the pk.
     */
    public void testRemoveColumnFromPK()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='50' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='50' primaryKey='false' required='true'/>\n"+
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
     * Tests the removal of a pk column.
     */
    public void testDropPKColumn()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='50' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { new Integer(1), "test" });

    	alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip");

        assertEquals(new Integer(1), beans.get(0), "pk");
    }

    /**
     * Tests the addition of an index.
     */
    public void testAddIndex()
    {
        if (!getPlatformInfo().isIndicesSupported())
        {
            return;
        }

        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='VARCHAR' size='50'/>\n"+
            "    <column name='avalue2' type='INTEGER' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='VARCHAR' size='50'/>\n"+
            "    <column name='avalue2' type='INTEGER' required='true'/>\n"+
            "    <index name='test'>\n"+
            "      <index-column name='avalue1'/>\n"+
            "      <index-column name='avalue2'/>\n"+
            "    </index>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { new Integer(1), null, new Integer(2) });

    	alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip");

        assertEquals((Object)null, beans.get(0), "avalue1");
        assertEquals(new Integer(2), beans.get(0), "avalue2");
    }

    /**
     * Tests the addition of an unique index.
     */
    public void testAddUniqueIndex()
    {
        if (!getPlatformInfo().isIndicesSupported())
        {
            return;
        }

        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <unique name='test'>\n"+
            "      <unique-column name='avalue'/>\n"+
            "    </unique>\n"+
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
     * Tests the removal of an unique index.
     */
    public void testDropUniqueIndex()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
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
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='DOUBLE'/>\n"+
            "    <column name='avalue2' type='VARCHAR' size='50'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { new Integer(1), new Double(2.0), "test" });

    	alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip");

        assertEquals(new Double(2.0), beans.get(0), "avalue1");
        assertEquals((Object)"test", beans.get(0), "avalue2");
    }

    /**
     * Tests the removal of an index that has column that are also used by foreign keys. This is a
     * test esp. for the handling of http://bugs.mysql.com/bug.php?id=21395.
     */
    public void testDropIndexOverlappingWithForeignKeys()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='VARCHAR' size='50' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip3'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='INTEGER'/>\n"+
            "    <column name='avalue2' type='VARCHAR' size='50'/>\n"+
            "    <index name='test_index'>\n"+
            "      <index-column name='avalue2'/>\n"+
            "      <index-column name='avalue1'/>\n"+
            "    </index>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue1' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "    <foreign-key foreignTable='roundtrip2'>\n"+
            "      <reference local='avalue2' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='VARCHAR' size='50' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip3'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='INTEGER'/>\n"+
            "    <column name='avalue2' type='VARCHAR' size='50'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue1' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "    <foreign-key foreignTable='roundtrip2'>\n"+
            "      <reference local='avalue2' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip1", new Object[] { new Integer(1) });
        insertRow("roundtrip2", new Object[] { "test" });
        insertRow("roundtrip3", new Object[] { new Integer(1), new Integer(1), "test" });

        alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans1 = getRows("roundtrip1");
        List beans2 = getRows("roundtrip2");
        List beans3 = getRows("roundtrip3");

        assertEquals(new Integer(1), beans1.get(0), "pk");
        assertEquals((Object)"test", beans2.get(0), "pk");
        assertEquals(new Integer(1), beans3.get(0), "pk");
        assertEquals(new Integer(1), beans3.get(0), "avalue1");
        assertEquals((Object)"test", beans3.get(0), "avalue2");
    }

    /**
     * Tests the removal of an index that has column that are also referenced by a remote foreign key. 
     */
    public void testDropIndexOverlappingWithRemoteForeignKey()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='50'/>\n"+
            "    <index name='test_index'>\n"+
            "      <index-column name='pk'/>\n"+
            "      <index-column name='avalue'/>\n"+
            "    </index>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='50'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip1", new Object[] { new Integer(1), "test" });
        insertRow("roundtrip2", new Object[] { new Integer(1), new Integer(1) });

        alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans1 = getRows("roundtrip1");
        List beans2 = getRows("roundtrip2");

        assertEquals(new Integer(1), beans1.get(0), "pk");
        assertEquals((Object)"test", beans1.get(0), "avalue");
        assertEquals(new Integer(1), beans2.get(0), "pk");
        assertEquals(new Integer(1), beans2.get(0), "avalue");
    }

    /**
     * Tests the removal of a column from an index.
     */
    public void testRemoveColumnFromUniqueIndex()
    {
        if (!getPlatformInfo().isIndicesSupported())
        {
            return;
        }

        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
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
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='DOUBLE'/>\n"+
            "    <column name='avalue2' type='INTEGER'/>\n"+
            "    <unique name='test_index'>\n"+
            "      <unique-column name='avalue1'/>\n"+
            "    </unique>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { new Integer(1), new Double(2.0), new Integer(3) });

    	alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip");

        assertEquals(new Double(2.0), beans.get(0), "avalue1");
        assertEquals(new Integer(3), beans.get(0), "avalue2");
    }

    /**
     * Tests the addition of a foreign key.
     */
    public void testAddFK()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='true'/>\n"+
            "    <foreign-key name='test' foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip1", new Object[] { new Integer(1) });
        insertRow("roundtrip2", new Object[] { "2", new Integer(1) });

    	alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans1 = getRows("roundtrip1");
        List beans2 = getRows("roundtrip2");

        assertEquals(new Integer(1), beans1.get(0), "pk");
        assertEquals((Object)"2", beans2.get(0), "pk");
        assertEquals(new Integer(1), beans2.get(0), "avalue");
    }

    /**
     * Tests the removal of a foreign key.
     */
    public void testDropFK()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
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
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='pk2' type='DOUBLE' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='DOUBLE' required='true'/>\n"+
            "    <column name='avalue2' type='INTEGER' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip1", new Object[] { new Integer(1), new Double(2.0) });
        insertRow("roundtrip2", new Object[] { new Integer(2), new Double(2.0), new Integer(1) });

    	alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans1 = getRows("roundtrip1");
        List beans2 = getRows("roundtrip2");

        assertEquals(new Integer(1), beans1.get(0), "pk1");
        assertEquals(new Double(2.0), beans1.get(0), "pk2");
        assertEquals(new Integer(2), beans2.get(0), "pk");
        assertEquals(new Double(2.0), beans2.get(0), "avalue1");
        assertEquals(new Integer(1), beans2.get(0), "avalue2");
    }

    /**
     * Tests the removal of a foreign key with camel case naming (DDLUTILS-195).
     */
    public void testDropCamelCaseFK()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip1'>\n"+
            "    <column name='Pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='Pk2' type='DOUBLE' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip2'>\n"+
            "    <column name='Pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='Avalue1' type='DOUBLE' required='true'/>\n"+
            "    <column name='Avalue2' type='INTEGER' required='true'/>\n"+
            "    <foreign-key name='Rt1_To_Rt2' foreignTable='Roundtrip1'>\n"+
            "      <reference local='Avalue2' foreign='Pk1'/>\n"+
            "      <reference local='Avalue1' foreign='Pk2'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip1'>\n"+
            "    <column name='Pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='Pk2' type='DOUBLE' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip2'>\n"+
            "    <column name='Pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='Avalue1' type='DOUBLE' required='true'/>\n"+
            "    <column name='Avalue2' type='INTEGER' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("Roundtrip1", new Object[] { new Integer(1), new Double(2.0) });
        insertRow("Roundtrip2", new Object[] { new Integer(2), new Double(2.0), new Integer(1) });

        alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans1 = getRows("Roundtrip1");
        List beans2 = getRows("Roundtrip2");

        assertEquals(new Integer(1), beans1.get(0), "Pk1");
        assertEquals(new Double(2.0), beans1.get(0), "Pk2");
        assertEquals(new Integer(2), beans2.get(0), "Pk");
        assertEquals(new Double(2.0), beans2.get(0), "Avalue1");
        assertEquals(new Integer(1), beans2.get(0), "Avalue2");
    }

    /**
     * Tests removing a foreign key and an index that has the same name and same column.
     */
    public void testDropFKAndCorrespondingIndex()
    {
        if (!getPlatformInfo().isIndicesSupported() ||
            FirebirdPlatform.DATABASENAME.equals(getPlatform().getName()))
        {
            // Firebird does not allow an index and a foreign key in the same table to have the same name
            return;
        }

        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='pk2' type='DOUBLE' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='DOUBLE' required='true'/>\n"+
            "    <column name='avalue2' type='INTEGER' required='true'/>\n"+
            "    <index name='test'>\n"+
            "      <index-column name='avalue2'/>\n"+
            "      <index-column name='avalue1'/>\n"+
            "    </index>\n"+
            "    <foreign-key name='test' foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue2' foreign='pk1'/>\n"+
            "      <reference local='avalue1' foreign='pk2'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='pk2' type='DOUBLE' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='DOUBLE' required='true'/>\n"+
            "    <column name='avalue2' type='INTEGER' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip1", new Object[] { new Integer(1), new Double(2.0) });
        insertRow("roundtrip2", new Object[] { new Integer(2), new Double(2.0), new Integer(1) });

        alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans1 = getRows("roundtrip1");
        List beans2 = getRows("roundtrip2");

        assertEquals(new Integer(1), beans1.get(0), "pk1");
        assertEquals(new Double(2.0), beans1.get(0), "pk2");
        assertEquals(new Integer(2), beans2.get(0), "pk");
        assertEquals(new Double(2.0), beans2.get(0), "avalue1");
        assertEquals(new Integer(1), beans2.get(0), "avalue2");
    }

    /**
     * Tests removing a foreign key but not the index that has the same name and same column.
     */
    public void testDropFKButNotCorrespondingIndex()
    {
        if (!getPlatformInfo().isIndicesSupported() ||
            FirebirdPlatform.DATABASENAME.equals(getPlatform().getName()))
        {
            // Firebird does not allow an index and a foreign key in the same table to have the same name
            return;
        }

        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='pk2' type='DOUBLE' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='DOUBLE' required='true'/>\n"+
            "    <column name='avalue2' type='INTEGER' required='true'/>\n"+
            "    <index name='test'>\n"+
            "      <index-column name='avalue2'/>\n"+
            "      <index-column name='avalue1'/>\n"+
            "    </index>\n"+
            "    <foreign-key name='test' foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue2' foreign='pk1'/>\n"+
            "      <reference local='avalue1' foreign='pk2'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='pk2' type='DOUBLE' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='DOUBLE' required='true'/>\n"+
            "    <column name='avalue2' type='INTEGER' required='true'/>\n"+
            "    <index name='test'>\n"+
            "      <index-column name='avalue2'/>\n"+
            "      <index-column name='avalue1'/>\n"+
            "    </index>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip1", new Object[] { new Integer(1), new Double(2.0) });
        insertRow("roundtrip2", new Object[] { new Integer(2), new Double(2.0), new Integer(1) });

        alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans1 = getRows("roundtrip1");
        List beans2 = getRows("roundtrip2");

        assertEquals(new Integer(1), beans1.get(0), "pk1");
        assertEquals(new Double(2.0), beans1.get(0), "pk2");
        assertEquals(new Integer(2), beans2.get(0), "pk");
        assertEquals(new Double(2.0), beans2.get(0), "avalue1");
        assertEquals(new Integer(1), beans2.get(0), "avalue2");
    }

    /**
     * Tests removing a foreign key and an index that has the same name but different columns.
     */
    public void testDropFKAndDifferentIndexWithSameName()
    {
        // MySql/InnoDB doesn't allow the creation of a foreign key and index with the same name
        // unless the index can be used as the FK's index
        // Firebird does not allow an index and a foreign key in the same table to have the same name at all
        if (!getPlatformInfo().isIndicesSupported() ||
            MySqlPlatform.DATABASENAME.equals(getPlatform().getName()) ||
            MySql50Platform.DATABASENAME.equals(getPlatform().getName()) ||
            FirebirdPlatform.DATABASENAME.equals(getPlatform().getName()))
        {
            return;
        }

        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='pk2' type='DOUBLE' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='DOUBLE' required='true'/>\n"+
            "    <column name='avalue2' type='INTEGER' required='true'/>\n"+
            "    <index name='test'>\n"+
            "      <index-column name='avalue1'/>\n"+
            "    </index>\n"+
            "    <foreign-key name='test' foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue2' foreign='pk1'/>\n"+
            "      <reference local='avalue1' foreign='pk2'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='pk2' type='DOUBLE' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='DOUBLE' required='true'/>\n"+
            "    <column name='avalue2' type='INTEGER' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip1", new Object[] { new Integer(1), new Double(2.0) });
        insertRow("roundtrip2", new Object[] { new Integer(2), new Double(2.0), new Integer(1) });

        alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans1 = getRows("roundtrip1");
        List beans2 = getRows("roundtrip2");

        assertEquals(new Integer(1), beans1.get(0), "pk1");
        assertEquals(new Double(2.0), beans1.get(0), "pk2");
        assertEquals(new Integer(2), beans2.get(0), "pk");
        assertEquals(new Double(2.0), beans2.get(0), "avalue1");
        assertEquals(new Integer(1), beans2.get(0), "avalue2");
    }

    /**
     * Tests removing a foreign key but not the index that has the same name but different columns.
     */
    public void testDropFKButNotDifferentIndexWithSameName()
    {
        // MySql/InnoDB doesn't allow the creation of a foreign key and index with the same name
        // unless the index can be used as the FK's index
        // Firebird does not allow an index and a foreign key in the same table to have the same name at all
        if (!getPlatformInfo().isIndicesSupported() ||
            MySqlPlatform.DATABASENAME.equals(getPlatform().getName()) ||
            MySql50Platform.DATABASENAME.equals(getPlatform().getName()) ||
            FirebirdPlatform.DATABASENAME.equals(getPlatform().getName()))
        {
            return;
        }

        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='pk2' type='DOUBLE' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='DOUBLE' required='true'/>\n"+
            "    <column name='avalue2' type='INTEGER' required='true'/>\n"+
            "    <index name='test'>\n"+
            "      <index-column name='avalue1'/>\n"+
            "    </index>\n"+
            "    <foreign-key name='test' foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue2' foreign='pk1'/>\n"+
            "      <reference local='avalue1' foreign='pk2'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='pk2' type='DOUBLE' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='DOUBLE' required='true'/>\n"+
            "    <column name='avalue2' type='INTEGER' required='true'/>\n"+
            "    <index name='test'>\n"+
            "      <index-column name='avalue1'/>\n"+
            "    </index>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip1", new Object[] { new Integer(1), new Double(2.0) });
        insertRow("roundtrip2", new Object[] { new Integer(2), new Double(2.0), new Integer(1) });

        alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans1 = getRows("roundtrip1");
        List beans2 = getRows("roundtrip2");

        assertEquals(new Integer(1), beans1.get(0), "pk1");
        assertEquals(new Double(2.0), beans1.get(0), "pk2");
        assertEquals(new Integer(2), beans2.get(0), "pk");
        assertEquals(new Double(2.0), beans2.get(0), "avalue1");
        assertEquals(new Integer(1), beans2.get(0), "avalue2");
    }

    /**
     * Tests the removal of several foreign keys. Test for DDLUTILS-150.
     */
    public void testDropFKs()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip3'>\n"+
            "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='pk2' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip4'>\n"+
            "    <column name='pk' primaryKey='true' required='true' type='INTEGER' />\n"+
            "    <column name='fk1' required='true' type='INTEGER' />\n"+
            "    <column name='fk2' type='INTEGER' required='false' />\n"+
            "    <foreign-key name='roundtrip1_fk' foreignTable='roundtrip1'>\n"+
            "      <reference foreign='pk' local='pk' />\n"+
            "    </foreign-key>\n"+
            "    <foreign-key name='roundtrip2_fk1' foreignTable='roundtrip2'>\n"+
            "      <reference foreign='pk' local='fk1' />\n"+
            "    </foreign-key>\n"+
            "    <foreign-key name='roundtrip2_fk2' foreignTable='roundtrip2'>\n"+
            "      <reference foreign='pk' local='fk2' />\n"+
            "    </foreign-key>\n"+
            "    <foreign-key name='roundtrip3_fk' foreignTable='roundtrip3'>\n"+
            "      <reference foreign='pk1' local='pk' />\n"+
            "      <reference foreign='pk2' local='fk2' />\n"+
            "    </foreign-key>\n"+
            "   </table> \n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip3'>\n"+
            "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='pk2' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip4'>\n"+
            "    <column name='pk' primaryKey='true' required='true' type='INTEGER' />\n"+
            "    <column name='fk1' required='true' type='INTEGER' />\n"+
            "    <column name='fk2' type='INTEGER' required='false' />\n"+
            "   </table> \n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip1", new Object[] { new Integer(1) });
        insertRow("roundtrip2", new Object[] { new Integer(2) });
        insertRow("roundtrip2", new Object[] { new Integer(3) });
        insertRow("roundtrip3", new Object[] { new Integer(1), new Integer(2) });
        insertRow("roundtrip4", new Object[] { new Integer(1), new Integer(3), new Integer(2) });

        alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans1 = getRows("roundtrip1");
        List beans2 = getRows("roundtrip2");
        List beans3 = getRows("roundtrip3");
        List beans4 = getRows("roundtrip4");

        assertEquals(new Integer(1),  beans1.get(0), "pk");
        assertEquals(new Integer(2),  beans2.get(0), "pk");
        assertEquals(new Integer(3),  beans2.get(1), "pk");
        assertEquals(new Integer(1),  beans3.get(0), "pk1");
        assertEquals(new Integer(2),  beans3.get(0), "pk2");
        assertEquals(new Integer(1),  beans4.get(0), "pk");
        assertEquals(new Integer(3),  beans4.get(0), "fk1");
        assertEquals(new Integer(2),  beans4.get(0), "fk2");
    }

    /**
     * Tests the addition of a reference to a foreign key.
     */
    public void testAddReferenceToFK()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
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
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='pk2' type='DOUBLE' default='0.0' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='INTEGER' required='true'/>\n"+
            "    <column name='avalue2' type='DOUBLE' default='0.0' required='true'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue1' foreign='pk1'/>\n"+
            "      <reference local='avalue2' foreign='pk2'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip1", new Object[] { new Integer(1) });
        insertRow("roundtrip2", new Object[] { new Integer(2), new Integer(1) });

    	alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans1 = getRows("roundtrip1");
        List beans2 = getRows("roundtrip2");

        assertEquals(new Integer(1), beans1.get(0), "pk1");
        assertEquals(new Double(0.0), beans1.get(0), "pk2");
        assertEquals(new Integer(2), beans2.get(0), "pk");
        assertEquals(new Integer(1), beans2.get(0), "avalue1");
        assertEquals(new Double(0.0), beans2.get(0), "avalue2");
    }

    /**
     * Tests the removal of a reference from a foreign key.
     */
    public void testRemoveReferenceFromFK()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='pk2' type='VARCHAR' size='12' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue1' type='VARCHAR' size='12' required='true'/>\n"+
            "    <column name='avalue2' type='INTEGER' required='true'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue2' foreign='pk1'/>\n"+
            "      <reference local='avalue1' foreign='pk2'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue2' type='INTEGER' required='true'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue2' foreign='pk1'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip1", new Object[] { new Integer(1), "test" });
        insertRow("roundtrip2", new Object[] { new Integer(2), "test", new Integer(1) });

    	alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans1 = getRows("roundtrip1");
        List beans2 = getRows("roundtrip2");

        assertEquals(new Integer(1), beans1.get(0), "pk1");
        assertEquals(new Integer(2), beans2.get(0), "pk");
        assertEquals(new Integer(1), beans2.get(0), "avalue2");
    }

    /**
     * Tests the addition of a table.
     */
    public void testAddTable1()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
           "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='VARCHAR' size='20' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
           "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip1", new Object[] { new Integer(1) });

    	alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip1");

        assertEquals(new Integer(1), beans.get(0), "pk");
    }

    /**
     * Tests the addition of a table.
     */
    public void testAddTable2()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32' required='true'/>\n"+
            "  </table>\n"+
           "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
           "</database>";
        final String model3Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32' required='true'/>\n"+
            "    <foreign-key foreignTable='roundtrip2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
           "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip1", new Object[] { new Integer(1), "test" });

        alterDatabase(model2Xml);

        // note that we have to split the alteration because we can only add the foreign key if
        // there is a corresponding row in the new table

        insertRow("roundtrip2", new Object[] { "test" });

    	alterDatabase(model3Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans1 = getRows("roundtrip1");
        List beans2 = getRows("roundtrip2");

        assertEquals(new Integer(1), beans1.get(0), "pk");
        assertEquals((Object)"test", beans1.get(0), "avalue");
        assertEquals((Object)"test", beans2.get(0), "pk");
    }

    /**
     * Tests the addition of a table with an auto-increment primary key.
     */
    public void testAddAutoIncrementTable()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='VARCHAR' size='20' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
           "</database>";
        final String model2Xml; 

        // Sybase does not like INTEGER auto-increment columns
        if (SybasePlatform.DATABASENAME.equals(getPlatform().getName()))
        {
            model2Xml = 
                "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
                "  <table name='roundtrip1'>\n"+
                "    <column name='pk' type='VARCHAR' size='20' primaryKey='true' required='true'/>\n"+
                "  </table>\n"+
                "  <table name='roundtrip2'>\n"+
                "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' autoIncrement='true' required='true'/>\n"+
                "  </table>\n"+
               "</database>";
        }
        else
        {
            model2Xml = 
                "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
                "  <table name='roundtrip1'>\n"+
                "    <column name='pk' type='VARCHAR' size='20' primaryKey='true' required='true'/>\n"+
                "  </table>\n"+
                "  <table name='roundtrip2'>\n"+
                "    <column name='pk' type='INTEGER' primaryKey='true' autoIncrement='true' required='true'/>\n"+
                "  </table>\n"+
               "</database>";
        }
        createDatabase(model1Xml);
        
        insertRow("roundtrip1", new Object[] { "1" });

        alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip1");

        assertEquals((Object)"1", beans.get(0), "pk");
    }

    /**
     * Tests the removal of a table.
     */
    public void testRemoveTable1()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='DOUBLE' required='true'/>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip1", new Object[] { new Integer(1) });
        insertRow("roundtrip2", new Object[] { new Integer(2), new Double(2.0) });

    	alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip1");

        assertEquals(new Integer(1), beans.get(0), "pk");
    }

    /**
     * Tests the removal of a table.
     */
    public void testRemoveTable2()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
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
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='20' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip1", new Object[] { "test" });
        insertRow("roundtrip2", new Object[] { new Integer(1), "test" });

    	alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip2");

        assertEquals(new Integer(1), beans.get(0), "pk");
        assertEquals((Object)"test", beans.get(0), "avalue");
    }

    /**
     * Tests the removal of a table with an auto-increment column.
     */
    public void testRemoveTable3()
    {
        final String model1Xml;
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "</database>";

        // Sybase does not like INTEGER auto-increment columns
        if (SybasePlatform.DATABASENAME.equals(getPlatform().getName()))
        {
            model1Xml = 
                "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
                "  <table name='roundtrip'>\n"+
                "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true' autoIncrement='true'/>\n"+
                "    <column name='avalue' type='VARCHAR' size='20' required='true'/>\n"+
                "  </table>\n"+
                "</database>";
        }
        else
        {
            model1Xml = 
                "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
                "  <table name='roundtrip'>\n"+
                "    <column name='pk' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n"+
                "    <column name='avalue' type='VARCHAR' size='20' required='true'/>\n"+
                "  </table>\n"+
                "</database>";
        }

        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { null, "1" });

        alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));
    }

    /**
     * Test for DDLUTILS-54.
     */
    public void testIssue54() throws Exception
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='coltype'>\n" +
            "    <column name='COL_FLOAT' primaryKey='false' required='false' type='FLOAT'/>\n" +
            "    <column name='COL_BOOLEAN' primaryKey='false' required='false' type='BOOLEAN'/>\n" +
            "  </table>\n" +
            "</database>";

        createDatabase(modelXml);

        Properties props   = getTestProperties();
        String     catalog = props.getProperty(DDLUTILS_CATALOG_PROPERTY);
        String     schema  = props.getProperty(DDLUTILS_SCHEMA_PROPERTY);
        Database   model   = parseDatabaseFromString(modelXml);

        getPlatform().setSqlCommentsOn(false);

        String alterationSql = getPlatform().getAlterTablesSql(catalog, schema, null, model);

        assertEqualsIgnoringWhitespaces("", alterationSql);
    }

    /**
     * Test for DDLUTILS-159.
     */
    public void testRenamePK() throws Exception
    {
        final String model1Xml = 
            "<?xml version='1.0'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
            "  <table name='roundtrip'>\n" +
            "    <column name='id' primaryKey='true' required='true' type='INTEGER'/>\n" +
            "    <column name='avalue' primaryKey='false' required='false' type='VARCHAR' size='40'/>\n" +
            "  </table>\n" +
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
            "  <table name='roundtrip'>\n" +
            "    <column name='pk' primaryKey='true' required='true' type='INTEGER'/>\n" +
            "    <column name='avalue' primaryKey='false' required='false' type='VARCHAR' size='40'/>\n" +
            "  </table>\n" +
            "</database>";

        createDatabase(model1Xml);

        insertRow("roundtrip", new Object[] { new Integer(1), "test" });

        alterDatabase(model2Xml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));

        List beans = getRows("roundtrip");

        if (MckoiPlatform.DATABASENAME.equals(getPlatform().getName()))
        {
            // McKoi can actually handle this, though interestingly it will result in a null value for the pk
            assertEquals((Object)null,   beans.get(0), "pk");
            assertEquals((Object)"test", beans.get(0), "avalue");
        }
        else
        {
            assertTrue(beans.isEmpty());
        }
    }
}
