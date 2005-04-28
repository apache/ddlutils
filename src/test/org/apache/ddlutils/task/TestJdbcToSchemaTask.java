package org.apache.ddlutils.task;

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

import org.apache.ddlutils.task.JdbcToSchemaTask;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * A JUnit test for JdbcToSchemaTask.java
 *
 * @author <a href="mailto:drfish@cox.net">J. Russell Smyth</a>
 * @version $Revision$
 */
public class TestJdbcToSchemaTask extends TestCase {
    
    public TestJdbcToSchemaTask(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(TestJdbcToSchemaTask.class);
        return suite;
    }
    
    
    /** Test of execute method, of class test.JdbcToSchemaTask. */
    public void testExecute() {
        System.out.println("testExecute");
        JdbcToSchemaTask t = createTask();
        t.execute();
    }
    
    
    protected JdbcToSchemaTask createTask() {
        JdbcToSchemaTask task = new JdbcToSchemaTask();
        task.setDbDriver("org.axiondb.jdbc.AxionDriver");
        task.setDbUrl("jdbc:axiondb:diskdb:target/axiondb");
        task.setDbSchema("TEST");
        task.setOutputFile("target/test.xml");
        return task;
    }
    
    
}
