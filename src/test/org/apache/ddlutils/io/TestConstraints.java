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
        "    <column name='value_2' type='VARCHAR'/>\n"+
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
        "    <column name='pk_2' type='VARCHAR' primaryKey='true' required='true'/>\n"+
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
        "    <column name='pk' type='VARCHAR' primaryKey='true' required='true'/>\n"+
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
        "    <column name='pk_2' type='VARCHAR' primaryKey='true' required='true'/>\n"+
        "  </table>\n"+
        "  <table name='roundtrip_2'>\n"+
        "    <column name='pk' type='VARCHAR' primaryKey='true' required='true'/>\n"+
        "    <column name='value_1' type='INTEGER' required='true'/>\n"+
        "    <column name='value_2' type='INTEGER'/>\n"+
        "    <column name='value_3' type='VARCHAR'/>\n"+
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
        "    <column name='pk_2' type='VARCHAR' primaryKey='true' required='true'/>\n"+
        "    <column name='value_1' type='INTEGER'/>\n"+
        "    <column name='value_2' type='VARCHAR'/>\n"+
        "    <foreign-key foreignTable='roundtrip_2'>\n"+
        "      <reference local='value_1' foreign='pk_1'/>\n"+
        "      <reference local='value_2' foreign='pk_2'/>\n"+
        "    </foreign-key>\n"+
        "  </table>\n"+
        "  <table name='roundtrip_2'>\n"+
        "    <column name='pk_1' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='pk_2' type='VARCHAR' primaryKey='true' required='true'/>\n"+
        "    <column name='value_1' type='VARCHAR' required='true'/>\n"+
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
     * Tests a nullable column.
     * 
     * @param modelXml The model to be tested in XML form
     */
    protected void performConstraintsTest(String modelXml)
    {
        createDatabase(modelXml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests a nullable column. 
     */
    public void testNullableColumn()
    {
        performConstraintsTest(TEST_NULL_MODEL);
    }

    /**
     * Tests a not-nullable column. 
     */
    public void testNotNullableColumn()
    {
        performConstraintsTest(TEST_NOT_NULL_MODEL);
    }

    /**
     * Tests an auto-increment INTEGER column. 
     */
    public void testAutoIncrementIntegerColumn()
    {
        // only test this if the platform supports it
        if (getPlatformInfo().isSupportingNonPKIdentityColumns())
        {
            performConstraintsTest(TEST_AUTO_INCREMENT_INTEGER_MODEL);
        }
    }

    /**
     * Tests an auto-increment primary key column. 
     */
    public void testPrimaryKeyAutoIncrementColumn()
    {
        performConstraintsTest(TEST_PRIMARY_KEY_AUTO_INCREMENT_MODEL);
    }

    /**
     * Tests a simple index. 
     */
    public void testIndex()
    {
        if (getPlatformInfo().isSupportingNonUniqueIndices())
        {
            performConstraintsTest(TEST_INDEX_MODEL);
        }
    }

    /**
     * Tests an unique index for two columns. 
     */
    public void testUniqueIndex()
    {
        performConstraintsTest(TEST_UNIQUE_INDEX_MODEL);
    }

    /**
     * Tests an index for two columns, one of which a pk column. 
     */
    public void testPrimaryKeyIndex()
    {
        if (getPlatformInfo().isSupportingNonUniqueIndices())
        {
            performConstraintsTest(TEST_PRIMARY_KEY_INDEX_MODEL);
        }
    }

    /**
     * Tests two tables with a simple foreign key relationship between them. 
     */
    public void testSimpleForeignKey()
    {
        performConstraintsTest(TEST_SIMPLE_FOREIGN_KEY_MODEL);
    }

    /**
     * Tests two tables with overlapping foreign key relationships between them. 
     */
    public void testOverlappingForeignKeys()
    {
        performConstraintsTest(TEST_OVERLAPPING_FOREIGN_KEYS_MODEL);
    }

    /**
     * Tests two tables with circular foreign key relationships between them. 
     */
    public void testCircularForeignKeys()
    {
        performConstraintsTest(TEST_CIRCULAR_FOREIGN_KEYS_MODEL);
    }
}
