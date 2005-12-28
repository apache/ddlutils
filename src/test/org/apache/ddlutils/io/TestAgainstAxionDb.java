package org.apache.ddlutils.io;

/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import org.apache.ddlutils.TestDatabaseWriterBase;
import org.apache.ddlutils.platform.axion.AxionPlatform;

/**
 * Performs tests against Axion.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class TestAgainstAxionDb extends TestDatabaseWriterBase
{
    /** The database schema for testing all column types that Axion supports. */
    public static final String COLUMN_TEST_SCHEMA =
        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
        "<database name=\"datatypetest\">\n"+
        "  <table name=\"coltype\">\n"+
        "    <column name=\"COL_BIGINT\"          type=\"BIGINT\"/>\n"+
        "    <column name=\"COL_BINARY\"          type=\"BINARY\"/>\n"+
        "    <column name=\"COL_BIT\"             type=\"BIT\"/>\n"+
        "    <column name=\"COL_BLOB\"            type=\"BLOB\"/>\n"+
        "    <column name=\"COL_BOOLEAN\"         type=\"BOOLEAN\"/>\n"+
        "    <column name=\"COL_CHAR\"            size=\"15\" type=\"CHAR\"/>\n"+
        "    <column name=\"COL_CLOB\"            type=\"CLOB\"/>\n"+
        "    <column name=\"COL_DATE\"            type=\"DATE\"/>\n"+
        "    <column name=\"COL_DECIMAL\"         type=\"DECIMAL\" scale=\"3\" size=\"15\"/>\n"+
        "    <column name=\"COL_DECIMAL_NOSCALE\" type=\"DECIMAL\" size=\"15\"/>\n"+
        "    <column name=\"COL_DOUBLE\"          type=\"DOUBLE\"/>\n"+
        "    <column name=\"COL_FLOAT\"           type=\"FLOAT\"/>\n"+
        "    <column name=\"COL_INTEGER\"         type=\"INTEGER\"/>\n"+
        "    <column name=\"COL_JAVA_OBJECT\"     type=\"JAVA_OBJECT\"/>\n"+
        "    <column name=\"COL_LONGVARBINARY\"   type=\"LONGVARBINARY\"/>\n"+
        "    <column name=\"COL_LONGVARCHAR\"     type=\"LONGVARCHAR\"/>\n"+
        "    <column name=\"COL_NUMERIC\"         type=\"NUMERIC\" size=\"15\" />\n"+
        "    <column name=\"COL_REAL\"            type=\"REAL\"/>\n"+
        "    <column name=\"COL_SMALLINT\"        type=\"SMALLINT\"/>\n"+
        "    <column name=\"COL_TIME\"            type=\"TIME\"/>\n"+
        "    <column name=\"COL_TIMESTAMP\"       type=\"TIMESTAMP\"/>\n"+
        "    <column name=\"COL_TINYINT\"         type=\"TINYINT\"/>\n"+
        "    <column name=\"COL_VARBINARY\"       size=\"15\" type=\"VARBINARY\"/>\n"+
        "    <column name=\"COL_VARCHAR\"         size=\"15\" type=\"VARCHAR\"/>\n"+
        "  </table>\n"+
        "</database>";

    /**
     * Tests the database creation.
     */
    public void testCreation() throws Exception
    {
        createDatabase(COLUMN_TEST_SCHEMA);
    }

    /**
     * @see org.apache.ddlutils.TestDatabaseWriterBase#getDatabaseName()
     */
    protected String getDatabaseName()
    {
        return AxionPlatform.DATABASENAME;
    }
}
