package org.apache.commons.sql.model;

/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 *
 * $Id: TestTypeMap.java,v 1.1 2002/09/20 21:28:00 thorhauer Exp $
 */

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.SimpleLog;

/**
 * Test harness for TypeMap Class
 *
 * @author <a href="mailto:john@zenplex.com">John Thorhauer</a>
 * @version $Revision: 1.1 $
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