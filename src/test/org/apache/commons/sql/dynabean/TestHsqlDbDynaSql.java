/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.commons.sql.dynabean;

import javax.sql.DataSource;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * DynaSql test harness for the HsqlDb database
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.3 $
 */
public class TestHsqlDbDynaSql extends AbstractTestDynaSql
{
    public static void main( String[] args ) 
    {
        TestRunner.run( suite() );
    }
    
    /**
     * A unit test suite for JUnit
     */
    public static Test suite()
    {
        return new TestSuite(TestHsqlDbDynaSql.class);
    }

    /**
     * Constructor for the TestHsqlDbDynaSql object
     *
     * @param testName
     */
    public TestHsqlDbDynaSql(String testName)
    {
        super(testName);
    }

    protected String getDatabaseType() 
    {
        return "hsqldb";
    }
    
    protected DataSource createDataSource() throws Exception
    {
        return createDataSource(
            "org.hsqldb.jdbcDriver", 
            "jdbc:hsqldb:target/hsqldb", 
            "sa", 
            ""
        );
    }
}

