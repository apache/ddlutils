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
 * Tests the model comparison of tables.
 * 
 * @version $Revision: $
 */
public class TestTableComparison extends TestComparisonBase
{
    /**
     * Tests the addition a column.
     */
    public void testAddColumn()
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
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col1' type='DOUBLE'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        AddColumnChange change = (AddColumnChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("Col1", Types.DOUBLE, null, null, false, false, false,
                     change.getNewColumn());
        assertEquals("ColPK",
                     change.getPreviousColumn());
        assertNull(change.getNextColumn());
    }

    /**
     * Tests the addition of an auto-increment column.
     */
    public void testAddAutoIncrementColumn()
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
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColA' type='INTEGER' autoIncrement='true'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        AddColumnChange change = (AddColumnChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("ColA", Types.INTEGER, null, null, false, false, true,
                     change.getNewColumn());
        assertEquals("ColPK",
                     change.getPreviousColumn());
        assertNull(change.getNextColumn());
    }

    /**
     * Tests the addition of a required column.
     */
    public void testAddRequiredColumn()
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
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='ColA' type='INTEGER' required='true'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        AddColumnChange change = (AddColumnChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("ColA", Types.INTEGER, null, null, false, true, false,
                     change.getNewColumn());
        assertEquals("ColPK",
                     change.getPreviousColumn());
        assertNull(change.getNextColumn());
    }

    /**
     * Tests the addition of a column that has a size spec and a default value.
     */
    public void testAddColumnWithSizeAndDefaultValue()
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
            "    <column name='COLA' type='VARCHAR' size='32' default='text'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        AddColumnChange change = (AddColumnChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("COLA", Types.VARCHAR, "32", "text", false, false, false,
                     change.getNewColumn());
        assertEquals("ColPK",
                     change.getPreviousColumn());
        assertNull(change.getNextColumn());
    }

    /**
     * Tests making a column required.
     */
    public void testMakeColumnRequired()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='INTEGER' required='false'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL' type='INTEGER' required='true'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        ColumnDefinitionChange change = (ColumnDefinitionChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("Col", Types.INTEGER, null, null, false, true, false,
                     change.getNewColumn());
    }

    /**
     * Tests making a column not required.
     */
    public void testMakeColumnNotRequired()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='INTEGER' required='true'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL' type='INTEGER' required='false'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        ColumnDefinitionChange change = (ColumnDefinitionChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("Col", Types.INTEGER, null, null, false, false, false,
                     change.getNewColumn());
    }
    
    /**
     * Tests making a column auto-increment.
     */
    public void testMakeColumnAutoIncrement()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='INTEGER' autoIncrement='false'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL' type='INTEGER' autoIncrement='true'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        ColumnDefinitionChange change = (ColumnDefinitionChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("Col", Types.INTEGER, null, null, false, false, true,
                     change.getNewColumn());
    }
    
    /**
     * Tests making a column not auto-increment.
     */
    public void testMakeColumnNotAutoIncrement()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='INTEGER' autoIncrement='true'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL' type='INTEGER' autoIncrement='false'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        ColumnDefinitionChange change = (ColumnDefinitionChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("Col", Types.INTEGER, null, null, false, false, false,
                     change.getNewColumn());
    }

    /**
     * Tests changing the data type of a column.
     */
    public void testChangeColumnDataType1()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='DOUBLE'/>\n" +
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

        ColumnDefinitionChange change = (ColumnDefinitionChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("Col", Types.INTEGER, null, null, false, false, false,
                     change.getNewColumn());
    }

    /**
     * Tests changing the data type of a column.
     */
    public void testChangeColumnDataType2()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='DOUBLE'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='VARCHAR' size='32'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        ColumnDefinitionChange change = (ColumnDefinitionChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("Col", Types.VARCHAR, "32", null, false, false, false,
                     change.getNewColumn());
    }

    /**
     * Tests changing the data type of a column.
     */
    public void testChangeColumnDataType3()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='DOUBLE'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='NUMERIC' size='10,5'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        ColumnDefinitionChange change = (ColumnDefinitionChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("Col", Types.NUMERIC, "10,5", null, false, false, false,
                     change.getNewColumn());
    }

    /**
     * Tests changing the data type of a column.
     */
    public void testChangeColumnDataType4()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='VARCHAR' size='32'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='CHAR' size='32'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        ColumnDefinitionChange change = (ColumnDefinitionChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("Col", Types.CHAR, "32", null, false, false, false,
                     change.getNewColumn());
    }

    /**
     * Tests changing the data type of a column.
     */
    public void testChangeColumnDataType5()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='VARCHAR' size='32'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='NUMERIC' size='32,5'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        ColumnDefinitionChange change = (ColumnDefinitionChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("Col", Types.NUMERIC, "32,5", null, false, false, false,
                     change.getNewColumn());
    }

    /**
     * Tests changing the data type of a column.
     */
    public void testChangeColumnDataType6()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='DECIMAL' size='10,5'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='VARCHAR' size='32'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        ColumnDefinitionChange change = (ColumnDefinitionChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("Col", Types.VARCHAR, "32", null, false, false, false,
                     change.getNewColumn());
    }

    /**
     * Tests changing the data type of a column.
     */
    public void testChangeColumnDataType7()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='VARCHAR' size='32'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='FLOAT'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        ColumnDefinitionChange change = (ColumnDefinitionChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("Col", Types.FLOAT, null, null, false, false, false,
                     change.getNewColumn());
    }

    /**
     * Tests changing the data type of a column.
     */
    public void testChangeColumnDataType8()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='DECIMAL' size='10,5'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='TIMESTAMP'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        ColumnDefinitionChange change = (ColumnDefinitionChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("Col", Types.TIMESTAMP, null, null, false, false, false,
                     change.getNewColumn());
    }
    
    /**
     * Tests changing the size of a column.
     */
    public void testChangeColumnSize()
    {
        // note that we also have a size for the INTEGER column, but we don't
        // expect a change for it because the size is not relevant for this type
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' size='8' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='VARCHAR' size='16'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL' type='VARCHAR' size='32'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        ColumnDefinitionChange change = (ColumnDefinitionChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("Col", Types.VARCHAR, "32", null, false, false, false,
                     change.getNewColumn());
    }
    
    /**
     * Tests changing the precision & scale of a column.
     */
    public void testChangeColumnPrecisionAndScale()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='DECIMAL' size='16,5'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL' type='DECIMAL' size='32,7'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        ColumnDefinitionChange change = (ColumnDefinitionChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("Col", Types.DECIMAL, "32,7", null, false, false, false,
                     change.getNewColumn());
    }
    
    /**
     * Tests changing the scale of a column.
     */
    public void testChangeColumnScale()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='NUMERIC' size='32,0'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='NUMERIC' size='32,5'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        ColumnDefinitionChange change = (ColumnDefinitionChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("Col", Types.NUMERIC, "32,5", null, false, false, false,
                     change.getNewColumn());
    }

    /**
     * Tests removing the size of a column. This test shows how the comparator
     * reacts in the common case of comparing a model read from a live database
     * (which usually returns sizes for every column) and a model from XML.
     * The model comparator will filter out these changes depending on the
     * platform info with which the comparator was created. 
     */
    public void testRemoveUnnecessaryColumnSize()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='INTEGER' size='8'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL' type='INTEGER'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertTrue(changes.isEmpty());
    }

    /**
     * Tests adding a default value to a column.
     */
    public void testAddDefaultValue()
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
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='INTEGER' default='0'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        ColumnDefinitionChange change = (ColumnDefinitionChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("Col", Types.INTEGER, null, "0", false, false, false,
                     change.getNewColumn());
    }

    /**
     * Tests changing the default value of a column.
     */
    public void testChangeDefaultValue()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='INTEGER' default='1'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='INTEGER' default='2'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        ColumnDefinitionChange change = (ColumnDefinitionChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("Col", Types.INTEGER, null, "2", false, false, false,
                     change.getNewColumn());
    }

    /**
     * Tests that shows that the same default value expressed differently does not
     * result in a change.
     */
    public void testSameDefaultValueExpressedDifferently()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='DOUBLE' default='10'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TABLEA'>\n" +
            "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='COL' type='DOUBLE' default='1e+1'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertTrue(changes.isEmpty());
    }

    /**
     * Tests removing the default value of a column.
     */
    public void testRemoveDefaultValue()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='VARCHAR' size='16' default='1'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='VARCHAR' size='16'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        ColumnDefinitionChange change = (ColumnDefinitionChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("Col", Types.VARCHAR, "16", null, false, false, false,
                     change.getNewColumn());
    }

    /**
     * Tests making a columnb required with a new default value.
     */
    public void testMakeColumnRequiredWithDefaultValue()
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
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='INTEGER' required='true' default='0'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        ColumnDefinitionChange change = (ColumnDefinitionChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("Col", Types.INTEGER, null, "0", false, true, false,
                     change.getNewColumn());
    }

    /**
     * Tests making a column not required and removing the default value.
     */
    public void testMakeColumnNotRequiredWithoutDefaultValue()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='VARCHAR' size='16' default='1' required='true'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col' type='VARCHAR' size='16'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        ColumnDefinitionChange change = (ColumnDefinitionChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertColumn("Col", Types.VARCHAR, "16", null, false, false, false,
                     change.getNewColumn());
    }

    /**
     * Tests changing the order of the columns in a table.
     */
    public void testChangeColumnOrder()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col1' type='DOUBLE'/>\n" +
            "    <column name='Col2' type='INTEGER' required='true'/>\n" +
            "    <column name='Col3' type='VARCHAR' size='32'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col3' type='VARCHAR' size='32'/>\n" +
            "    <column name='Col2' type='INTEGER' required='true'/>\n" +
            "    <column name='Col1' type='DOUBLE'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(1,
                     changes.size());

        ColumnOrderChange change = (ColumnOrderChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertEquals(3,
                     change.getNewPosition("Col1", true));
        assertEquals(-1,
                     change.getNewPosition("Col2", true));
        assertEquals(1,
                     change.getNewPosition("Col3", true));
    }

    /**
     * Tests adding a column and changing the order of the existing columns in a table.
     */
    public void testAddColumnAndChangeColumnOrder()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col1' type='DOUBLE'/>\n" +
            "    <column name='Col2' type='INTEGER' required='true'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col3' type='VARCHAR' size='32'/>\n" +
            "    <column name='Col2' type='INTEGER' required='true'/>\n" +
            "    <column name='Col1' type='DOUBLE'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(2,
                     changes.size());

        ColumnOrderChange change1 = (ColumnOrderChange)changes.get(0);
        AddColumnChange   change2 = (AddColumnChange)changes.get(1);

        assertEquals("TableA",
                     change1.getChangedTable());
        assertEquals(2,
                     change1.getNewPosition("Col1", false));
        assertEquals(1,
                     change1.getNewPosition("Col2", false));
        
        assertEquals("TableA",
                     change2.getChangedTable());
        assertColumn("Col3", Types.VARCHAR, "32", null, false, false, false,
                     change2.getNewColumn());
        assertEquals("ColPK",
                     change2.getPreviousColumn());
        assertEquals("Col2",
                     change2.getNextColumn());
    }

    /**
     * Tests removing a column and changing the order of the existing columns in a table.
     */
    public void testRemoveColumnAndChangeColumnOrder()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col3' type='VARCHAR' size='32'/>\n" +
            "    <column name='Col1' type='DOUBLE'/>\n" +
            "    <column name='Col2' type='INTEGER' required='true'/>\n" +
            "  </table>\n" +
            "</database>";
        final String MODEL2 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col2' type='INTEGER' required='true'/>\n" +
            "    <column name='Col1' type='DOUBLE'/>\n" +
            "  </table>\n" +
            "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List     changes = getPlatform(true).getChanges(model1, model2);

        assertEquals(2,
                     changes.size());

        RemoveColumnChange change1 = (RemoveColumnChange)changes.get(0);
        ColumnOrderChange  change2 = (ColumnOrderChange)changes.get(1);

        assertEquals("TableA",
                     change1.getChangedTable());
        assertEquals("Col3",
                     change1.getChangedColumn());

        assertEquals("TableA",
                     change2.getChangedTable());
        assertEquals(-1,
                     change2.getNewPosition("ColPK", true));
        assertEquals(2,
                     change2.getNewPosition("Col1", true));
        assertEquals(1,
                     change2.getNewPosition("Col2", true));
    }
    
    /**
     * Tests the removal of a column.
     */
    public void testDropColumn()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col1' type='DOUBLE'/>\n" +
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

        RemoveColumnChange change = (RemoveColumnChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertEquals("Col1",
                     change.getChangedColumn());
    }
    
    /**
     * Tests the removal of an auto-increment column.
     */
    public void testDropAutoIncrementColumn()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col1' type='INTEGER' autoIncrement='true'/>\n" +
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

        RemoveColumnChange change = (RemoveColumnChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertEquals("Col1",
                     change.getChangedColumn());
    }
    
    /**
     * Tests the removal of a required column.
     */
    public void testDropRequiredColumn()
    {
        final String MODEL1 = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableA'>\n" +
            "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
            "    <column name='Col1' type='INTEGER' required='true'/>\n" +
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

        RemoveColumnChange change = (RemoveColumnChange)changes.get(0);

        assertEquals("TableA",
                     change.getChangedTable());
        assertEquals("Col1",
                     change.getChangedColumn());
    }
}
