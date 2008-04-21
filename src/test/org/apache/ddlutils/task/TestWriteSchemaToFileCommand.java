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

import java.io.File;
import java.io.IOException;

import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.apache.tools.ant.BuildException;

/**
 * Tests the writeSchemaToFile sub task.
 * 
 * @version $Revision: $
 */
public class TestWriteSchemaToFileCommand extends TestTaskBase
{
    /**
     * Adds the writeSchemaToFile sub task to the given task, executes it, and checks its output.
     *
     * @param task                    The task
     * @param useDelimitedIdentifiers Whether to run with delimited identifiers
     * @param expectedModel           The expected model
     */
    private void runTask(DatabaseToDdlTask task, boolean useDelimitedIdentifiers, Database expectedModel) throws IOException
    {
        WriteSchemaToFileCommand subTask = new WriteSchemaToFileCommand();
        File                     tmpFile = File.createTempFile("schema", ".xml");

        try
        {
            subTask.setOutputFile(tmpFile);
            task.setUseDelimitedSqlIdentifiers(useDelimitedIdentifiers);
            task.addWriteSchemaToFile(subTask);
            task.setModelName("roundtriptest");
            task.execute();

            assertEquals(expectedModel,
                         new DatabaseIO().read(tmpFile),
                         useDelimitedIdentifiers);
        }
        finally
        {
            if (!tmpFile.delete())
            {
                getLog().warn("Could not delete temporary file " + tmpFile.getAbsolutePath());
            }
        }
    }

    /**
     * Tests the task against an empty database. 
     */
    public void testEmptyDatabase() throws IOException
    {
        runTask(getDatabaseToDdlTaskInstance(), false, new Database("roundtriptest"));
    }

    /**
     * Tests against a model with two tables and a FK. 
     */
    public void testSimpleModel() throws IOException
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
        runTask(getDatabaseToDdlTaskInstance(), false, readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests against a model with two tables and a FK. 
     */
    public void testSimpleModelWithDelimitedIdentifiers() throws IOException
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip 1'>\n"+
            "    <column name='A PK' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='A Value' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 2'>\n"+
            "      <reference local='A Value' foreign='A PK'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip 2'>\n"+
            "    <column name='A PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='A Value' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 1'>\n"+
            "      <reference local='A Value' foreign='A PK'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        getPlatform().setDelimitedIdentifierModeOn(true);
        createDatabase(modelXml);
        runTask(getDatabaseToDdlTaskInstance(), true, readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests of the includeTables filter. 
     */
    public void testIncludeSingleTable() throws IOException
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='roundtrip2'>\n"+
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

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.setIncludeTables("roundtrip1");
        runTask(task, false, parseDatabaseFromString(model2Xml));
    }

    /**
     * Tests of the includeTables filter in the presence of a foreign key to the indicated table.
     */
    public void testIncludeSingleTableWithFk() throws IOException
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

        task.setIncludeTables("roundtrip1");
        try
        {
            runTask(task, false, readModelFromDatabase("roundtriptest"));
            fail();
        }
        catch (BuildException ex)
        {
            // expected
        }
    }

    /**
     * Tests of the includeTableFilter filter. 
     */
    public void testIncludeSingleTableViaRegExp() throws IOException
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip 1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip 2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 2'>\n"+
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

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.setIncludeTableFilter("Round.*\\s1");
        runTask(task, true, parseDatabaseFromString(model2Xml));
    }

    /**
     * Tests of the includeTableFilter filter in the presence of a foreign key to the indicated table.
     */
    public void testIncludeSingleTableWithFkViaRegExp() throws IOException
    {
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

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.setIncludeTableFilter("Round.*\\s1");
        try
        {
            runTask(task, true, readModelFromDatabase("roundtriptest"));
            fail();
        }
        catch (BuildException ex)
        {
            // expected
        }
    }

    /**
     * Tests of the includeTables filter for multiple tables. 
     */
    public void testIncludeMultipleTables() throws IOException
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='roundtrip3'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip3'>\n"+
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
            "    <foreign-key foreignTable='roundtrip3'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='roundtrip3'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.setIncludeTables("roundtrip1,roundtrip3");
        runTask(task, false, parseDatabaseFromString(model2Xml));
    }

    /**
     * Tests of the includeTables filter for multiple tables. 
     */
    public void testIncludeMultipleTablesWithFKPointingToThem() throws IOException
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
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='roundtrip2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='roundtrip3'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(modelXml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.setIncludeTables("roundtrip1,roundtrip3");
        try
        {
            runTask(task, false, readModelFromDatabase("roundtriptest"));
            fail();
        }
        catch (BuildException ex)
        {
            // expected
        }
    }

    /**
     * Tests of the includeTableFilter filter for multiple tables. 
     */
    public void testIncludeMultipleTablesViaRegExp() throws IOException
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip 1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 3'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip 2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip 3'>\n"+
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
            "    <foreign-key foreignTable='Roundtrip 3'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip 3'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        getPlatform().setDelimitedIdentifierModeOn(true);
        createDatabase(model1Xml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.setIncludeTableFilter(".*trip [1|3]");
        runTask(task, true, parseDatabaseFromString(model2Xml));
    }

    /**
     * Tests of the includeTables filter for multiple tables via reg exp. 
     */
    public void testIncludeMultipleTablesWithFKPointingToThemViaRegExp() throws IOException
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip 1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 3'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip 2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 3'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip 3'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        getPlatform().setDelimitedIdentifierModeOn(true);
        createDatabase(modelXml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.setIncludeTableFilter(".*trip\\s*[1|3]");
        try
        {
            runTask(task, true, readModelFromDatabase("roundtriptest"));
            fail();
        }
        catch (BuildException ex)
        {
            // expected
        }
    }

    /**
     * Tests of the excludeTables filter. 
     */
    public void testExcludeSingleTable() throws IOException
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='roundtrip2'>\n"+
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

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.setExcludeTables("roundtrip2");
        runTask(task, false, parseDatabaseFromString(model2Xml));
    }

    /**
     * Tests of the excludeTables filter in the presence of a foreign key to the indicated table.
     */
    public void testExcludeSingleTableWithFk() throws IOException
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

        task.setExcludeTables("roundtrip1");
        try
        {
            runTask(task, false, readModelFromDatabase("roundtriptest"));
            fail();
        }
        catch (BuildException ex)
        {
            // expected
        }
    }

    /**
     * Tests of the excludeTableFilter filter. 
     */
    public void testExcludeSingleTableViaRegExp() throws IOException
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip 1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip 2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 2'>\n"+
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

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.setExcludeTableFilter("Round.*\\s2");
        runTask(task, true, parseDatabaseFromString(model2Xml));
    }

    /**
     * Tests of the excludeTableFilter filter in the presence of a foreign key to the indicated table.
     */
    public void testExcludeSingleTableWithFkViaRegExp() throws IOException
    {
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

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.setExcludeTableFilter("Round.*\\s2");
        try
        {
            runTask(task, true, readModelFromDatabase("roundtriptest"));
            fail();
        }
        catch (BuildException ex)
        {
            // expected
        }
    }

    /**
     * Tests of the excludeTables filter for multiple tables. 
     */
    public void testExcludeMultipleTables() throws IOException
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='roundtrip3'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip3'>\n"+
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

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.setExcludeTables("roundtrip1,roundtrip3");
        runTask(task, false, parseDatabaseFromString(model2Xml));
    }

    /**
     * Tests of the excludeTables filter for multiple tables. 
     */
    public void testExcludeMultipleTablesWithFKPointingToThem() throws IOException
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
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='roundtrip2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='roundtrip3'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(modelXml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.setExcludeTables("roundtrip1,roundtrip3");
        try
        {
            runTask(task, false, readModelFromDatabase("roundtriptest"));
            fail();
        }
        catch (BuildException ex)
        {
            // expected
        }
    }

    /**
     * Tests of the excludeTableFilter filter for multiple tables. 
     */
    public void testExcludeMultipleTablesViaRegExp() throws IOException
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip 1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 3'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip 2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip 3'>\n"+
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
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        getPlatform().setDelimitedIdentifierModeOn(true);
        createDatabase(model1Xml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.setExcludeTableFilter(".*trip [1|3]");
        runTask(task, true, parseDatabaseFromString(model2Xml));
    }

    /**
     * Tests of the excludeTables filter for multiple tables via reg exp. 
     */
    public void testExcludeMultipleTablesWithFKPointingToThemViaRegExp() throws IOException
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip 1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 3'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip 2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 3'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip 3'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        getPlatform().setDelimitedIdentifierModeOn(true);
        createDatabase(modelXml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.setExcludeTableFilter(".*trip\\s*[1|3]");
        try
        {
            runTask(task, true, readModelFromDatabase("roundtriptest"));
            fail();
        }
        catch (BuildException ex)
        {
            // expected
        }
    }

    /**
     * Tests of the includeTables and excludeTables filters for multiple tables. 
     */
    public void testIncludeAndExcludeMultipleTables() throws IOException
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip 1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 3'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip 2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip 3'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='Roundtrip 1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip 4'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip 4'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "</database>";

        getPlatform().setDelimitedIdentifierModeOn(true);
        createDatabase(model1Xml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.setIncludeTables("Roundtrip 1,Roundtrip 3,Roundtrip 4");
        task.setExcludeTables("Roundtrip 1,Roundtrip 3");
        runTask(task, true, parseDatabaseFromString(model2Xml));
    }

    /**
     * Tests of the includeTableFilter and excludeTableFilter filters for multiple tables. 
     */
    public void testIncludeAndExcludeMultipleTablesViaRegExp() throws IOException
    {
        final String model1Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='roundtrip3'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip3'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='roundtrip1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='roundtrip4'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
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

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        task.setIncludeTableFilter(".*[1|2|3]");
        task.setExcludeTableFilter(".*[1|3]");
        runTask(task, false, parseDatabaseFromString(model2Xml));
    }
}
