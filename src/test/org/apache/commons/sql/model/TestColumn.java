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

package org.apache.commons.sql.model;

import java.sql.Types;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test harness for Column Class
 *
 * @author <a href="mailto:john@zenplex.com">John Thorhauer</a>
 * @version $Revision$
 */
public class TestColumn
     extends TestCase
{

    /**
     * A unit test suite for JUnit
     */
    public static Test suite()
    {
        return new TestSuite(TestColumn.class);
    }

    /**
     * Constructor for the TestColumn
     *
     * @param testName
     */
    public TestColumn(String testName)
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
    public void testColumn()
        throws Exception
    {
        Column column = new Column("Test1", "Test1",Types.INTEGER,"255",true,true,true,"");
        assertTrue("Column is null", column != null);
        assertTrue("Column toString does not end with [name=Test1;type=INTEGER]", 
               ((String)column.toString()).endsWith("[name=Test1;type=INTEGER]"));
               
        assertEquals("INTEGER", column.getType());                           
    }

    public void testTypeName()
        throws Exception
    {
        Column column = new Column("Test1","Test1", "INTEGER","0", true,true,true,"");
        
        assertEquals("INTEGER", column.getType());                           
        assertEquals(Types.INTEGER, column.getTypeCode());

        column = new Column();
        column.setName("foo");
        column.setType("INTEGER");
                
        assertEquals("INTEGER", column.getType());                           
        assertEquals(Types.INTEGER, column.getTypeCode());
        
        column = new Column();
        column.setName("foo");
        column.setTypeCode(Types.INTEGER);
                
        assertEquals("INTEGER", column.getType());                           
        assertEquals(Types.INTEGER, column.getTypeCode());
        
    }

}

