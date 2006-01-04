package org.apache.ddlutils.io.mysql;

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

import org.apache.ddlutils.io.TestConstraints;
import org.apache.ddlutils.platform.mysql.MySqlPlatform;

/**
 * Performs the roundtrip constraint tests against a PostgreSql database.
 * NOTE: On windows you have to set ower_case_table_names=2 in you my.ini to
 *       pass this test.
 * 
 * As a work around I've overrien the test_index_model, since above is not happening
 * in a consequent manner.
 *
 * @author Martin van den Bemt
 * @version $Revision: $
 */
public class TestMySql41Constraints extends TestConstraints
{

    /** 
     * Test model with a simple index.
     * Added OVERRIDE to the name to minimize confusion.
     */
    public static final String TEST_INDEX_MODEL_OVERRIDE = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='DOUBLE'/>\n"+
        "    <index name='TEST_INDEX'>\n"+
        "      <index-column name='VALUE'/>\n"+
        "    </index>\n"+
        "  </table>\n"+
        "</database>";

    /** Test model with a not-nullable column. */
    protected static final String TEST_NOT_NULL_MODEL_OVERRIDE = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='VARCHAR' required='true'/>\n"+
        "  </table>\n"+
        "</database>";

    /** Test model with an unique index with two columns. */
    protected static final String TEST_UNIQUE_INDEX_MODEL_OVERRIDE = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE1' type='DOUBLE'/>\n"+
        "    <column name='VALUE2' type='VARCHAR'/>\n"+
        "    <unique name='TEST_INDEX'>\n"+
        "      <unique-column name='VALUE2'/>\n"+
        "      <unique-column name='VALUE1'/>\n"+
        "    </unique>\n"+
        "  </table>\n"+
        "</database>";

    /** Test model with an index with two columns, one of which a pk field. */
    protected static final String TEST_PRIMARY_KEY_INDEX_MODEL_OVERRIDE = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip'>\n"+
        "    <column name='PK_1' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='PK_2' type='VARCHAR' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='DOUBLE'/>\n"+
        "    <index name='TEST_INDEX'>\n"+
        "      <index-column name='VALUE'/>\n"+
        "      <index-column name='PK_1'/>\n"+
        "    </index>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with two tables and a simple foreign key relationship between them. */
    protected static final String TEST_SIMPLE_FOREIGN_KEY_MODEL_OVERRIDE = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip_1'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "  </table>\n"+
        "  <table name='roundtrip_2'>\n"+
        "    <column name='PK' type='VARCHAR' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='INTEGER' required='true'/>\n"+
        "    <foreign-key foreignTable='roundtrip_1'>\n"+
        "      <reference local='VALUE' foreign='PK'/>\n"+
        "    </foreign-key>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with two tables and overlapping foreign keys between them. */
    protected static final String TEST_OVERLAPPING_FOREIGN_KEYS_MODEL_OVERRIDE = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip_1'>\n"+
        "    <column name='PK_1' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='PK_2' type='VARCHAR' primaryKey='true' required='true'/>\n"+
        "  </table>\n"+
        "  <table name='roundtrip_2'>\n"+
        "    <column name='PK' type='VARCHAR' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE_1' type='INTEGER' required='true'/>\n"+
        "    <column name='VALUE_2' type='INTEGER'/>\n"+
        "    <column name='VALUE_3' type='VARCHAR'/>\n"+
        "    <foreign-key name='FK_1' foreignTable='roundtrip_1'>\n"+
        "      <reference local='VALUE_1' foreign='PK_1'/>\n"+
        "      <reference local='VALUE_3' foreign='PK_2'/>\n"+
        "    </foreign-key>\n"+
        "    <foreign-key foreignTable='roundtrip_1'>\n"+
        "      <reference local='VALUE_2' foreign='PK_1'/>\n"+
        "      <reference local='VALUE_3' foreign='PK_2'/>\n"+
        "    </foreign-key>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with two tables and circular foreign key relationships between them. */
    protected static final String TEST_CIRCULAR_FOREIGN_KEYS_MODEL_OVERRIDE = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='roundtrip_1'>\n"+
        "    <column name='PK_1' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='PK_2' type='VARCHAR' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE_1' type='INTEGER'/>\n"+
        "    <column name='VALUE_2' type='VARCHAR'/>\n"+
        "    <foreign-key foreignTable='roundtrip_2'>\n"+
        "      <reference local='VALUE_1' foreign='PK_1'/>\n"+
        "      <reference local='VALUE_2' foreign='PK_2'/>\n"+
        "    </foreign-key>\n"+
        "  </table>\n"+
        "  <table name='roundtrip_2'>\n"+
        "    <column name='PK_1' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='PK_2' type='VARCHAR' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE_1' type='VARCHAR' required='true'/>\n"+
        "    <column name='VALUE_2' type='INTEGER' required='true'/>\n"+
        "    <foreign-key foreignTable='roundtrip_1'>\n"+
        "      <reference local='VALUE_2' foreign='PK_1'/>\n"+
        "      <reference local='VALUE_1' foreign='PK_2'/>\n"+
        "    </foreign-key>\n"+
        "  </table>\n"+
        "</database>";


    /**
     * Tests a simple index.
     * This test actually shouldn't pass :( 
     */
    public void testIndex()
    {
        if (getPlatformInfo().isSupportingNonUniqueIndices())
        {
            performConstraintsTest(TEST_INDEX_MODEL_OVERRIDE);
        }
    }

    /**
     * Tests a not-nullable column. 
     */
    public void testNotNullableColumn()
    {
        performConstraintsTest(TEST_NOT_NULL_MODEL_OVERRIDE);
    }

    /**
     * Tests an unique index for two columns. 
     */
    public void testUniqueIndex()
    {
        performConstraintsTest(TEST_UNIQUE_INDEX_MODEL_OVERRIDE);
    }

    /**
     * Tests an index for two columns, one of which a pk column. 
     */
    public void testPrimaryKeyIndex()
    {
        if (getPlatformInfo().isSupportingNonUniqueIndices())
        {
            performConstraintsTest(TEST_PRIMARY_KEY_INDEX_MODEL_OVERRIDE);
        }
    }

    /**
     * Tests two tables with a simple foreign key relationship between them. 
     */
    public void testSimpleForeignKey()
    {
        performConstraintsTest(TEST_SIMPLE_FOREIGN_KEY_MODEL_OVERRIDE);
    }

    /**
     * @see org.apache.ddlutils.io.RoundtripTestBase#getPlatformName()
     */
    protected String getPlatformName()
    {
        return MySqlPlatform.DATABASENAME;
    }

    /**
     * Tests two tables with overlapping foreign key relationships between them. 
     */
    public void testOverlappingForeignKeys()
    {
        performConstraintsTest(TEST_OVERLAPPING_FOREIGN_KEYS_MODEL_OVERRIDE);
    }

    /**
     * Tests two tables with circular foreign key relationships between them. 
     */
    public void testCircularForeignKeys()
    {
        performConstraintsTest(TEST_CIRCULAR_FOREIGN_KEYS_MODEL_OVERRIDE);
    }

}
