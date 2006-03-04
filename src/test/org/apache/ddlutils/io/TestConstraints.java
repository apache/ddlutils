package org.apache.ddlutils.io;

/*
 * Copyright 2005-2006 The Apache Software Foundation.
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

import org.apache.ddlutils.model.Database;

import junit.framework.Test;

/**
 * Performs the constraint tests.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class TestConstraints extends RoundtripTestBase
{
    /** Test model with a nullable column. */
    protected static final String TEST_NULL_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='INTEGER' required='false'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a not-nullable column. */
    protected static final String TEST_NOT_NULL_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='VARCHAR' required='true'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a auto-increment INTEGER column. */
    protected static final String TEST_AUTO_INCREMENT_INTEGER_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='INTEGER' required='true' autoIncrement='true'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a auto-increment DOUBLE column. */
    protected static final String TEST_AUTO_INCREMENT_DOUBLE_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='DOUBLE' required='true' autoIncrement='true'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a auto-increment primary key column. */
    protected static final String TEST_PRIMARY_KEY_AUTO_INCREMENT_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple index. */
    protected static final String TEST_INDEX_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='avalue' type='DOUBLE'/>\n"+
        "    <index name='TEST_INDEX'>\n"+
        "      <index-column name='avalue'/>\n"+
        "    </index>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with an unique index with two columns. */
    protected static final String TEST_UNIQUE_INDEX_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
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
    /** Test model with an index with two columns, one of which a pk field. */
    protected static final String TEST_PRIMARY_KEY_INDEX_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
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
    /** Test model with two tables and a simple foreign key relationship between them. */
    protected static final String TEST_SIMPLE_FOREIGN_KEY_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
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
    /** Test model with two tables and overlapping foreign keys between them. */
    protected static final String TEST_OVERLAPPING_FOREIGN_KEYS_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
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
    /** Test model with two tables and circular foreign key relationships between them. */
    protected static final String TEST_CIRCULAR_FOREIGN_KEYS_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
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
     * Tests a nullable column. 
     */
    public void testNullableColumn()
    {
        performConstraintsTest(TEST_NULL_MODEL, true);
    }

    /**
     * Tests a not-nullable column. 
     */
    public void testNotNullableColumn()
    {
        performConstraintsTest(TEST_NOT_NULL_MODEL, true);
    }

    /**
     * Tests an auto-increment INTEGER column. 
     */
    public void testAutoIncrementIntegerColumn()
    {
        // only test this if the platform supports it
        if (getPlatformInfo().isNonPKIdentityColumnsSupported())
        {
            performConstraintsTest(TEST_AUTO_INCREMENT_INTEGER_MODEL,
            		               getPlatformInfo().getAutoIncrementStatusReadingSupported());
        }
    }

    /**
     * Tests an auto-increment primary key column. 
     */
    public void testPrimaryKeyAutoIncrementColumn()
    {
        performConstraintsTest(TEST_PRIMARY_KEY_AUTO_INCREMENT_MODEL,
	                           getPlatformInfo().getAutoIncrementStatusReadingSupported());
    }

    /**
     * Tests a simple index. 
     */
    public void testIndex()
    {
        if (getPlatformInfo().isNonUniqueIndicesSupported())
        {
            performConstraintsTest(TEST_INDEX_MODEL, true);
        }
    }

    /**
     * Tests an unique index for two columns. 
     */
    public void testUniqueIndex()
    {
        performConstraintsTest(TEST_UNIQUE_INDEX_MODEL, true);
    }

    /**
     * Tests an index for two columns, one of which a pk column. 
     */
    public void testPrimaryKeyIndex()
    {
        if (getPlatformInfo().isNonUniqueIndicesSupported())
        {
            performConstraintsTest(TEST_PRIMARY_KEY_INDEX_MODEL, true);
        }
    }

    /**
     * Tests two tables with a simple foreign key relationship between them. 
     */
    public void testSimpleForeignKey()
    {
        performConstraintsTest(TEST_SIMPLE_FOREIGN_KEY_MODEL, true);
    }

    /**
     * Tests two tables with overlapping foreign key relationships between them. 
     */
    public void testOverlappingForeignKeys()
    {
        performConstraintsTest(TEST_OVERLAPPING_FOREIGN_KEYS_MODEL, true);
    }

    /**
     * Tests two tables with circular foreign key relationships between them. 
     */
    public void testCircularForeignKeys()
    {
        performConstraintsTest(TEST_CIRCULAR_FOREIGN_KEYS_MODEL, true);
    }
}
