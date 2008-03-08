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

import org.apache.ddlutils.model.Database;

/**
 * Tests the dropTables sub task.
 * 
 * @version $Revision: $
 */
public class TestDropTablesCommand extends TestTaskBase
{
    /**
     * Tests the task against an empty database. 
     */
    public void testEmptyDatabase()
    {
        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.addDropTables(new DropTablesCommand());
        task.execute();

        assertEquals(new Database("roundtriptest"),
                     readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests the removal of a single table. 
     */
    public void testSingleTable()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
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
                     readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests the removal of a single table with an auto increment column. 
     */
    public void testSingleTableWithAutoIncrementColumn()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
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
                     readModelFromDatabase("roundtriptest"));
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
            "<database name='roundtriptest'>\n"+
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
                     readModelFromDatabase("roundtriptest"));
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
            "<database name='roundtriptest'>\n"+
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
                     readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests the removal of a table with a self-referencing foreign key. 
     */
    public void testSingleTablesWithSelfReferencingFK()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
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
                     readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests the removal of two tables with a foreign key between them. 
     */
    public void testTwoTablesWithFK()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
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
                     readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests the removal of two tables with circular foreign keys between them. 
     */
    public void testTwoTablesWithCircularFK()
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database name='roundtriptest'>\n"+
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
                     readModelFromDatabase("roundtriptest"));
    }

    // circular fks
}
