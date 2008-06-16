package org.apache.ddlutils.alteration;

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

import java.sql.Types;
import java.util.List;

import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;

/**
 * Tests the model comparison.
 * 
 * TODO: need tests with indexes without a name
 * 
 * @version $Revision: $
 */
public class TestIndexComparison extends TestComparisonBase
{
    /**
     * Tests the addition of an index with one column.
     */
    public void testAddSingleColumnIndex1()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='INTEGER'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL' type='INTEGER'/>\n" +
            "    <index name='TESTINDEX'>\n" +
            "      <index-column name='COL'/>\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        AddIndexChange change = (AddIndexChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertIndex("TESTINDEX", false, new String[] { "Col" },
                    change.getNewIndex());
    }

    /**
     * Tests the addition of an index with one column.
     */
    public void testAddSingleColumnIndex2()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL' type='INTEGER'/>\n" +
            "    <index name='TESTINDEX'>\n" +
            "      <index-column name='COL'/>\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(2,
                     changes.size());

        AddColumnChange colChange   = (AddColumnChange)changes.get(0);
        AddIndexChange  indexChange = (AddIndexChange)changes.get(1);

        assertEquals("TableA",
                     colChange.getChangedTable());
        assertColumn("COL", Types.INTEGER, null, null, false, false, false,
                     colChange.getNewColumn());
        assertEquals("ColPK",
                     colChange.getPreviousColumn());
        assertNull(colChange.getNextColumn());

        assertEquals("TableA",
                     indexChange.getChangedTable());
        assertIndex("TESTINDEX", false, new String[] { "COL" },
                     indexChange.getNewIndex());
    }

    /**
     * Tests the addition of an index with multiple columns.
     */
    public void testAddMultiColumnIndex1()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col1' type='INTEGER'/>\n" +
            "    <column name='Col2' type='DOUBLE'/>\n" +
            "    <column name='Col3' type='VARCHAR' size='32'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL1' type='INTEGER'/>\n" +
            "    <column name='COL2' type='DOUBLE'/>\n" +
            "    <column name='COL3' type='VARCHAR' size='32'/>\n" +
            "    <unique name='TESTINDEX'>\n" +
            "      <unique-column name='COL3'/>\n" +
            "      <unique-column name='COL1'/>\n" +
            "      <unique-column name='COL2'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        AddIndexChange change = (AddIndexChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertIndex("TESTINDEX", true, new String[] { "Col3", "Col1", "Col2" },
                    change.getNewIndex());
    }

    /**
     * Tests the addition of an index with multiple columns.
     */
    public void testAddMultiColumnIndex2()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL1' type='INTEGER'/>\n" +
            "    <column name='COL2' type='DOUBLE'/>\n" +
            "    <column name='COL3' type='VARCHAR' size='32'/>\n" +
            "    <unique name='TESTINDEX'>\n" +
            "      <unique-column name='COL3'/>\n" +
            "      <unique-column name='COL1'/>\n" +
            "      <unique-column name='COL2'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(4,
                     changes.size());

        AddColumnChange colChange1  = (AddColumnChange)changes.get(0);
        AddColumnChange colChange2  = (AddColumnChange)changes.get(1);
        AddColumnChange colChange3  = (AddColumnChange)changes.get(2);
        AddIndexChange  indexChange = (AddIndexChange)changes.get(3);

        assertEquals("TableA",
                     colChange1.getChangedTable());
        assertColumn("COL1", Types.INTEGER, null, null, false, false, false,
                     colChange1.getNewColumn());
        assertEquals("ColPK",
                     colChange1.getPreviousColumn());
        assertNull(colChange1.getNextColumn());

        assertEquals("TableA",
                     colChange2.getChangedTable());
        assertColumn("COL2", Types.DOUBLE, null, null, false, false, false,
                     colChange2.getNewColumn());
        assertEquals("COL1",
                     colChange2.getPreviousColumn());
        assertNull(colChange2.getNextColumn());

        assertEquals("TableA",
                     colChange3.getChangedTable());
        assertColumn("COL3", Types.VARCHAR, "32", null, false, false, false,
                     colChange3.getNewColumn());
        assertEquals("COL2",
                     colChange3.getPreviousColumn());
        assertNull(colChange3.getNextColumn());

        assertEquals("TableA",
                     indexChange.getChangedTable());
        assertIndex("TESTINDEX", true, new String[] { "COL3", "COL1", "COL2" },
                    indexChange.getNewIndex());
    }

    /**
     * Tests the addition of a column into an existing index with multiple columns.
     */
    public void testAddNewColumnToMultiColumnIndex()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL1' type='INTEGER'/>\n" +
            "    <column name='COL3' type='VARCHAR' size='32'/>\n" +
            "    <unique name='TESTINDEX'>\n" +
            "      <unique-column name='COL3'/>\n" +
            "      <unique-column name='COL1'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL1' type='INTEGER'/>\n" +
            "    <column name='COL2' type='DOUBLE'/>\n" +
            "    <column name='COL3' type='VARCHAR' size='32'/>\n" +
            "    <unique name='TESTINDEX'>\n" +
            "      <unique-column name='COL3'/>\n" +
            "      <unique-column name='COL1'/>\n" +
            "      <unique-column name='COL2'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(3,
                     changes.size());

        RemoveIndexChange indexChange1 = (RemoveIndexChange)changes.get(0);
        AddColumnChange   colChange    = (AddColumnChange)changes.get(1);
        AddIndexChange    indexChange2 = (AddIndexChange)changes.get(2);

        assertEquals("TableA",
                     indexChange1.getChangedTable());
        assertEquals(model1.findTable("TableA").getIndex(0),
                     indexChange1.findChangedIndex(model1, false));

        assertEquals("TableA",
                     colChange.getChangedTable());
        assertColumn("COL2", Types.DOUBLE, null, null, false, false, false,
                     colChange.getNewColumn());
        assertEquals("COL1",
                     colChange.getPreviousColumn());
        assertEquals("COL3",
                     colChange.getNextColumn());

        assertEquals("TableA",
                     indexChange2.getChangedTable());
        assertIndex("TESTINDEX", true, new String[] { "COL3", "COL1", "COL2" },
                    indexChange2.getNewIndex());
    }

    /**
     * Tests the addition of columns to an existing index with a single column.
     */
    public void testAddNewColumnsToSingleColumnIndex()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL3' type='VARCHAR' size='32'/>\n" +
            "    <unique name='TESTINDEX'>\n" +
            "      <unique-column name='COL3'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL1' type='INTEGER'/>\n" +
            "    <column name='COL2' type='DOUBLE'/>\n" +
            "    <column name='COL3' type='VARCHAR' size='32'/>\n" +
            "    <unique name='TESTINDEX'>\n" +
            "      <unique-column name='COL1'/>\n" +
            "      <unique-column name='COL3'/>\n" +
            "      <unique-column name='COL2'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(4,
                     changes.size());

        RemoveIndexChange indexChange1 = (RemoveIndexChange)changes.get(0);
        AddColumnChange   colChange1   = (AddColumnChange)changes.get(1);
        AddColumnChange   colChange2   = (AddColumnChange)changes.get(2);
        AddIndexChange    indexChange2 = (AddIndexChange)changes.get(3);

        assertEquals("TableA",
                     indexChange1.getChangedTable());
        assertEquals(model1.findTable("TableA").getIndex(0),
                     indexChange1.findChangedIndex(model1, false));

        assertEquals("TableA",
                     colChange1.getChangedTable());
        assertColumn("COL1", Types.INTEGER, null, null, false, false, false,
                     colChange1.getNewColumn());
        assertEquals("ColPK",
                     colChange1.getPreviousColumn());
        assertEquals("COL3",
                     colChange1.getNextColumn());

        assertEquals("TableA",
                     colChange2.getChangedTable());
        assertColumn("COL2", Types.DOUBLE, null, null, false, false, false,
                     colChange2.getNewColumn());
        assertEquals("COL1",
                     colChange2.getPreviousColumn());
        assertEquals("COL3",
                     colChange2.getNextColumn());

        assertEquals("TableA",
                     indexChange2.getChangedTable());
        assertIndex("TESTINDEX", true, new String[] { "COL1", "COL3", "COL2" },
                    indexChange2.getNewIndex());
    }

    /**
     * Tests the addition of a column to an index with multiple columns.
     */
    public void testAddColumnToMultiColumnIndex()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL1' type='INTEGER'/>\n" +
            "    <column name='COL2' type='DOUBLE'/>\n" +
            "    <column name='COL3' type='VARCHAR' size='32'/>\n" +
            "    <index name='TESTINDEX'>\n" +
            "      <index-column name='COL3'/>\n" +
            "      <index-column name='COL2'/>\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL1' type='INTEGER'/>\n" +
            "    <column name='COL2' type='DOUBLE'/>\n" +
            "    <column name='COL3' type='VARCHAR' size='32'/>\n" +
            "    <index name='TESTINDEX'>\n" +
            "      <index-column name='COL3'/>\n" +
            "      <index-column name='COL1'/>\n" +
            "      <index-column name='COL2'/>\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(2,
                     changes.size());

        RemoveIndexChange indexChange1 = (RemoveIndexChange)changes.get(0);
        AddIndexChange    indexChange2 = (AddIndexChange)changes.get(1);

        assertEquals("TableA",
                     indexChange1.getChangedTable());
        assertEquals(model1.findTable("TableA").getIndex(0),
                     indexChange1.findChangedIndex(model1, false));

        assertEquals("TableA",
                     indexChange2.getChangedTable());
        assertIndex("TESTINDEX", false, new String[] { "COL3", "COL1", "COL2" },
                    indexChange2.getNewIndex());
    }

    /**
     * Tests the addition of columns to an index with a single column.
     */
    public void testAddColumnsToSingleColumnIndex()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL1' type='INTEGER'/>\n" +
            "    <column name='COL2' type='DOUBLE'/>\n" +
            "    <column name='COL3' type='VARCHAR' size='32'/>\n" +
            "    <unique name='TESTINDEX'>\n" +
            "      <unique-column name='COL1'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL1' type='INTEGER'/>\n" +
            "    <column name='COL2' type='DOUBLE'/>\n" +
            "    <column name='COL3' type='VARCHAR' size='32'/>\n" +
            "    <unique name='TESTINDEX'>\n" +
            "      <unique-column name='COL3'/>\n" +
            "      <unique-column name='COL1'/>\n" +
            "      <unique-column name='COL2'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(2,
                     changes.size());

        RemoveIndexChange indexChange1 = (RemoveIndexChange)changes.get(0);
        AddIndexChange    indexChange2 = (AddIndexChange)changes.get(1);

        assertEquals("TableA",
                     indexChange1.getChangedTable());
        assertEquals(model1.findTable("TableA").getIndex(0),
                     indexChange1.findChangedIndex(model1, false));

        assertEquals("TableA",
                     indexChange2.getChangedTable());
        assertIndex("TESTINDEX", true, new String[] { "COL3", "COL1", "COL2" },
                    indexChange2.getNewIndex());
    }

    /**
     * Tests the addition and removal of an index because of the change of column order.
     */
    public void testChangeIndexColumnOrder()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col1' type='INTEGER'/>\n" +
            "    <column name='Col2' type='DOUBLE'/>\n" +
            "    <unique name='TestIndex'>\n" +
            "      <unique-column name='Col1'/>\n" +
            "      <unique-column name='Col2'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col1' type='INTEGER'/>\n" +
            "    <column name='Col2' type='DOUBLE'/>\n" +
            "    <unique name='TestIndex'>\n" +
            "      <unique-column name='Col2'/>\n" +
            "      <unique-column name='Col1'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(2,
                     changes.size());

        RemoveIndexChange change1 = (RemoveIndexChange)changes.get(0);
        AddIndexChange    change2 = (AddIndexChange)changes.get(1);

        assertEquals("TableA",
                     change1.getChangedTable());
        assertEquals(model1.findTable("TableA").getIndex(0),
                     change1.findChangedIndex(model1, false));

        assertEquals("TableA",
                     change2.getChangedTable());
        assertIndex("TestIndex", true, new String[] { "Col2", "Col1" }, 
                     change2.getNewIndex());
    }
    
    /**
     * Tests the recreation of an index because of the addition of an index column.
     */
    public void testAddIndexColumn()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col1' type='INTEGER'/>\n" +
            "    <column name='Col2' type='DOUBLE'/>\n" +
            "    <index name='TestIndex'>\n" +
            "      <index-column name='Col1'/>\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col1' type='INTEGER'/>\n" +
            "    <column name='Col2' type='DOUBLE'/>\n" +
            "    <index name='TestIndex'>\n" +
            "      <index-column name='Col1'/>\n" +
            "      <index-column name='Col2'/>\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(2,
                     changes.size());

        RemoveIndexChange change1 = (RemoveIndexChange)changes.get(0);
        AddIndexChange    change2 = (AddIndexChange)changes.get(1);

        assertEquals("TableA",
                     change1.getChangedTable());
        assertEquals(model1.findTable("TableA").getIndex(0),
                     change1.findChangedIndex(model1, false));

        assertEquals("TableA",
                     change2.getChangedTable());
        assertIndex("TestIndex", false, new String[] { "Col1", "Col2" }, 
                     change2.getNewIndex());
    }
   
    /**
     * Tests the addition and removal of an index because of the removal of an index column.
     */
    public void testRemoveIndexColumn()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col1' type='INTEGER'/>\n" +
            "    <column name='Col2' type='DOUBLE'/>\n" +
            "    <index name='TestIndex'>\n" +
            "      <index-column name='Col1'/>\n" +
            "      <index-column name='Col2'/>\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col1' type='INTEGER'/>\n" +
            "    <column name='Col2' type='DOUBLE'/>\n" +
            "    <index name='TestIndex'>\n" +
            "      <index-column name='Col1'/>\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(2,
                     changes.size());

        RemoveIndexChange change1 = (RemoveIndexChange)changes.get(0);
        AddIndexChange    change2 = (AddIndexChange)changes.get(1);

        assertEquals("TableA",
                     change1.getChangedTable());
        assertEquals(model1.findTable("TableA").getIndex(0),
                     change1.findChangedIndex(model1, false));

        assertEquals("TableA",
                     change2.getChangedTable());
        assertIndex("TestIndex", false, new String[] { "Col1" }, 
                    change2.getNewIndex());
    }

    /**
     * Tests changing the type of an index.
     */
    public void testChangeIndexType()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col1' type='INTEGER'/>\n" +
            "    <column name='Col2' type='DOUBLE'/>\n" +
            "    <index name='TestIndex'>\n" +
            "      <index-column name='Col1'/>\n" +
            "      <index-column name='Col2'/>\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col1' type='INTEGER'/>\n" +
            "    <column name='Col2' type='DOUBLE'/>\n" +
            "    <unique name='TestIndex'>\n" +
            "      <unique-column name='Col1'/>\n" +
            "      <unique-column name='Col2'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(2,
                     changes.size());

        RemoveIndexChange change1 = (RemoveIndexChange)changes.get(0);
        AddIndexChange    change2 = (AddIndexChange)changes.get(1);

        assertEquals("TableA",
                     change1.getChangedTable());
        assertEquals(model1.findTable("TableA").getIndex(0),
                     change1.findChangedIndex(model1, false));

        assertEquals("TableA",
                     change2.getChangedTable());
        assertIndex("TestIndex", true, new String[] { "Col1", "Col2" }, 
                    change2.getNewIndex());
    }

    /**
     * Tests the removal of a column that is the single column in an index.
     */
    public void testDropColumnFromSingleColumnIndex()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL1' type='INTEGER'/>\n" +
            "    <index name='TESTINDEX'>\n" +
            "      <index-column name='COL1'/>\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(2,
                     changes.size());

        RemoveIndexChange  indexChange = (RemoveIndexChange)changes.get(0);
        RemoveColumnChange colChange   = (RemoveColumnChange)changes.get(1);

        assertEquals("TableA",
                     indexChange.getChangedTable());
        assertEquals(model1.findTable("TableA").getIndex(0),
                     indexChange.findChangedIndex(model1, false));

        assertEquals("TableA",
                     colChange.getChangedTable());
        assertEquals("COL1",
                     colChange.getChangedColumn());
    }

    /**
     * Tests the removal of a column that is part of an index.
     */
    public void testDropColumnFromMultiColumnIndex()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL1' type='INTEGER'/>\n" +
            "    <column name='COL2' type='DOUBLE'/>\n" +
            "    <column name='COL3' type='VARCHAR' size='32'/>\n" +
            "    <unique name='TESTINDEX'>\n" +
            "      <unique-column name='COL3'/>\n" +
            "      <unique-column name='COL2'/>\n" +
            "      <unique-column name='COL1'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL1' type='INTEGER'/>\n" +
            "    <column name='COL3' type='VARCHAR' size='32'/>\n" +
            "    <unique name='TESTINDEX'>\n" +
            "      <unique-column name='COL3'/>\n" +
            "      <unique-column name='COL1'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(3,
                     changes.size());

        RemoveIndexChange  indexChange1 = (RemoveIndexChange)changes.get(0);
        RemoveColumnChange colChange    = (RemoveColumnChange)changes.get(1);
        AddIndexChange     indexChange2 = (AddIndexChange)changes.get(2);

        assertEquals("TableA",
                     indexChange1.getChangedTable());
        assertEquals(model1.findTable("TableA").getIndex(0),
                     indexChange1.findChangedIndex(model1, false));

        assertEquals("TableA",
                     colChange.getChangedTable());
        assertEquals("COL2",
                     colChange.getChangedColumn());

        assertEquals("TableA",
                     indexChange2.getChangedTable());
        assertIndex("TESTINDEX", true, new String[] { "COL3", "COL1" },
                    indexChange2.getNewIndex());
    }

    /**
     * Tests the addition of a column and changing the order of the columns in an index.
     */
    public void testAddColumnAndChangeIndexColumnOrder()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL1' type='INTEGER'/>\n" +
            "    <column name='COL3' type='VARCHAR' size='32'/>\n" +
            "    <unique name='TESTINDEX'>\n" +
            "      <unique-column name='COL3'/>\n" +
            "      <unique-column name='COL1'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL1' type='INTEGER'/>\n" +
            "    <column name='COL2' type='DOUBLE'/>\n" +
            "    <column name='COL3' type='VARCHAR' size='32'/>\n" +
            "    <unique name='TESTINDEX'>\n" +
            "      <unique-column name='COL1'/>\n" +
            "      <unique-column name='COL2'/>\n" +
            "      <unique-column name='COL3'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(3,
                     changes.size());

        RemoveIndexChange indexChange1 = (RemoveIndexChange)changes.get(0);
        AddColumnChange   colChange    = (AddColumnChange)changes.get(1);
        AddIndexChange    indexChange2 = (AddIndexChange)changes.get(2);

        assertEquals("TableA",
                     indexChange1.getChangedTable());
        assertEquals(model1.findTable("TableA").getIndex(0),
                     indexChange1.findChangedIndex(model1, false));

        assertEquals("TableA",
                     colChange.getChangedTable());
        assertColumn("COL2", Types.DOUBLE, null, null, false, false, false,
                     colChange.getNewColumn());
        assertEquals("COL1",
                     colChange.getPreviousColumn());
        assertEquals("COL3",
                     colChange.getNextColumn());

        assertEquals("TableA",
                     indexChange2.getChangedTable());
        assertIndex("TESTINDEX", true, new String[] { "COL1", "COL2", "COL3" },
                    indexChange2.getNewIndex());
    }

    /**
     * Tests the removal of a column and changing the order of the columns in an index.
     */
    public void testDropColumnAndChangeIndexColumnOrder()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL1' type='INTEGER'/>\n" +
            "    <column name='COL2' type='DOUBLE'/>\n" +
            "    <column name='COL3' type='VARCHAR' size='32'/>\n" +
            "    <unique name='TESTINDEX'>\n" +
            "      <unique-column name='COL3'/>\n" +
            "      <unique-column name='COL2'/>\n" +
            "      <unique-column name='COL1'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL1' type='INTEGER'/>\n" +
            "    <column name='COL3' type='VARCHAR' size='32'/>\n" +
            "    <unique name='TESTINDEX'>\n" +
            "      <unique-column name='COL1'/>\n" +
            "      <unique-column name='COL3'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(3,
                     changes.size());

        RemoveIndexChange  indexChange1 = (RemoveIndexChange)changes.get(0);
        RemoveColumnChange colChange    = (RemoveColumnChange)changes.get(1);
        AddIndexChange     indexChange2 = (AddIndexChange)changes.get(2);

        assertEquals("TableA",
                     indexChange1.getChangedTable());
        assertEquals(model1.findTable("TableA").getIndex(0),
                     indexChange1.findChangedIndex(model1, false));

        assertEquals("TableA",
                     colChange.getChangedTable());
        assertEquals("COL2",
                     colChange.getChangedColumn());

        assertEquals("TableA",
                     indexChange2.getChangedTable());
        assertIndex("TESTINDEX", true, new String[] { "COL1", "COL3" },
                    indexChange2.getNewIndex());
    }
    
    /**
     * Tests the removal of an index.
     */
    public void testDropIndex1()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='INTEGER'/>\n" +
            "    <unique name='TestIndex'>\n" +
            "      <unique-column name='Col'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='INTEGER'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        RemoveIndexChange change = (RemoveIndexChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertEquals(model1.findTable("TableA").getIndex(0),
                     change.findChangedIndex(model1, true));
    }

    /**
     * Tests the removal of an index.
     */
    public void testDropIndex2()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL1' type='INTEGER'/>\n" +
            "    <column name='COL2' type='DOUBLE'/>\n" +
            "    <column name='COL3' type='VARCHAR' size='32'/>\n" +
            "    <index name='TESTINDEX'>\n" +
            "      <index-column name='COL3'/>\n" +
            "      <index-column name='COL2'/>\n" +
            "      <index-column name='COL1'/>\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL1' type='INTEGER'/>\n" +
            "    <column name='COL2' type='DOUBLE'/>\n" +
            "    <column name='COL3' type='VARCHAR' size='32'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        RemoveIndexChange indexChange = (RemoveIndexChange)changes.get(0);

        assertEquals("TableA",
                     indexChange.getChangedTable());
        assertEquals(model1.findTable("TableA").getIndex(0),
                     indexChange.findChangedIndex(model1, false));
    }
    
    /**
     * Tests the recreation of an index because of the change of type of the index.
     */
    public void testAddAndDropIndex()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='INTEGER'/>\n" +
            "    <unique name='TestIndex'>\n" +
            "      <unique-column name='Col'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='INTEGER'/>\n" +
            "    <index name='TestIndex'>\n" +
            "      <index-column name='Col'/>\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(2,
                     changes.size());

        RemoveIndexChange change1 = (RemoveIndexChange)changes.get(0);
        AddIndexChange    change2 = (AddIndexChange)changes.get(1);

        assertEquals("TableA",
                     change1.getChangedTable());
        assertEquals(model1.findTable("TableA").getIndex(0),
                     change1.findChangedIndex(model1, false));

        assertEquals("TableA",
                     change2.getChangedTable());
        assertIndex("TestIndex", false, new String[] { "Col" }, 
                     change2.getNewIndex());
    }
}
