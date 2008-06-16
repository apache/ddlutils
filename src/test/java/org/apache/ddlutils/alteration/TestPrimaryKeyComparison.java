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
 * Tests the model comparison of primary keys.
 * 
 * @version $Revision: $
 */
public class TestPrimaryKeyComparison extends TestComparisonBase
{
    /**
     * Tests the addition of a column that is the primary key.
     */
    public void testAddPrimaryKeyColumn()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK2' type='INTEGER' required='true'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK2' type='INTEGER' required='true'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(2,
                     changes.size());

        AddColumnChange     colChange = (AddColumnChange)changes.get(0);
        AddPrimaryKeyChange pkChange  = (AddPrimaryKeyChange)changes.get(1);

        assertEquals("TableA",
                     colChange.getChangedTable());
        assertColumn("ColPK1", Types.INTEGER, null, null, false, true, false,
                     colChange.getNewColumn());
        assertNull(colChange.getPreviousColumn());
        assertEquals("ColPK2",
                     colChange.getNextColumn());

        assertEquals("TableA",
                     pkChange.getChangedTable());
        assertEquals(1,
                     pkChange.getPrimaryKeyColumns().length);
        assertEquals("ColPK1",
                     pkChange.getPrimaryKeyColumns()[0]);
    }

    /**
     * Tests the addition of a single-column primary key.
     */
    public void testMakeColumnPrimaryKey()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' required='true'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        AddPrimaryKeyChange change = (AddPrimaryKeyChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertEquals(1,
                     change.getPrimaryKeyColumns().length);
        assertEquals("ColPK",
                     change.getPrimaryKeyColumns()[0]);
    }

    /**
     * Tests the addition of a column to the primary key.
     */
    public void testAddColumnToPrimaryKey()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK2' type='INTEGER' required='true'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        PrimaryKeyChange change = (PrimaryKeyChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertEquals(2,
                     change.getNewPrimaryKeyColumns().length);
        assertEquals("ColPK1",
                     change.getNewPrimaryKeyColumns()[0]);
        assertEquals("ColPK2",
                     change.getNewPrimaryKeyColumns()[1]);
    }

    /**
     * Tests changing the order of columns in the primary key.
     */
    public void testChangeColumnOrderInPrimaryKey()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK3' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK3' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(2,
                     changes.size());

        PrimaryKeyChange  pkChange  = (PrimaryKeyChange)changes.get(0);
        ColumnOrderChange colChange = (ColumnOrderChange)changes.get(1);

        assertEquals("TableA",
                     pkChange.getChangedTable());
        assertEquals(3,
                     pkChange.getNewPrimaryKeyColumns().length);
        assertEquals("ColPK2",
                     pkChange.getNewPrimaryKeyColumns()[0]);
        assertEquals("ColPK3",
                     pkChange.getNewPrimaryKeyColumns()[1]);
        assertEquals("ColPK1",
                     pkChange.getNewPrimaryKeyColumns()[2]);
        
        assertEquals("TableA",
                     colChange.getChangedTable());
        assertEquals(2,
                     colChange.getNewPosition("ColPK1", true));
        assertEquals(0,
                     colChange.getNewPosition("ColPK2", true));
        assertEquals(1,
                     colChange.getNewPosition("ColPK3", true));
    }

    /**
     * Tests adding a column to and changing the order of columns in the primary key.
     */
    public void testAddColumnAndChangeColumnOrderInPrimaryKey()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK3' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK3' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(4,
                     changes.size());

        PrimaryKeyChange  pkChange1  = (PrimaryKeyChange)changes.get(0);
        ColumnOrderChange colChange1 = (ColumnOrderChange)changes.get(1);
        AddColumnChange   colChange2 = (AddColumnChange)changes.get(2);
        PrimaryKeyChange  pkChange2  = (PrimaryKeyChange)changes.get(3);
        
        assertEquals("TableA",
                     pkChange1.getChangedTable());
        assertEquals(2,
                     pkChange1.getNewPrimaryKeyColumns().length);
        assertEquals("ColPK3",
                     pkChange1.getNewPrimaryKeyColumns()[0]);
        assertEquals("ColPK1",
                     pkChange1.getNewPrimaryKeyColumns()[1]);

        assertEquals("TableA",
                     colChange1.getChangedTable());
        assertEquals(1,
                     colChange1.getNewPosition("ColPK1", true));
        assertEquals(-1,
                     colChange1.getNewPosition("ColPK2", true));
        assertEquals(0,
                     colChange1.getNewPosition("ColPK3", true));

        assertEquals("TableA",
                     colChange2.getChangedTable());
        assertColumn("ColPK2", Types.INTEGER, null, null, false, true, false,
                     colChange2.getNewColumn());
        assertNull(colChange2.getPreviousColumn());
        assertEquals("ColPK3",
                     colChange2.getNextColumn());

        assertEquals("TableA",
                     pkChange2.getChangedTable());
        assertEquals(3,
                     pkChange2.getNewPrimaryKeyColumns().length);
        assertEquals("ColPK2",
                     pkChange2.getNewPrimaryKeyColumns()[0]);
        assertEquals("ColPK3",
                     pkChange2.getNewPrimaryKeyColumns()[1]);
        assertEquals("ColPK1",
                     pkChange2.getNewPrimaryKeyColumns()[2]);
    }

    /**
     * Tests removing a column from and changing the order of columns in the primary key.
     */
    public void testRemoveColumnAndChangeColumnOrderInPrimaryKey()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK3' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK3' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(3,
                     changes.size());

        RemoveColumnChange colChange1 = (RemoveColumnChange)changes.get(0);
        PrimaryKeyChange   pkChange   = (PrimaryKeyChange)changes.get(1);
        ColumnOrderChange  colChange2 = (ColumnOrderChange)changes.get(2);

        assertEquals("TableA",
                     colChange1.getChangedTable());
        assertEquals("ColPK2",
                     colChange1.getChangedColumn());

        assertEquals("TableA",
                     pkChange.getChangedTable());
        assertEquals(2,
                     pkChange.getNewPrimaryKeyColumns().length);
        assertEquals("ColPK3",
                     pkChange.getNewPrimaryKeyColumns()[0]);
        assertEquals("ColPK1",
                     pkChange.getNewPrimaryKeyColumns()[1]);
        
        assertEquals("TableA",
                     colChange2.getChangedTable());
        assertEquals(1,
                     colChange2.getNewPosition("ColPK1", true));
        assertEquals(-1,
                     colChange2.getNewPosition("ColPK2", true));
        assertEquals(0,
                     colChange2.getNewPosition("ColPK3", true));
    }

    // TODO: remove, add & reorder PK columns
    /**
     * Tests the removal of a column from the primary key.
     */
    public void testMakeColumnNotPrimaryKey()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK1' type='INTEGER' required='true'/>\n" +
            "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        PrimaryKeyChange change = (PrimaryKeyChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertEquals(1,
                     change.getNewPrimaryKeyColumns().length);
        assertEquals("ColPK2",
                     change.getNewPrimaryKeyColumns()[0]);
    }


    /**
     * Tests removing the column that is the primary key.
     */
    public void testDropPrimaryKeyColumn1()
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
            "    <column name='COL' type='INTEGER'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        RemoveColumnChange colChange = (RemoveColumnChange)changes.get(0);

        assertEquals("TableA",
                     colChange.getChangedTable());
        assertEquals("ColPK",
                     colChange.getChangedColumn());
    }

    /**
     * Tests dropping a column that is part of the primary key.
     */
    public void testDropPrimaryKeyColumn2()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK3' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK3' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        RemoveColumnChange colChange = (RemoveColumnChange)changes.get(0);

        assertEquals("TableA",
                     colChange.getChangedTable());
        assertEquals("ColPK2",
                     colChange.getChangedColumn());
    }

    /**
     * Tests the removal of a primary key.
     */
    public void testRemovePrimaryKey1()
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
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' required='true'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        RemovePrimaryKeyChange change = (RemovePrimaryKeyChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
    }

    /**
     * Tests removing a multi-column primary key.
     */
    public void testRemovePrimaryKey2()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK3' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK1' type='INTEGER' required='true'/>\n" +
            "    <column name='COLPK2' type='INTEGER' required='true'/>\n" +
            "    <column name='COLPK3' type='INTEGER' required='true'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        RemovePrimaryKeyChange pkChange = (RemovePrimaryKeyChange)changes.get(0);

        assertEquals("TableA",
                     pkChange.getChangedTable());
    }

    /**
     * Tests changing the columns of a primary key.
     */
    public void testChangePrimaryKeyColumns()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK2' type='INTEGER' required='true'/>\n" +
            "    <column name='ColPK3' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK4' type='INTEGER' required='true'/>\n" +
            "    <column name='ColPK5' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='ColPK1' type='INTEGER' required='true'/>\n" +
            "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK3' type='INTEGER' required='true'/>\n" +
            "    <column name='ColPK4' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColPK5' type='INTEGER' required='true'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        PrimaryKeyChange pkChange = (PrimaryKeyChange)changes.get(0);

        assertEquals("TableA",
                     pkChange.getChangedTable());
        assertEquals(2,
                     pkChange.getNewPrimaryKeyColumns().length);
        assertEquals("ColPK2",
                     pkChange.getNewPrimaryKeyColumns()[0]);
        assertEquals("ColPK4",
                     pkChange.getNewPrimaryKeyColumns()[1]);
    }
}
