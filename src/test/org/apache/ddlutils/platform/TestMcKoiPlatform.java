package org.apache.ddlutils.platform;

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

import org.apache.ddlutils.TestPlatformBase;
import org.apache.ddlutils.platform.MckoiPlatform;

/**
 * Tests the McKoi platform.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 231110 $
 */
public class TestMcKoiPlatform extends TestPlatformBase
{
    /**
     * {@inheritDoc}
     */
    protected String getDatabaseName()
    {
        return MckoiPlatform.DATABASENAME;
    }

    /**
     * Tests the column types.
     */
    public void testColumnTypes() throws Exception
    {
        assertEqualsIgnoringWhitespaces(
            "DROP TABLE IF EXISTS \"coltype\";\n"+
            "CREATE TABLE \"coltype\"\n"+
            "(\n"+
            "    \"COL_ARRAY\"           BLOB,\n"+
            "    \"COL_BIGINT\"          BIGINT,\n"+
            "    \"COL_BINARY\"          BINARY,\n"+
            "    \"COL_BIT\"             BOOLEAN,\n"+
            "    \"COL_BLOB\"            BLOB,\n"+
            "    \"COL_BOOLEAN\"         BOOLEAN,\n"+
            "    \"COL_CHAR\"            CHAR(15),\n"+
            "    \"COL_CLOB\"            CLOB,\n"+
            "    \"COL_DATALINK\"        BLOB,\n"+
            "    \"COL_DATE\"            DATE,\n"+
            "    \"COL_DECIMAL\"         DECIMAL(15,3),\n"+
            "    \"COL_DECIMAL_NOSCALE\" DECIMAL(15,0),\n"+
            "    \"COL_DISTINCT\"        BLOB,\n"+
            "    \"COL_DOUBLE\"          DOUBLE,\n"+
            "    \"COL_FLOAT\"           DOUBLE,\n"+
            "    \"COL_INTEGER\"         INTEGER,\n"+
            "    \"COL_JAVA_OBJECT\"     JAVA_OBJECT,\n"+
            "    \"COL_LONGVARBINARY\"   LONGVARBINARY,\n"+
            "    \"COL_LONGVARCHAR\"     LONGVARCHAR,\n"+
            "    \"COL_NULL\"            BLOB,\n"+
            "    \"COL_NUMERIC\"         NUMERIC(15,0),\n"+
            "    \"COL_OTHER\"           BLOB,\n"+
            "    \"COL_REAL\"            REAL,\n"+
            "    \"COL_REF\"             BLOB,\n"+
            "    \"COL_SMALLINT\"        SMALLINT,\n"+
            "    \"COL_STRUCT\"          BLOB,\n"+
            "    \"COL_TIME\"            TIME,\n"+
            "    \"COL_TIMESTAMP\"       TIMESTAMP,\n"+
            "    \"COL_TINYINT\"         TINYINT,\n"+
            "    \"COL_VARBINARY\"       VARBINARY(15),\n"+
            "    \"COL_VARCHAR\"         VARCHAR(15)\n"+
            ");\n",
            createTestDatabase(COLUMN_TEST_SCHEMA));
    }

    /**
     * Tests the column constraints.
     */
    public void testColumnConstraints() throws Exception
    {
        // note that this is not valid SQL as obviously only one auto increment field
        // can be defined for each table
        assertEqualsIgnoringWhitespaces(
            "DROP TABLE IF EXISTS \"constraints\";\n" +
            "CREATE TABLE \"constraints\"\n"+
            "(\n"+
            "    \"COL_PK\"               VARCHAR(32),\n"+
            "    \"COL_PK_AUTO_INCR\"     INTEGER DEFAULT UNIQUEKEY(\"constraints\") + 1,\n"+
            "    \"COL_NOT_NULL\"         BINARY(100) NOT NULL,\n"+
            "    \"COL_NOT_NULL_DEFAULT\" DOUBLE DEFAULT -2.0 NOT NULL,\n"+
            "    \"COL_DEFAULT\"          CHAR(4) DEFAULT 'test',\n"+
            "    \"COL_AUTO_INCR\"        BIGINT DEFAULT UNIQUEKEY(\"constraints\") + 1,\n"+
            "    PRIMARY KEY (\"COL_PK\", \"COL_PK_AUTO_INCR\")\n"+
            ");\n",
            createTestDatabase(COLUMN_CONSTRAINT_TEST_SCHEMA));
    }

    /**
     * Tests the table constraints.
     */
    public void testTableConstraints() throws Exception
    {
        assertEqualsIgnoringWhitespaces(
            "ALTER TABLE \"table3\" DROP CONSTRAINT \"testfk\";\n"+
            "ALTER TABLE \"table2\" DROP CONSTRAINT \"table2_FK_COL_FK_1_COL_FK_2_table1\";\n"+
            "DROP TABLE IF EXISTS \"table3\";\n"+
            "DROP TABLE IF EXISTS \"table2\";\n"+
            "DROP TABLE IF EXISTS \"table1\";\n"+
            "CREATE TABLE \"table1\"\n"+
            "(\n"+
            "    \"COL_PK_1\"    VARCHAR(32) NOT NULL,\n"+
            "    \"COL_PK_2\"    INTEGER,\n"+
            "    \"COL_INDEX_1\" BINARY(100) NOT NULL,\n"+
            "    \"COL_INDEX_2\" DOUBLE NOT NULL,\n"+
            "    \"COL_INDEX_3\" CHAR(4),\n"+
            "    PRIMARY KEY (\"COL_PK_1\", \"COL_PK_2\")\n"+
            ");\n"+
            "CREATE INDEX \"testindex1\" ON \"table1\" (\"COL_INDEX_2\");\n"+
            "CREATE UNIQUE INDEX \"testindex2\" ON \"table1\" (\"COL_INDEX_3\", \"COL_INDEX_1\");\n"+
            "CREATE TABLE \"table2\"\n"+
            "(\n"+
            "    \"COL_PK\"   INTEGER,\n"+
            "    \"COL_FK_1\" INTEGER,\n"+
            "    \"COL_FK_2\" VARCHAR(32) NOT NULL,\n"+
            "    PRIMARY KEY (\"COL_PK\")\n"+
            ");\n"+
            "CREATE TABLE \"table3\"\n"+
            "(\n"+
            "    \"COL_PK\" VARCHAR(16),\n"+
            "    \"COL_FK\" INTEGER NOT NULL,\n"+
            "    PRIMARY KEY (\"COL_PK\")\n"+
            ");\n"+
            "ALTER TABLE \"table2\" ADD CONSTRAINT \"table2_FK_COL_FK_1_COL_FK_2_table1\" FOREIGN KEY (\"COL_FK_1\", \"COL_FK_2\") REFERENCES \"table1\" (\"COL_PK_2\", \"COL_PK_1\");\n"+
            "ALTER TABLE \"table3\" ADD CONSTRAINT \"testfk\" FOREIGN KEY (\"COL_FK\") REFERENCES \"table2\" (\"COL_PK\");\n",
            createTestDatabase(TABLE_CONSTRAINT_TEST_SCHEMA));
    }
}
