/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons-sandbox//sql/src/test/org/apache/commons/sql/task/TestDDLTask.java,v 1.1 2004/01/06 19:28:00 matth Exp $
 * $Revision: 1.1 $
 * $Date: 2004/01/06 19:28:00 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 * 
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
 * @version $Id: TestDDLTask.java,v 1.1 2004/01/06 19:28:00 matth Exp $
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
