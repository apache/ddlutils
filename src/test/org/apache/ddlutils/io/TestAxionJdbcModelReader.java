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
 * JdbcModelReader test harness for the Axion database
 *
 * @author <a href="mailto:dep4b@yahoo.com">Eric Pugh</a>
 * @version $Revision$
 */
public class TestAxionJdbcModelReader extends AbstractTestJdbcModelReader {

    /** The Log to which logging calls will be made. */
    private static final Log log =
        LogFactory.getLog(TestAxionJdbcModelReader.class);

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    /**
     * A unit test suite for JUnit
     */
    public static Test suite() {
        return new TestSuite(TestAxionJdbcModelReader.class);
    }

    public void doImportForeignKeys(Table srcTable, Table testTable) {
        // Axion doesn't support importing forign keys
        assertTrue(
            "No FK imported from Axion",
            testTable.getForeignKeys().size() == 0);
    }

    public void doImportPrimaryKeyColumns(Table srcTable, Table testTable) {
        // Axion doesn't support importing primary keys
        assertTrue(
            "No PK imported from Axion",
            testTable.getPrimaryKeyColumns().size() == 0);
    }

    /**
     * Constructor for the TestAxionJdbcModelReader object
     *
     * @param testName
     */
    public TestAxionJdbcModelReader(String testName) {
        super(testName);
    }

    protected boolean supportsPrimaryKeyMetadata() {
        return false;
    }
    
    protected String getDatabaseType() {
        return "axion";
    }

    protected DataSource createDataSource() throws Exception {
        return createDataSource(
            "org.axiondb.jdbc.AxionDriver",
            "jdbc:axiondb:diskdb:target/axiondb");
    }
}
