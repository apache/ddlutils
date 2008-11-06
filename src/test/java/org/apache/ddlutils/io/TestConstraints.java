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

import junit.framework.Test;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.DdlUtilsException;
import org.apache.ddlutils.TestAgainstLiveDatabaseBase;
import org.apache.ddlutils.model.CascadeActionEnum;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.platform.sybase.SybasePlatform;

/**
 * Performs the constraint tests.
 * 
 * @version $Revision: 289996 $
 */
public class TestConstraints extends TestAgainstLiveDatabaseBase
{
    /**
     * Parameterized test case pattern.
     * 
     * @return The tests
     */
    public static Test suite() throws Exception
    {
        return getTests(TestConstraints.class);
    }
    
    /**
     * Tests a nullable column. Basically we're creating the test database
     * and then read it back and compare the original with the read one.
     * In addition we can also check that DdlUtils does not try to alter the new
     * database when using the <code>alterTables</code>/<code>getAlterTablesSql</code>
     * methods of the {@link org.apache.ddlutils.Platform} with the read-back model.
     * 
     * @param modelXml        The model to be tested in XML form
     * @param checkAlteration Whether to also check the alter tables sql
     */
    protected void performConstraintsTest(String modelXml, boolean checkAlteration)
    {
        createDatabase(modelXml);

        Database modelFromDb = readModelFromDatabase("roundtriptest");
        
        assertEquals(getAdjustedModel(),
        		     modelFromDb);

        if (checkAlteration)
        {
	        String alterTablesSql = getAlterTablesSql(modelFromDb).trim();
	
	        assertTrue(alterTablesSql.length() == 0);
        }
    }

    /**
     * Tests a table name that is longer than the maximum allowed.
     */
    public void testLongTableName()
    {
        if (getSqlBuilder().getMaxTableNameLength() == -1)
        {
            return;
        }

        String       tableName = StringUtils.repeat("Test", (getSqlBuilder().getMaxTableNameLength() / 4) + 3);
        final String modelXml  = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='" + tableName + "'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='false'/>\n"+
            "  </table>\n"+
            "</database>";

        performConstraintsTest(modelXml, true);
    }

    /**
     * Tests a column name that is longer than the maximum allowed.
     */
    public void testLongColumnName()
    {
        if (getPlatformInfo().getMaxColumnNameLength() == -1)
        {
            return;
        }

        String       columnName = StringUtils.repeat("Test", (getSqlBuilder().getMaxColumnNameLength() / 4) + 3);
        final String modelXml   = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='lengthtest'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='" + columnName + "' type='INTEGER' required='false'/>\n"+
            "  </table>\n"+
            "</database>";

        performConstraintsTest(modelXml, true);
    }

    /**
     * Tests a constraint name that is longer than the maximum allowed.
     */
    public void testLongConstraintName()
    {
        if (getSqlBuilder().getMaxConstraintNameLength() == -1)
        {
            return;
        }

        String       constraintName = StringUtils.repeat("Test", (getSqlBuilder().getMaxConstraintNameLength() / 4) + 3);
        final String modelXml       = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='DOUBLE'/>\n"+
            "    <index name='" + constraintName + "'>\n"+
            "      <index-column name='avalue'/>\n"+
            "    </index>\n"+
            "  </table>\n"+
            "</database>";

        performConstraintsTest(modelXml, true);
    }

    /**
     * Tests a foreign key name that is longer than the maximum allowed.
     */
    public void testLongForeignKeyName()
    {
        if (getSqlBuilder().getMaxForeignKeyNameLength() == -1)
        {
            return;
        }

        String       fkName   = StringUtils.repeat("Test", (getSqlBuilder().getMaxForeignKeyNameLength() / 4) + 3);
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip_1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip_2'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='true'/>\n"+
            "    <foreign-key name='" + fkName + "' foreignTable='roundtrip_1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        performConstraintsTest(modelXml, true);
    }

    /**
     * Tests a nullable column. 
     */
    public void testNullableColumn()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='false'/>\n"+
            "  </table>\n"+
            "</database>";

        performConstraintsTest(modelXml, true);
    }

    /**
     * Tests a not-nullable column. 
     */
    public void testNotNullableColumn()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        performConstraintsTest(modelXml, true);
    }

    /**
     * Tests an auto-increment INTEGER column. 
     */
    public void testAutoIncrementIntegerColumn()
    {
        // only test this if the platform supports it
        if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported())
        {
            return;
        }

        // we need special catering for Sybase which does not support identity for INTEGER columns
        final String modelXml; 

        if (SybasePlatform.DATABASENAME.equals(getPlatform().getName()))
        {
            modelXml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                       "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
                       "  <table name='roundtrip'>\n"+
                       "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
                       "    <column name='avalue' type='NUMERIC' size='12,0' required='true' autoIncrement='true'/>\n"+
                       "  </table>\n"+
                       "</database>";
        }
        else
        {
            modelXml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                       "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
                       "  <table name='roundtrip'>\n"+
                       "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
                       "    <column name='avalue' type='INTEGER' required='true' autoIncrement='true'/>\n"+
                       "  </table>\n"+
                       "</database>";
        }

        performConstraintsTest(modelXml,
                               getPlatformInfo().getIdentityStatusReadingSupported());
    }

    /**
     * Tests an auto-increment primary key column. 
     */
    public void testPrimaryKeyAutoIncrementColumn()
    {
        // we need special catering for Sybase which does not support identity for INTEGER columns
        final String modelXml; 

        if (SybasePlatform.DATABASENAME.equals(getPlatform().getName()))
        {
            modelXml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                       "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
                       "  <table name='roundtrip'>\n"+
                       "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true' autoIncrement='true'/>\n"+
                       "  </table>\n"+
                       "</database>";
        }
        else
        {
            modelXml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                       "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
                       "  <table name='roundtrip'>\n"+
                       "    <column name='pk' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n"+
                       "  </table>\n"+
                       "</database>";
        }

        performConstraintsTest(modelXml,
	                           getPlatformInfo().getIdentityStatusReadingSupported());
    }

    /**
     * Test for DDLUTILS-199. 
     */
    public void testAutoIncrementPrimaryKeyWithUnderscoreInName()
    {
        // we need special catering for Sybase which does not support identity for INTEGER columns
        final String modelXml; 

        if (SybasePlatform.DATABASENAME.equals(getPlatform().getName()))
        {
            modelXml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                       "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
                       "  <table name='roundtrip'>\n"+
                       "    <column name='PK_Column' type='NUMERIC' size='12,0' primaryKey='true' required='true' autoIncrement='true'/>\n"+
                       "  </table>\n"+
                       "</database>";
        }
        else
        {
            modelXml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
                       "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
                       "  <table name='roundtrip'>\n"+
                       "    <column name='PK_Column' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n"+
                       "  </table>\n"+
                       "</database>";
        }

        performConstraintsTest(modelXml,
                               getPlatformInfo().getIdentityStatusReadingSupported());
    }

    /**
     * Tests a simple index. 
     */
    public void testIndex()
    {
        if (!getPlatformInfo().isIndicesSupported())
        {
            return;
        }

        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='DOUBLE'/>\n"+
            "    <index name='TEST_INDEX'>\n"+
            "      <index-column name='avalue'/>\n"+
            "    </index>\n"+
            "  </table>\n"+
            "</database>";

        performConstraintsTest(modelXml, true);
    }

    /**
     * Tests an unique index for two columns. 
     */
    public void testUniqueIndex()
    {
        if (!getPlatformInfo().isIndicesSupported())
        {
            return;
        }

        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='value_1' type='DOUBLE'/>\n"+
            "    <column name='value_2' type='VARCHAR' size='32'/>\n"+
            "    <unique name='test_index'>\n"+
            "      <unique-column name='value_2'/>\n"+
            "      <unique-column name='value_1'/>\n"+
            "    </unique>\n"+
            "  </table>\n"+
            "</database>";

        performConstraintsTest(modelXml, true);
    }

    /**
     * Tests an index for two columns, one of which a pk column. 
     */
    public void testPrimaryKeyIndex()
    {
        if (!getPlatformInfo().isIndicesSupported())
        {
            return;
        }

        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk_1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='pk_2' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='DOUBLE'/>\n"+
            "    <index name='test_index'>\n"+
            "      <index-column name='avalue'/>\n"+
            "      <index-column name='pk_1'/>\n"+
            "    </index>\n"+
            "  </table>\n"+
            "</database>";

        performConstraintsTest(modelXml, true);
    }

    /**
     * Tests two tables with a simple foreign key relationship between them. 
     */
    public void testSimpleForeignKey()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip_1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip_2'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='true'/>\n"+
            "    <foreign-key foreignTable='roundtrip_1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        performConstraintsTest(modelXml, true);
    }

    /**
     * Tests two tables with overlapping foreign key relationships between them. 
     */
    public void testOverlappingForeignKeys()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip_1'>\n"+
            "    <column name='pk_1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='pk_2' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip_2'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='value_1' type='INTEGER' required='true'/>\n"+
            "    <column name='value_2' type='INTEGER'/>\n"+
            "    <column name='value_3' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key name='fk_1' foreignTable='roundtrip_1'>\n"+
            "      <reference local='value_1' foreign='pk_1'/>\n"+
            "      <reference local='value_3' foreign='pk_2'/>\n"+
            "    </foreign-key>\n"+
            "    <foreign-key foreignTable='roundtrip_1'>\n"+
            "      <reference local='value_2' foreign='pk_1'/>\n"+
            "      <reference local='value_3' foreign='pk_2'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        performConstraintsTest(modelXml, true);
    }

    /**
     * Tests two tables with circular foreign key relationships between them. 
     */
    public void testCircularForeignKeys()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip_1'>\n"+
            "    <column name='pk_1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='pk_2' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='value_1' type='INTEGER'/>\n"+
            "    <column name='value_2' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='roundtrip_2'>\n"+
            "      <reference local='value_1' foreign='pk_1'/>\n"+
            "      <reference local='value_2' foreign='pk_2'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='roundtrip_2'>\n"+
            "    <column name='pk_1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='pk_2' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='value_1' type='VARCHAR' size='32' required='true'/>\n"+
            "    <column name='value_2' type='INTEGER' required='true'/>\n"+
            "    <foreign-key foreignTable='roundtrip_1'>\n"+
            "      <reference local='value_2' foreign='pk_1'/>\n"+
            "      <reference local='value_1' foreign='pk_2'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        performConstraintsTest(modelXml, true);
    }

    /**
     * Tests two tables with a foreign key with a restrict onDelete action. 
     */
    public void testForeignKeyWithOnDeleteRestrict()
    {
        if (!getPlatformInfo().isActionSupportedForOnDelete(CascadeActionEnum.RESTRICT))
        {
            return;
        }

        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip_1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip_2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='true'/>\n"+
            "    <foreign-key foreignTable='roundtrip_1' onDelete='restrict'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        performConstraintsTest(modelXml, true);

        insertRow("roundtrip_1", new Object[] { new Integer(1) });
        insertRow("roundtrip_2", new Object[] { new Integer(5), new Integer(1) });

        List beansTable1 = getRows("roundtrip_1");
        List beansTable2 = getRows("roundtrip_2");

        assertEquals(1, beansTable1.size());
        assertEquals(1, beansTable2.size());
        assertEquals(new Integer(1), beansTable1.get(0), "pk");
        assertEquals(new Integer(5), beansTable2.get(0), "pk");
        assertEquals(new Integer(1), beansTable2.get(0), "avalue");

        try
        {
            deleteRow("roundtrip_1", new Object[] { new Integer(1) });
            fail();
        }
        catch (DdlUtilsException ex)
        {}
    }

    /**
     * Tests two tables with a foreign key with a cascade onDelete action. 
     */
    public void testForeignKeyWithOnDeleteCascade()
    {
        if (!getPlatformInfo().isActionSupportedForOnDelete(CascadeActionEnum.CASCADE))
        {
            return;
        }

        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip_1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip_2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='true'/>\n"+
            "    <foreign-key foreignTable='roundtrip_1' onDelete='cascade'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        performConstraintsTest(modelXml, true);

        insertRow("roundtrip_1", new Object[] { new Integer(1) });
        insertRow("roundtrip_2", new Object[] { new Integer(5), new Integer(1) });

        List beansTable1 = getRows("roundtrip_1");
        List beansTable2 = getRows("roundtrip_2");

        assertEquals(1, beansTable1.size());
        assertEquals(1, beansTable2.size());
        assertEquals(new Integer(1), beansTable1.get(0), "pk");
        assertEquals(new Integer(5), beansTable2.get(0), "pk");
        assertEquals(new Integer(1), beansTable2.get(0), "avalue");

        deleteRow("roundtrip_1", new Object[] { new Integer(1) });

        beansTable1 = getRows("roundtrip_1");
        beansTable2 = getRows("roundtrip_2");

        assertEquals(0, beansTable1.size());
        assertEquals(0, beansTable2.size());
    }

    /**
     * Tests two tables with a foreign key with a set-null onDelete action. 
     */
    public void testForeignKeyWithOnDeleteSetNull()
    {
        if (!getPlatformInfo().isActionSupportedForOnDelete(CascadeActionEnum.SET_NULL))
        {
            return;
        }

        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip_1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip_2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='false'/>\n"+
            "    <foreign-key foreignTable='roundtrip_1' onDelete='setnull'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        performConstraintsTest(modelXml, true);

        insertRow("roundtrip_1", new Object[] { new Integer(1) });
        insertRow("roundtrip_2", new Object[] { new Integer(5), new Integer(1) });

        List beansTable1 = getRows("roundtrip_1");
        List beansTable2 = getRows("roundtrip_2");

        assertEquals(1, beansTable1.size());
        assertEquals(1, beansTable2.size());
        assertEquals(new Integer(1), beansTable1.get(0), "pk");
        assertEquals(new Integer(5), beansTable2.get(0), "pk");
        assertEquals(new Integer(1), beansTable2.get(0), "avalue");

        deleteRow("roundtrip_1", new Object[] { new Integer(1) });

        beansTable1 = getRows("roundtrip_1");
        beansTable2 = getRows("roundtrip_2");

        assertEquals(0, beansTable1.size());
        assertEquals(1, beansTable2.size());
        assertEquals(new Integer(5), beansTable2.get(0), "pk");
        assertEquals((Object)null, beansTable2.get(0), "avalue");
    }

    /**
     * Tests two tables with a foreign key with a set-default onDelete action. 
     */
    public void testForeignKeyWithOnDeleteSetDefault()
    {
        if (!getPlatformInfo().isActionSupportedForOnDelete(CascadeActionEnum.SET_DEFAULT))
        {
            return;
        }

        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip_1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip_2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='false' default='2'/>\n"+
            "    <foreign-key foreignTable='roundtrip_1' onDelete='setdefault'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        performConstraintsTest(modelXml, true);

        insertRow("roundtrip_1", new Object[] { new Integer(1) });
        insertRow("roundtrip_1", new Object[] { new Integer(2) });
        insertRow("roundtrip_2", new Object[] { new Integer(5), new Integer(1) });

        List beansTable1 = getRows("roundtrip_1");
        List beansTable2 = getRows("roundtrip_2");

        assertEquals(2, beansTable1.size());
        assertEquals(1, beansTable2.size());
        assertEquals(new Integer(1), beansTable1.get(0), "pk");
        assertEquals(new Integer(2), beansTable1.get(1), "pk");
        assertEquals(new Integer(5), beansTable2.get(0), "pk");
        assertEquals(new Integer(1), beansTable2.get(0), "avalue");

        deleteRow("roundtrip_1", new Object[] { new Integer(1) });

        beansTable1 = getRows("roundtrip_1");
        beansTable2 = getRows("roundtrip_2");

        assertEquals(1, beansTable1.size());
        assertEquals(1, beansTable2.size());
        assertEquals(new Integer(2), beansTable1.get(0), "pk");
        assertEquals(new Integer(5), beansTable2.get(0), "pk");
        assertEquals(new Integer(2), beansTable2.get(0), "avalue");
    }

    /**
     * Tests two tables with a foreign key with a restrict onUpdate action. 
     */
    public void testForeignKeyWithOnUpdateRestrict()
    {
        if (!getPlatformInfo().isActionSupportedForOnUpdate(CascadeActionEnum.RESTRICT))
        {
            return;
        }

        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip_1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip_2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='true'/>\n"+
            "    <foreign-key foreignTable='roundtrip_1' onUpdate='restrict'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        performConstraintsTest(modelXml, true);

        insertRow("roundtrip_1", new Object[] { new Integer(1) });
        insertRow("roundtrip_2", new Object[] { new Integer(5), new Integer(1) });

        List beansTable1 = getRows("roundtrip_1");
        List beansTable2 = getRows("roundtrip_2");

        assertEquals(1, beansTable1.size());
        assertEquals(1, beansTable2.size());
        assertEquals(new Integer(1), beansTable1.get(0), "pk");
        assertEquals(new Integer(5), beansTable2.get(0), "pk");
        assertEquals(new Integer(1), beansTable2.get(0), "avalue");

        try
        {
            updateRow("roundtrip_1", (DynaBean)beansTable1.get(0), new Object[] { new Integer(5) });
            fail();
        }
        catch (DdlUtilsException ex)
        {}
    }

    /**
     * Tests two tables with a foreign key with a cascade onUpdate action. 
     */
    public void testForeignKeyWithOnUpdateCascade()
    {
        if (!getPlatformInfo().isActionSupportedForOnUpdate(CascadeActionEnum.CASCADE))
        {
            return;
        }

        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip_1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip_2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='true'/>\n"+
            "    <foreign-key foreignTable='roundtrip_1' onUpdate='cascade'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        performConstraintsTest(modelXml, true);

        insertRow("roundtrip_1", new Object[] { new Integer(1) });
        insertRow("roundtrip_2", new Object[] { new Integer(5), new Integer(1) });

        List beansTable1 = getRows("roundtrip_1");
        List beansTable2 = getRows("roundtrip_2");

        assertEquals(1, beansTable1.size());
        assertEquals(1, beansTable2.size());
        assertEquals(new Integer(1), beansTable1.get(0), "pk");
        assertEquals(new Integer(5), beansTable2.get(0), "pk");
        assertEquals(new Integer(1), beansTable2.get(0), "avalue");

        updateRow("roundtrip_1", (DynaBean)beansTable1.get(0), new Object[] { new Integer(2) });

        beansTable1 = getRows("roundtrip_1");
        beansTable2 = getRows("roundtrip_2");

        assertEquals(1, beansTable1.size());
        assertEquals(1, beansTable2.size());
        assertEquals(new Integer(2), beansTable1.get(0), "pk");
        assertEquals(new Integer(5), beansTable2.get(0), "pk");
        assertEquals(new Integer(2), beansTable2.get(0), "avalue");
    }

    /**
     * Tests two tables with a foreign key with a set-null onUpdate action. 
     */
    public void testForeignKeyWithOnUpdateSetNull()
    {
        if (!getPlatformInfo().isActionSupportedForOnUpdate(CascadeActionEnum.SET_NULL))
        {
            return;
        }

        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip_1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip_2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='false'/>\n"+
            "    <foreign-key foreignTable='roundtrip_1' onUpdate='setnull'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        performConstraintsTest(modelXml, true);

        insertRow("roundtrip_1", new Object[] { new Integer(1) });
        insertRow("roundtrip_2", new Object[] { new Integer(5), new Integer(1) });

        List beansTable1 = getRows("roundtrip_1");
        List beansTable2 = getRows("roundtrip_2");

        assertEquals(1, beansTable1.size());
        assertEquals(1, beansTable2.size());
        assertEquals(new Integer(1), beansTable1.get(0), "pk");
        assertEquals(new Integer(5), beansTable2.get(0), "pk");
        assertEquals(new Integer(1), beansTable2.get(0), "avalue");

        updateRow("roundtrip_1", (DynaBean)beansTable1.get(0), new Object[] { new Integer(2) });

        beansTable1 = getRows("roundtrip_1");
        beansTable2 = getRows("roundtrip_2");

        assertEquals(1, beansTable1.size());
        assertEquals(1, beansTable2.size());
        assertEquals(new Integer(2), beansTable1.get(0), "pk");
        assertEquals(new Integer(5), beansTable2.get(0), "pk");
        assertEquals((Object)null, beansTable2.get(0), "avalue");
    }

    /**
     * Tests two tables with a foreign key with a det-default onUpdate action. 
     */
    public void testForeignKeyWithOnUpdateSetDefault()
    {
        if (!getPlatformInfo().isActionSupportedForOnUpdate(CascadeActionEnum.SET_DEFAULT))
        {
            return;
        }

        final String modelXml =
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip_1'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip_2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER' required='false' default='1'/>\n"+
            "    <foreign-key foreignTable='roundtrip_1' onUpdate='setdefault'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        performConstraintsTest(modelXml, true);

        insertRow("roundtrip_1", new Object[] { new Integer(1) });
        insertRow("roundtrip_1", new Object[] { new Integer(2) });
        insertRow("roundtrip_2", new Object[] { new Integer(5), new Integer(2) });

        List beansTable1 = getRows("roundtrip_1");
        List beansTable2 = getRows("roundtrip_2");

        assertEquals(2, beansTable1.size());
        assertEquals(1, beansTable2.size());
        assertEquals(new Integer(1), beansTable1.get(0), "pk");
        assertEquals(new Integer(2), beansTable1.get(1), "pk");
        assertEquals(new Integer(5), beansTable2.get(0), "pk");
        assertEquals(new Integer(2), beansTable2.get(0), "avalue");

        updateRow("roundtrip_1", (DynaBean)beansTable1.get(1), new Object[] { new Integer(0) });

        beansTable1 = getRows("roundtrip_1", "pk");
        beansTable2 = getRows("roundtrip_2", "pk");

        assertEquals(2, beansTable1.size());
        assertEquals(1, beansTable2.size());
        assertEquals(new Integer(0), beansTable1.get(0), "pk");
        assertEquals(new Integer(1), beansTable1.get(1), "pk");
        assertEquals(new Integer(5), beansTable2.get(0), "pk");
        assertEquals(new Integer(1), beansTable2.get(0), "avalue");
    }
}
