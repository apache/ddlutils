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
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import junit.framework.Test;

import org.apache.ddlutils.io.DatabaseIO;

/**
 * Tests the writeDataToDatabase sub task.
 * 
 * @version $Revision: $
 */
public class TestWriteDataToDatabaseCommand extends TestTaskBase
{
    /**
     * Parameterized test case pattern.
     * 
     * @return The tests
     */
    public static Test suite() throws Exception
    {
        return getTests(TestWriteDataToDatabaseCommand.class);
    }

    /**
     * Adds the writeDataToDatabase sub task to the given task, executes it, and checks its output.
     *
     * @param task          The task
     * @param dataXml       The data xml to write
     * @param useBatchMode  Whether to use batch mode for inserting the data
     * @param ensureFkOrder Whether to ensure foreign key order
     */
    private void runTask(DatabaseToDdlTask task, String dataXml, boolean useBatchMode, boolean ensureFkOrder) throws IOException
    {
        WriteDataToDatabaseCommand subTask = new WriteDataToDatabaseCommand();
        File                       tmpFile = File.createTempFile("schema", ".xml");
        FileWriter                 writer  = null;

        try
        {
            writer = new FileWriter(tmpFile);

            writer.write(dataXml);
            writer.close();

            subTask.setDataFile(tmpFile);
            subTask.setBatchSize(100);
            subTask.setFailOnError(true);
            subTask.setUseBatchMode(useBatchMode);
            subTask.setEnsureForeignKeyOrder(ensureFkOrder);
            task.addWriteDataToDatabase(subTask);
            task.setModelName("roundtriptest");
            task.execute();
        }
        finally
        {
            if (writer != null)
            {
                try
                {
                    writer.close();
                }
                catch (IOException ex)
                {
                    getLog().error("Could not close the writer for the temporary file " + tmpFile.getAbsolutePath(), ex);
                }
            }
            if (!tmpFile.delete())
            {
                getLog().warn("Could not delete temporary file " + tmpFile.getAbsolutePath());
            }
        }
    }

    /**
     * Basic test that creates a schema and puts some data into it.
     */
    public void testSimple() throws Exception
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "</database>";
        final String dataXml =
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<data>\n" +
            "  <roundtrip pk='val1' avalue='1'/>\n" +
            "  <roundtrip pk='val2' avalue='2'/>\n" +
            "  <roundtrip pk='val3' avalue='3'/>\n" +
            "</data>";

        createDatabase(modelXml);

        runTask(getDatabaseToDdlTaskInstance(), dataXml, false, false);

        List beans = getRows("roundtrip", "pk");

        assertEquals(3, beans.size());
        assertEquals((Object)"val1", beans.get(0), "pk");
        assertEquals(new Integer(1), beans.get(0), "avalue");
        assertEquals((Object)"val2", beans.get(1), "pk");
        assertEquals(new Integer(2), beans.get(1), "avalue");
        assertEquals((Object)"val3", beans.get(2), "pk");
        assertEquals(new Integer(3), beans.get(2), "avalue");
    }

    /**
     * Tests data insertion in batch mode.
     */
    public void testBatchMode() throws Exception
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip'>\n"+
            "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n"+
            "    <column name='avalue' type='INTEGER'/>\n"+
            "  </table>\n"+
            "</database>";

        StringBuffer dataXml = new StringBuffer();
        final int    numObjs = 2000;

        dataXml.append("<?xml version='1.0' encoding='ISO-8859-1'?>\n<data>");
        for (int idx = 0; idx < numObjs; idx++)
        {
            dataXml.append("  <roundtrip pk='val");
            dataXml.append(idx);
            dataXml.append("' avalue='");
            dataXml.append(idx);
            dataXml.append("'/>\n");
        }
        dataXml.append("</data>");

        createDatabase(modelXml);

        runTask(getDatabaseToDdlTaskInstance(), dataXml.toString(), true, false);

        List beans = getRows("roundtrip", "avalue");

        assertEquals(numObjs, beans.size());
        for (int idx = 0; idx < numObjs; idx++)
        {
            assertEquals((Object)("val" + idx), beans.get(idx), "pk");
            assertEquals(new Integer(idx),      beans.get(idx), "avalue");
        }
    }
}
