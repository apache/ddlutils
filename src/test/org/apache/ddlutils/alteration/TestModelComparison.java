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
 * Tests the comparison on the model level.
 * 
 * @version $Revision: $
 */
public class TestModelComparison extends TestComparisonBase
{
    /**
     * Tests the addition of a table.
     */
    public void testAddTable()
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
            "  </table>\n" +
            "  <table name='TABLEB'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        AddTableChange change = (AddTableChange)changes.get(0);

        assertTable("TABLEB", null, 1, 0, 0,
                    change.getNewTable());
        assertColumn("COLPK", Types.INTEGER, null, null, true, true, false,
                     change.getNewTable().getColumn(0));
    }

    /**
     * Tests the addition of a table with an index.
     */
    public void testAddTableWithIndex()
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
            "  </table>\n" +
            "  <table name='TABLEB'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COLA' type='DOUBLE'/>\n" +
            "    <index name='TestIndex'>\n" +
            "      <index-column name='COLA'/>\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        AddTableChange change = (AddTableChange)changes.get(0);

        assertTable("TABLEB", null, 2, 0, 1, 
                     change.getNewTable());
        assertColumn("COLPK", Types.INTEGER, null, null, true, true, false,
                     change.getNewTable().getColumn(0));
        assertColumn("COLA", Types.DOUBLE, null, null, false, false, false,
                     change.getNewTable().getColumn(1));
        assertIndex("TestIndex", false, new String[] { "COLA" },
                     change.getNewTable().getIndex(0));
    }

    /**
     * Tests the addition of a table and a foreign key to it.
     */
    public void testAddTableAndForeignKeyToIt()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COLFK' type='INTEGER'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COLFK' type='INTEGER'/>\n" +
            "    <foreign-key name='TESTFKB' foreignTable='TABLEB'>\n" +
            "      <reference local='COLFK' foreign='COLPK'/>\n" +
            "    </foreign-key>\n" +
            "  </table>\n" +
            "  <table name='TABLEB'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(2,
                     changes.size());

        AddTableChange      tableChange = (AddTableChange)changes.get(0);
        AddForeignKeyChange fkChange    = (AddForeignKeyChange)changes.get(1);

        assertTable("TABLEB", null, 1, 0, 0, 
                    tableChange.getNewTable());
        assertColumn("COLPK", Types.INTEGER, null, null, true, true, false,
                     tableChange.getNewTable().getColumn(0));

        assertEquals("TABLEA",
                     fkChange.getChangedTable());
        assertForeignKey("TESTFKB", "TABLEB", new String[] { "COLFK" }, new String[] { "COLPK" }, 
                         fkChange.getNewForeignKey());
    }

    /**
     * Tests the addition of two tables with foreign keys to each other .
     */
    public void testAddTablesWithForeignKeys()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COLFK' type='INTEGER'/>\n" +
            "    <foreign-key name='TESTFKB' foreignTable='TABLEB'>\n" +
            "      <reference local='COLFK' foreign='COLPK'/>\n" +
            "    </foreign-key>\n" +
            "  </table>\n" +
            "  <table name='TABLEB'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COLFK' type='INTEGER'/>\n" +
            "    <foreign-key name='TESTFKA' foreignTable='TABLEA'>\n" +
            "      <reference local='COLFK' foreign='COLPK'/>\n" +
            "    </foreign-key>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(4,
                     changes.size());

        AddTableChange      tableChange1 = (AddTableChange)changes.get(0);
        AddTableChange      tableChange2 = (AddTableChange)changes.get(1);
        AddForeignKeyChange fkChange1    = (AddForeignKeyChange)changes.get(2);
        AddForeignKeyChange fkChange2    = (AddForeignKeyChange)changes.get(3);

        assertTable("TABLEA", null, 2, 0, 0, 
                    tableChange1.getNewTable());
        assertColumn("COLPK", Types.INTEGER, null, null, true, true, false,
                     tableChange1.getNewTable().getColumn(0));
        assertColumn("COLFK", Types.INTEGER, null, null, false, false, false,
                     tableChange1.getNewTable().getColumn(1));

        assertTable("TABLEB", null, 2, 0, 0, 
                    tableChange2.getNewTable());
        assertColumn("COLPK", Types.INTEGER, null, null, true, true, false,
                     tableChange2.getNewTable().getColumn(0));
        assertColumn("COLFK", Types.INTEGER, null, null, false, false, false,
                     tableChange2.getNewTable().getColumn(1));

        assertEquals("TABLEA",
                     fkChange1.getChangedTable());
        assertForeignKey("TESTFKB", "TABLEB", new String[] { "COLFK" }, new String[] { "COLPK" }, 
                         fkChange1.getNewForeignKey());

        assertEquals("TABLEB",
                     fkChange2.getChangedTable());
        assertForeignKey("TESTFKA", "TABLEA", new String[] { "COLFK" }, new String[] { "COLPK" }, 
                         fkChange2.getNewForeignKey());
    }

    /**
     * Tests the removal of a table.
     */
    public void testDropTable()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "  <table name='TableB'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEB'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        RemoveTableChange change = (RemoveTableChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
    }

    /**
     * Tests the removal of a table with an index.
     */
    public void testDropTableWithIndex()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColA' type='INTEGER'/>\n" +
            "    <index name='TestIndex'>\n" +
            "      <index-column name='ColA'/>\n" +
            "    </index>\n" +
            "  </table>\n" +
            "  <table name='TableB'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEB'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        RemoveTableChange change = (RemoveTableChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
    }

    /**
     * Tests the removal of a table with a foreign key.
     */
    public void testDropTableWithForeignKey()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COLFK' type='INTEGER'/>\n" +
            "    <foreign-key name='TESTFKB' foreignTable='TableB'>\n" +
            "      <reference local='COLFK' foreign='COLPK'/>\n" +
            "    </foreign-key>\n" +
            "  </table>\n" +
            "  <table name='TableB'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEB'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(2,
                     changes.size());

        RemoveForeignKeyChange fkChange    = (RemoveForeignKeyChange)changes.get(0);
        RemoveTableChange      tableChange = (RemoveTableChange)changes.get(1);

        assertEquals("TableA",
                     fkChange.getChangedTable());
        assertEquals(model1.findTable("TableA").getForeignKey(0),
                     fkChange.findChangedForeignKey(model1, false));

        assertEquals("TableA",
                     tableChange.getChangedTable());
    }

    /**
     * Tests the removal of a table and a foreign key to it.
     */
    public void testDropTableAndForeignKeyToIt()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "  <table name='TableB'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COLFK' type='INTEGER'/>\n" +
            "    <foreign-key name='TESTFKA' foreignTable='TableA'>\n" +
            "      <reference local='COLFK' foreign='ColPK'/>\n" +
            "    </foreign-key>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEB'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COLFK' type='INTEGER'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(2,
                     changes.size());

        RemoveForeignKeyChange fkChange    = (RemoveForeignKeyChange)changes.get(0);
        RemoveTableChange      tableChange = (RemoveTableChange)changes.get(1);

        assertEquals("TableB",
                     fkChange.getChangedTable());
        assertEquals(model1.findTable("TableB").getForeignKey(0),
                     fkChange.findChangedForeignKey(model1, false));

        assertEquals("TableA",
                     tableChange.getChangedTable());
    }

    /**
     * Tests the addition and removal of a table.
     */
    public void testAddAndDropTable()
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
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(2,
                     changes.size());

        RemoveTableChange change1 = (RemoveTableChange)changes.get(0);
        AddTableChange    change2 = (AddTableChange)changes.get(1);

        assertEquals("TableA",
                     change1.getChangedTable());

        assertTable("TABLEA", null, 1, 0, 0, 
                    change2.getNewTable());
        assertColumn("COLPK", Types.INTEGER, null, null, true, true, false,
                     change2.getNewTable().getColumn(0));
    }
}
