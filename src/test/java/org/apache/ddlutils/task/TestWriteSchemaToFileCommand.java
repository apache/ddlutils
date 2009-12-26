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

import junit.framework.Test;

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
     * Parameterized test case pattern.
     * 
     * @return The tests
     */
    public static Test suite() throws Exception
    {
        return getTests(TestWriteSchemaToFileCommand.class);
    }

    /**
     * Adds the writeSchemaToFile sub task to the given task, executes it, and checks its output.
     *
     * @param task          The task
     * @param expectedModel The expected model
     */
    private void runTask(DatabaseToDdlTask task, Database expectedModel) throws IOException
    {
        WriteSchemaToFileCommand subTask = new WriteSchemaToFileCommand();
        File                     tmpFile = File.createTempFile("schema", ".xml");

        try
        {
            subTask.setOutputFile(tmpFile);
            task.addWriteSchemaToFile(subTask);
            task.setModelName("roundtriptest");
            task.execute();

            assertEquals(expectedModel,
                         new DatabaseIO().read(tmpFile),
                         isUseDelimitedIdentifiers());
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
        runTask(getDatabaseToDdlTaskInstance(), new Database("roundtriptest"));
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
        runTask(getDatabaseToDdlTaskInstance(), readModelFromDatabase("roundtriptest"));
    }

    /**
     * Tests against a model with two tables and a FK. 
     */
    public void testSimpleModelWithDelimitedIdentifiers() throws IOException
    {
        if (!isUseDelimitedIdentifiers())
        {
            return;
        }

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

        createDatabase(modelXml);
        runTask(getDatabaseToDdlTaskInstance(), readModelFromDatabase("roundtriptest"));
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

        if (isUseDelimitedIdentifiers())
        {
            task.setIncludeTables("roundtrip1");
        }
        else
        {
            task.setIncludeTables("ROUNDTRIP1");
        }
        runTask(task, parseDatabaseFromString(model2Xml));
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

        if (isUseDelimitedIdentifiers())
        {
            task.setIncludeTables("roundtrip1");
        }
        else
        {
            task.setIncludeTables("ROUNDTRIP1");
        }
        try
        {
            runTask(task, readModelFromDatabase("roundtriptest"));
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
            "  <table name='Roundtrip_1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip_2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip_1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        if (isUseDelimitedIdentifiers())
        {
            task.setIncludeTableFilter("Round.*1");
        }
        else
        {
            task.setIncludeTableFilter("ROUND.*1");
        }
        runTask(task, parseDatabaseFromString(model2Xml));
    }

    /**
     * Tests of the includeTableFilter filter in the presence of a foreign key to the indicated table.
     */
    public void testIncludeSingleTableWithFkViaRegExp() throws IOException
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip_1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip_2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(modelXml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        if (isUseDelimitedIdentifiers())
        {
            task.setIncludeTableFilter("Round.*1");
        }
        else
        {
            task.setIncludeTableFilter("ROUND.*1");
        }
        try
        {
            runTask(task, readModelFromDatabase("roundtriptest"));
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

        if (isUseDelimitedIdentifiers())
        {
            task.setIncludeTables("roundtrip1,roundtrip3");
        }
        else
        {
            task.setIncludeTables("ROUNDTRIP1,ROUNDTRIP3");
        }
        runTask(task, parseDatabaseFromString(model2Xml));
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

        if (isUseDelimitedIdentifiers())
        {
            task.setIncludeTables("roundtrip1,roundtrip3");
        }
        else
        {
            task.setIncludeTables("ROUNDTRIP1,ROUNDTRIP3");
        }
        try
        {
            runTask(task, readModelFromDatabase("roundtriptest"));
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
            "  <table name='Roundtrip_1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_3'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip_2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip_3'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip_1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_3'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip_3'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        if (isUseDelimitedIdentifiers())
        {
            task.setIncludeTableFilter(".*trip_[1|3]");
        }
        else
        {
            task.setIncludeTableFilter(".*TRIP_[1|3]");
        }
        runTask(task, parseDatabaseFromString(model2Xml));
    }

    /**
     * Tests of the includeTables filter for multiple tables via reg exp. 
     */
    public void testIncludeMultipleTablesWithFKPointingToThemViaRegExp() throws IOException
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip_1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_3'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip_2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_3'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip_3'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(modelXml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        if (isUseDelimitedIdentifiers())
        {
            task.setIncludeTableFilter(".*trip_*[1|3]");
        }
        else
        {
            task.setIncludeTableFilter(".*TRIP_*[1|3]");
        }
        try
        {
            runTask(task, readModelFromDatabase("roundtriptest"));
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

        if (isUseDelimitedIdentifiers())
        {
            task.setExcludeTables("roundtrip2");
        }
        else
        {
            task.setExcludeTables("ROUNDTRIP2");
        }
        runTask(task, parseDatabaseFromString(model2Xml));
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

        if (isUseDelimitedIdentifiers())
        {
            task.setExcludeTables("roundtrip1");
        }
        else
        {
            task.setExcludeTables("ROUNDTRIP1");
        }
        try
        {
            runTask(task, readModelFromDatabase("roundtriptest"));
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
            "  <table name='Roundtrip_1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip_2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip_1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        if (isUseDelimitedIdentifiers())
        {
            task.setExcludeTableFilter("Round.*_2");
        }
        else
        {
            task.setExcludeTableFilter("ROUND.*_2");
        }
        runTask(task, parseDatabaseFromString(model2Xml));
    }

    /**
     * Tests of the excludeTableFilter filter in the presence of a foreign key to the indicated table.
     */
    public void testExcludeSingleTableWithFkViaRegExp() throws IOException
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip_1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip_2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(modelXml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        if (isUseDelimitedIdentifiers())
        {
            task.setExcludeTableFilter("Round.*_2");
        }
        else
        {
            task.setExcludeTableFilter("ROUND.*_2");
        }
        try
        {
            runTask(task, readModelFromDatabase("roundtriptest"));
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

        if (isUseDelimitedIdentifiers())
        {
            task.setExcludeTables("roundtrip1,roundtrip3");
        }
        else
        {
            task.setExcludeTables("ROUNDTRIP1,ROUNDTRIP3");
        }
        runTask(task, parseDatabaseFromString(model2Xml));
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

        if (isUseDelimitedIdentifiers())
        {
            task.setExcludeTables("roundtrip1,roundtrip3");
        }
        else
        {
            task.setExcludeTables("ROUNDTRIP1,ROUNDTRIP3");
        }
        try
        {
            runTask(task, readModelFromDatabase("roundtriptest"));
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
            "  <table name='Roundtrip_1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_3'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip_2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip_3'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip_2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        if (isUseDelimitedIdentifiers())
        {
            task.setExcludeTableFilter(".*trip_[1|3]");
        }
        else
        {
            task.setExcludeTableFilter(".*TRIP_[1|3]");
        }
        runTask(task, parseDatabaseFromString(model2Xml));
    }

    /**
     * Tests of the excludeTables filter for multiple tables via reg exp. 
     */
    public void testExcludeMultipleTablesWithFKPointingToThemViaRegExp() throws IOException
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip_1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_3'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip_2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_3'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip_3'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(modelXml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        if (isUseDelimitedIdentifiers())
        {
            task.setExcludeTableFilter(".*trip_*[1|3]");
        }
        else
        {
            task.setExcludeTableFilter(".*TRIP_*[1|3]");
        }
        try
        {
            runTask(task, readModelFromDatabase("roundtriptest"));
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
            "  <table name='Roundtrip_1'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_3'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip_2'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_2'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip_3'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='VARCHAR' size='32'/>\n"+
            "    <foreign-key foreignTable='Roundtrip_1'>\n"+
            "      <reference local='avalue' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='Roundtrip_4'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "</database>";
        final String model2Xml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip_4'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(model1Xml);

        DatabaseToDdlTask task = getDatabaseToDdlTaskInstance();

        if (isUseDelimitedIdentifiers())
        {
            task.setIncludeTables("Roundtrip_1,Roundtrip_3,Roundtrip_4");
            task.setExcludeTables("Roundtrip_1,Roundtrip_3");
        }
        else
        {
            task.setIncludeTables("ROUNDTRIP_1,ROUNDTRIP_3,ROUNDTRIP_4");
            task.setExcludeTables("ROUNDTRIP_1,ROUNDTRIP_3");
        }
        runTask(task, parseDatabaseFromString(model2Xml));
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
        runTask(task, parseDatabaseFromString(model2Xml));
    }
}
