/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.commons.sql.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JUnit tests.
 * 
 * @version $Id: TestDDLTask.java,v 1.2 2004/02/28 03:35:49 bayard Exp $
 * @see DDLTask
 */
public final class TestDDLTask extends TestCase {

    // TODO: Remove after debugging
    private final Log logger = LogFactory.getLog(getClass());

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static final Test suite() {
        return new TestSuite(TestDDLTask.class);
    }

    public TestDDLTask(String test) {
        super(test);
    }

    public void testExecute() throws Exception {
        // With drop statements
        DDLTask task = createTask();
        task.setOutput(createOutputFile());
        task.execute();

        // Without drop statements
        task.setOutput(createOutputFile());
        task.setDropTables(false);
        task.execute();
    }

    /**
     * Creates a task with the default configuration.
     * @return a task.
     * @throws IOException if an error occurs
     */
    private final DDLTask createTask() throws IOException {
        final DDLTask task = new DDLTask();
        task.setTargetDatabase("postgresql");
        task.setXmlFile(getInputFile());
        return task;
    }

    /**
     * Creates a temporary output file.
     * @return a file
     * @throws IOException if an error occurs
     */
    private final File createOutputFile() throws IOException {
        final File output = File.createTempFile(getName() + "-output", null);
        log("output: " + output);
        return output;
    }

    /**
     * Loads a test input file from the classpath and converts to
     * a <code>java.io.File</code>.
     * 
     * @return the input file
     * @throws IOException if an error occurs
     */
    private final File getInputFile() throws IOException {
        // Reads input file
        final String name = "/datamodel.xml";
        final InputStream is = getClass().getResourceAsStream(name);

        assertNotNull(name + " was not found.", is);

        try {
            // Creates output file
            final File outputFile =
                File.createTempFile(getName() + "-input", null);
            final FileWriter writer = new FileWriter(outputFile);

            // Copies data from URL to file
            final BufferedReader reader =
                new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
            }

            // Closes stream
            writer.close();

            return outputFile;

        } finally {
            is.close();
        }
    }

    // Debug method
    private final void log(Object msg) {
        logger.debug(getName() + " " + msg);
    }

}
