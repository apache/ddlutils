package org.apache.commons.sql.model;

/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 *
 * $Id: TestColumn.java,v 1.1 2002/09/20 21:28:00 thorhauer Exp $
 */

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.SimpleLog;

import org.apache.commons.sql.model.Column;

/**
 * Test harness for Column Class
 *
 * @author <a href="mailto:john@zenplex.com">John Thorhauer</a>
 * @version $Revision: 1.1 $
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
        Column column = new Column("Test1","INTEGER",255,true,true,true);
        assertTrue("Column is null", column != null);
        assertTrue("Column toString does not end with [name=Test1;type=INTEGER]", 
               ((String)column.toString()).endsWith("[name=Test1;type=INTEGER]"));
    }

}

