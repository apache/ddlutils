package org.apache.ddlutils.task;

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

import junit.framework.Test;

import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;

/**
 * Tests the dropTables sub task.
 * 
 * @version $Revision: $
 */
public class TestDropTablesCommand extends TestTaskBase
{
    /**
     * Parameterized test case pattern.
     * 
     * @return The tests
     */
    public static Test suite() throws Exception
    {
        return getTests(TestDropTablesCommand.class);
    }

    /**
     * Tests the task against an empty database. 
     */
    public void testEmptyDatabase()
    {
        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.addDropTables(new DropTablesCommand());
        task.execute();

        assertEquals(new Database("roundtriptest"),
                     readModelFromDatabase("roundtriptest"),
                     false);
    }

    /**
     * Tests the removal of a single table. 
     */
    public void testSingleTable()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(modelXml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.addDropTables(new DropTablesCommand());
        task.execute();

        assertEquals(new Database("roundtriptest"),
                     readModelFromDatabase("roundtriptest"),
                     false);
    }

    /**
     * Tests the removal of a single table with an auto increment column. 
     */
    public void testSingleTableWithAutoIncrementColumn()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(modelXml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.addDropTables(new DropTablesCommand());
        task.execute();

        assertEquals(new Database("roundtriptest"),
                     readModelFromDatabase("roundtriptest"),
                     false);
    }

    /**
     * Tests the removal of a single table with an index. 
     */
    public void testSingleTableWithIndex()
    {
        if (!getPlatformInfo().isIndicesSupported())
        {
            return;
        }

        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <index name='test'>\n"+
            "      <index-column name='avalue'/>\n"+
            "    </index>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(modelXml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.addDropTables(new DropTablesCommand());
        task.execute();

        assertEquals(new Database("roundtriptest"),
                     readModelFromDatabase("roundtriptest"),
                     false);
    }

    /**
     * Tests the removal of a single table with a unique index. 
     */
    public void testSingleTableWithUniqeIndex()
    {
        if (!getPlatformInfo().isIndicesSupported())
        {
            return;
        }

        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <unique name='test'>\n"+
            "      <unique-column name='avalue'/>\n"+
            "    </unique>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(modelXml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.addDropTables(new DropTablesCommand());
        task.execute();

        assertEquals(new Database("roundtriptest"),
                     readModelFromDatabase("roundtriptest"),
                     false);
    }

    /**
     * Tests the removal of a table with a self-referencing foreign key. 
     */
    public void testSingleTablesWithSelfReferencingFK()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='roundtrip'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(modelXml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.addDropTables(new DropTablesCommand());
        task.execute();

        assertEquals(new Database("roundtriptest"),
                     readModelFromDatabase("roundtriptest"),
                     false);
    }

    /**
     * Tests the removal of two tables with a foreign key between them. 
     */
    public void testTwoTablesWithFK()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(modelXml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.addDropTables(new DropTablesCommand());
        task.execute();

        assertEquals(new Database("roundtriptest"),
                     readModelFromDatabase("roundtriptest"),
                     false);
    }

    /**
     * Tests the removal of two tables with circular foreign keys between them. 
     */
    public void testTwoTablesWithCircularFK()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='roundtrip2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(modelXml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.addDropTables(new DropTablesCommand());
        task.execute();

        assertEquals(new Database("roundtriptest"),
                     readModelFromDatabase("roundtriptest"),
                     false);
    }

    /**
     * Tests the removal of a table via the names list. 
     */
    public void testNamesListWithSingleName()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='roundtrip2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
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
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        DatabaseToDdlTask task    = getDatabaseToDdlTaskInstance();
        DropTablesCommand subTask = new DropTablesCommand();

        subTask.setTables("roundtrip1");
        task.addDropTables(subTask);
        task.execute();

        assertEquals(adjustModel(parseDatabaseFromString(model2Xml)),
                     readModelFromDatabase("roundtriptest"),
                     false);
    }

    /**
     * Tests the removal of multiple tables via the names list. 
     */
    public void testNamesListWithMultipleNames()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='roundtrip2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(modelXml);

        DatabaseToDdlTask task    = getDatabaseToDdlTaskInstance();
        DropTablesCommand subTask = new DropTablesCommand();

        subTask.setTables("roundtrip1,roundtrip2,roundtrip3");
        task.addDropTables(subTask);
        task.execute();

        assertEquals(new Database("roundtriptest"),
                     readModelFromDatabase("roundtriptest"),
                     false);
    }

    /**
     * Tests the removal of a table via the names list. 
     */
    public void testNamesListWithSingleDelimitedName()
    {
        if (!getPlatformInfo().isDelimitedIdentifiersSupported())
        {
            return;
        }

        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip 1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip 2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip 2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "  </table>\n"+
            "</database>";

        getPlatform().setDelimitedIdentifierModeOn(true);
        createDatabase(model1Xml);

        DatabaseToDdlTask task    = getDatabaseToDdlTaskInstance();
        DropTablesCommand subTask = new DropTablesCommand();

        subTask.setTables("Roundtrip 1");
        task.addDropTables(subTask);
        task.setUseDelimitedSqlIdentifiers(true);
        task.execute();

        assertEquals(adjustModel(parseDatabaseFromString(model2Xml)),
                     readModelFromDatabase("roundtriptest"),
                     true);
    }

    /**
     * Tests the removal of multiple tables via the names list. 
     */
    public void testNamesListWithMultipleDelimitedNames()
    {
        if (!getPlatformInfo().isDelimitedIdentifiersSupported())
        {
            return;
        }

        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip 1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip 2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        getPlatform().setDelimitedIdentifierModeOn(true);
        createDatabase(modelXml);

        DatabaseToDdlTask task    = getDatabaseToDdlTaskInstance();
        DropTablesCommand subTask = new DropTablesCommand();

        subTask.setTables("Roundtrip 2,Roundtrip 1");
        task.addDropTables(subTask);
        task.setUseDelimitedSqlIdentifiers(true);
        task.execute();

        assertEquals(new Database("roundtriptest"),
                     readModelFromDatabase("roundtriptest"),
                     true);
    }

    /**
     * Tests the removal of a table via the names list. 
     */
    public void testNamesListWithSingleDelimitedNameWithComma()
    {
        if (!getPlatformInfo().isDelimitedIdentifiersSupported())
        {
            return;
        }

        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip, 1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip, 2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip, 2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='Roundtrip, 1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip, 1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "</database>";

        getPlatform().setDelimitedIdentifierModeOn(true);
        createDatabase(model1Xml);

        DatabaseToDdlTask task    = getDatabaseToDdlTaskInstance();
        DropTablesCommand subTask = new DropTablesCommand();

        subTask.setTables("Roundtrip\\, 2");
        task.addDropTables(subTask);
        task.setUseDelimitedSqlIdentifiers(true);
        task.execute();

        assertEquals(adjustModel(parseDatabaseFromString(model2Xml)),
                     readModelFromDatabase("roundtriptest"),
                     true);
    }

    /**
     * Tests the removal of a table via the names list. 
     */
    public void testNamesListWithSingleDelimitedNameEndingInComma()
    {
        if (!getPlatformInfo().isDelimitedIdentifiersSupported())
        {
            return;
        }

        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip 1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 2,'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip 2,'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip 1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "</database>";

        getPlatform().setDelimitedIdentifierModeOn(true);
        createDatabase(model1Xml);

        DatabaseToDdlTask task    = getDatabaseToDdlTaskInstance();
        DropTablesCommand subTask = new DropTablesCommand();

        subTask.setTables("Roundtrip 2\\,");
        task.addDropTables(subTask);
        task.setUseDelimitedSqlIdentifiers(true);
        task.execute();

        assertEquals(adjustModel(parseDatabaseFromString(model2Xml)),
                     readModelFromDatabase("roundtriptest"),
                     true);
    }

    /**
     * Tests the removal of multiple tables via the names list. 
     */
    public void testNamesListWithMultipleDelimitedNameWithCommas()
    {
        if (!getPlatformInfo().isDelimitedIdentifiersSupported())
        {
            return;
        }

        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip, 1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 2,'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip 2,'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='Roundtrip, 1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        getPlatform().setDelimitedIdentifierModeOn(true);
        createDatabase(modelXml);

        DatabaseToDdlTask task    = getDatabaseToDdlTaskInstance();
        DropTablesCommand subTask = new DropTablesCommand();

        subTask.setTables("Roundtrip\\, 1,Roundtrip 2\\,");
        task.addDropTables(subTask);
        task.setUseDelimitedSqlIdentifiers(true);
        task.execute();

        assertEquals(new Database("roundtriptest"),
                     readModelFromDatabase("roundtriptest"),
                     false);
    }

    /**
     * Tests an empty names list. 
     */
    public void testEmptyNamesList()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='roundtrip2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(modelXml);

        DatabaseToDdlTask task    = getDatabaseToDdlTaskInstance();
        DropTablesCommand subTask = new DropTablesCommand();

        subTask.setTables("");
        task.addDropTables(subTask);
        task.execute();

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"),
                     false);
    }

    /**
     * Tests the removal of a table via a regular expression. 
     */
    public void testSimpleRegExp()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='roundtrip2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        DatabaseToDdlTask task    = getDatabaseToDdlTaskInstance();
        DropTablesCommand subTask = new DropTablesCommand();

        subTask.setTableFilter(".*2");
        task.addDropTables(subTask);
        task.execute();

        assertEquals(adjustModel(parseDatabaseFromString(model2Xml)),
                     readModelFromDatabase("roundtriptest"),
                     false);
    }

    /**
     * Tests the removal of a table via a regular expression. 
     */
    public void testRegExpInDelimitedIdentifierMode()
    {
        if (!getPlatformInfo().isDelimitedIdentifiersSupported())
        {
            return;
        }

        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip 1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip 2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip 1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "</database>";

        getPlatform().setDelimitedIdentifierModeOn(true);
        createDatabase(model1Xml);

        DatabaseToDdlTask task    = getDatabaseToDdlTaskInstance();
        DropTablesCommand subTask = new DropTablesCommand();

        subTask.setTableFilter(".*\\s[2|3]");
        task.addDropTables(subTask);
        task.setUseDelimitedSqlIdentifiers(true);
        task.execute();

        assertEquals(adjustModel(parseDatabaseFromString(model2Xml)),
                     readModelFromDatabase("roundtriptest"),
                     true);
    }

    /**
     * Tests the removal of multiple tables via a regular expression. 
     */
    public void testRegExpMultipleTables()
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='roundtrip2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='roundtrap3'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrap3'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "  </table>\n"+
            "</database>";

        getPlatform().setDelimitedIdentifierModeOn(true);
        createDatabase(model1Xml);

        DatabaseToDdlTask task    = getDatabaseToDdlTaskInstance();
        DropTablesCommand subTask = new DropTablesCommand();

        subTask.setTableFilter(".*trip.*");
        task.addDropTables(subTask);
        task.setUseDelimitedSqlIdentifiers(true);
        task.execute();

        assertEquals(adjustModel(parseDatabaseFromString(model2Xml)),
                     readModelFromDatabase("roundtriptest"),
                     true);
    }

    /**
     * Tests the removal of multiple tables via a regular expression. 
     */
    public void testRegExpMultipleTablesInDelimitedIdentifierMode()
    {
        if (!getPlatformInfo().isDelimitedIdentifiersSupported())
        {
            return;
        }

        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip 1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip 2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip A'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip A'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "  </table>\n"+
            "</database>";

        getPlatform().setDelimitedIdentifierModeOn(true);
        createDatabase(model1Xml);

        DatabaseToDdlTask task    = getDatabaseToDdlTaskInstance();
        DropTablesCommand subTask = new DropTablesCommand();

        subTask.setTableFilter(".*\\d");
        task.addDropTables(subTask);
        task.setUseDelimitedSqlIdentifiers(true);
        task.execute();

        assertEquals(adjustModel(parseDatabaseFromString(model2Xml)),
                     readModelFromDatabase("roundtriptest"),
                     true);
    }

    /**
     * Tests a regular expression that matches nothing. 
     */
    public void testRegExpMatchingNothing()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='roundtrip2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(modelXml);

        DatabaseToDdlTask task    = getDatabaseToDdlTaskInstance();
        DropTablesCommand subTask = new DropTablesCommand();

        subTask.setTableFilter(".*\\s\\D");
        task.addDropTables(subTask);
        task.execute();

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"),
                     false);
    }

    /**
     * Tests an empty regular expression. 
     */
    public void testEmptyRegExp()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='roundtrip2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(modelXml);

        DatabaseToDdlTask task    = getDatabaseToDdlTaskInstance();
        DropTablesCommand subTask = new DropTablesCommand();

        subTask.setTableFilter("");
        task.addDropTables(subTask);
        task.execute();

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"),
                     false);
    }
}
