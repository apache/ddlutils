/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons-sandbox//sql/src/test/org/apache/commons/sql/model/TestColumn.java,v 1.4 2003/04/30 11:27:30 jstrachan Exp $
 * $Revision: 1.4 $
 * $Date: 2003/04/30 11:27:30 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
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
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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
 * $Id: TestColumn.java,v 1.4 2003/04/30 11:27:30 jstrachan Exp $
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
 * @version $Revision: 1.4 $
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
        Column column = new Column("Test1",Types.INTEGER,255,true,true,true,"");
        assertTrue("Column is null", column != null);
        assertTrue("Column toString does not end with [name=Test1;type=INTEGER]", 
               ((String)column.toString()).endsWith("[name=Test1;type=INTEGER]"));
               
        assertEquals("INTEGER", column.getType());                           
    }

    public void testTypeName()
        throws Exception
    {
        Column column = new Column("Test1","INTEGER",0, true,true,true,"");
        
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

