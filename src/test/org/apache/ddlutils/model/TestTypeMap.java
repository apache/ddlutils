package org.apache.ddlutils.model;

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

import org.apache.ddlutils.model.TypeMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test harness for TypeMap Class
 *
 * @author <a href="mailto:john@zenplex.com">John Thorhauer</a>
 * @version $Revision$
 */
public class TestTypeMap
     extends TestCase
{

    /**
     * A unit test suite for JUnit
     */
    public static Test suite()
    {
        return new TestSuite(TestTypeMap.class);
    }

    /**
     * Constructor for the TestTypeMap
     *
     * @param testName
     */
    public TestTypeMap(String testName)
    {
        super(testName);
    }

    /**
     * The JUnit setup method
     */
    protected void setUp()
        throws Exception
    {
        super.setUp();
    }

    /**
     * A unit test for JUnit
     */
    public void testTextType()
        throws Exception
    {
        assertTrue("VARCHAR should be a text type", TypeMap.isTextType("VARCHAR"));
        assertTrue("FLOAT should not be a text type", !TypeMap.isTextType("FLOAT"));
    }

}