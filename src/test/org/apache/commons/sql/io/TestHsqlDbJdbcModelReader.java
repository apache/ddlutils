/*
 * $Header: /home/cvs/jakarta-commons-sandbox/jelly/src/java/org/apache/commons/jelly/CompilableTag.java,v 1.5 2002/05/17 15:18:12 jstrachan Exp $
 * $Revision: 1.5 $
 * $Date: 2002/05/17 15:18:12 $
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
 * $Id: CompilableTag.java,v 1.5 2002/05/17 15:18:12 jstrachan Exp $
 */
package org.apache.commons.sql.io;

import javax.sql.DataSource;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.sql.model.Table;

/**
 * JdbcModelReader test harness for the HsqlDb database
 *
 * @author <a href="mailto:dep4b@yahoo.com">Eric Pugh</a>
 * @version $Revision: 1.3 $
 */
public class TestHsqlDbJdbcModelReader extends AbstractTestJdbcModelReader {

    private float driverMajor = -1;
    private int driverMinor = -1;
    
    /** The Log to which logging calls will be made. */
    private static final Log log =
        LogFactory.getLog(TestHsqlDbJdbcModelReader.class);

    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    public void setUp() throws Exception{
        super.setUp();
        
        // Get driver version
        // cant use DatabaseMetaData.getDatabaseMajorVersion() - it dosnt work
        // for hsqldb
        String driverVersion = null;
        try{
            java.sql.Connection conn = getConnection();
            java.sql.DatabaseMetaData dbmd = conn.getMetaData();
            driverVersion = dbmd.getDriverVersion();
        }catch(Exception e){
        }
        int dotpos = driverVersion.lastIndexOf(".");
        String strMajor = null;
        String strMinor = null;
        if(dotpos>=0){
            strMajor = driverVersion.substring(0,dotpos);
            strMinor = driverVersion.substring(dotpos+1,driverVersion.length());
        }
        try{
            driverMajor = Float.parseFloat(strMajor);
        }catch(NumberFormatException nfe){
        }
        try{
            driverMinor = Integer.parseInt(strMinor);
        }catch(NumberFormatException nfe){
        }
    }

    /**
     * A unit test suite for JUnit
     */
    public static Test suite() {
        return new TestSuite(TestHsqlDbJdbcModelReader.class);
    }

    public void doImportForeignKeys(Table srcTable, Table testTable) {
        if(driverMajor>=1.7 && driverMinor>0){
            assertTrue(
                "Foreign Keys Imported", 
                testTable.getForeignKeys().size() ==
                    testTable.getForeignKeys().size());
        }else{
            // HsqlDb < 1.7.1 doesn't support importing forign keys
            assertTrue(
                "No FK imported from HSQLDB",
                testTable.getForeignKeys().size() == 0);
        }

    }

    public void doImportPrimaryKeyColumns(Table srcTable, Table testTable) {
        assertTrue(
            "Table PK's match",
            testTable.getPrimaryKeyColumns().size()
                == srcTable.getPrimaryKeyColumns().size());
    }

    /**
     * Constructor for the TestHsqlDbJdbcModelReader object
     *
     * @param testName
     */
    public TestHsqlDbJdbcModelReader(String testName) {
        super(testName);
    }

    protected String getDatabaseType() {
        return "hsqldb";
    }

    protected DataSource createDataSource() throws Exception {
        return createDataSource(
            "org.hsqldb.jdbcDriver",
            "jdbc:hsqldb:target/hsqldb",
            "sa",
            "");
    }
}
