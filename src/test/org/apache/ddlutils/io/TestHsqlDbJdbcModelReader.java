package org.apache.ddlutils.io;

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

import javax.sql.DataSource;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.model.Table;

/**
 * JdbcModelReader test harness for the HsqlDb database
 *
 * @author <a href="mailto:dep4b@yahoo.com">Eric Pugh</a>
 * @version $Revision$
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
