package org.apache.ddlutils.platform;

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

import org.apache.ddlutils.TestPlatformBase;
import org.apache.ddlutils.platform.Oracle9Platform;

/**
 * Tests the Oracle 9 platform.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 231110 $
 */
public class TestOracle9Platform extends TestPlatformBase
{
    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.TestBuilderBase#getDatabaseName()
     */
    protected String getDatabaseName()
    {
        return Oracle9Platform.DATABASENAME;
    }

    /**
     * Tests the column types.
     */
    public void testColumnTypes() throws Exception
    {
        assertEqualsIgnoringWhitespaces(
            "DROP TABLE \"coltype\" CASCADE CONSTRAINTS;\n"+
            "CREATE TABLE \"coltype\"\n"+
            "(\n"+
            "    \"COL_ARRAY\"           BLOB,\n"+
            "    \"COL_BIGINT\"          NUMBER(38,0),\n"+
            "    \"COL_BINARY\"          RAW(254),\n"+
            "    \"COL_BIT\"             NUMBER(1,0),\n"+
            "    \"COL_BLOB\"            BLOB,\n"+
            "    \"COL_BOOLEAN\"         NUMBER(1,0),\n"+
            "    \"COL_CHAR\"            CHAR(15),\n"+
            "    \"COL_CLOB\"            CLOB,\n"+
            "    \"COL_DATALINK\"        BLOB,\n"+
            "    \"COL_DATE\"            DATE,\n"+
            "    \"COL_DECIMAL\"         NUMBER(15,3),\n"+
            "    \"COL_DECIMAL_NOSCALE\" NUMBER(15,0),\n"+
            "    \"COL_DISTINCT\"        BLOB,\n"+
            "    \"COL_DOUBLE\"          NUMBER(38),\n"+
            "    \"COL_FLOAT\"           NUMBER(38),\n"+
            "    \"COL_INTEGER\"         NUMBER(20,0),\n"+
            "    \"COL_JAVA_OBJECT\"     BLOB,\n"+
            "    \"COL_LONGVARBINARY\"   BLOB,\n"+
            "    \"COL_LONGVARCHAR\"     CLOB,\n"+
            "    \"COL_NULL\"            BLOB,\n"+
            "    \"COL_NUMERIC\"         NUMBER(15,0),\n"+
            "    \"COL_OTHER\"           BLOB,\n"+
            "    \"COL_REAL\"            NUMBER(18),\n"+
            "    \"COL_REF\"             BLOB,\n"+
            "    \"COL_SMALLINT\"        NUMBER(5,0),\n"+
            "    \"COL_STRUCT\"          BLOB,\n"+
            "    \"COL_TIME\"            DATE,\n"+
            "    \"COL_TIMESTAMP\"       TIMESTAMP,\n"+
            "    \"COL_TINYINT\"         NUMBER(3,0),\n"+
            "    \"COL_VARBINARY\"       RAW(15),\n"+
            "    \"COL_VARCHAR\"         VARCHAR2(15)\n"+
            ");\n",
            createTestDatabase(COLUMN_TEST_SCHEMA));
    }

    /**
     * Tests the column constraints.
     */
    public void testColumnConstraints() throws Exception
    {
        assertEqualsIgnoringWhitespaces(
            "DROP TABLE \"constraints\" CASCADE CONSTRAINTS;\n" +
            "DROP TRIGGER \"trg_constraints_L_PK_AUTO_INCR\";\n"+
            "DROP SEQUENCE \"seq_constraints_L_PK_AUTO_INCR\";\n" +
            "DROP TRIGGER \"trg_constraints_COL_AUTO_INCR\";\n"+
            "DROP SEQUENCE \"seq_constraints_COL_AUTO_INCR\";\n" +
            "CREATE SEQUENCE \"seq_constraints_L_PK_AUTO_INCR\";\n" +
            "CREATE SEQUENCE \"seq_constraints_COL_AUTO_INCR\";\n" +
            "CREATE TABLE \"constraints\"\n"+
            "(\n"+
            "    \"COL_PK\"               VARCHAR2(32),\n"+
            "    \"COL_PK_AUTO_INCR\"     NUMBER(20,0),\n"+
            "    \"COL_NOT_NULL\"         RAW(100) NOT NULL,\n"+
            "    \"COL_NOT_NULL_DEFAULT\" NUMBER(38) DEFAULT '-2.0' NOT NULL,\n"+
            "    \"COL_DEFAULT\"          CHAR(4) DEFAULT 'test',\n"+
            "    \"COL_AUTO_INCR\"        NUMBER(38,0),\n"+
            "    PRIMARY KEY (\"COL_PK\", \"COL_PK_AUTO_INCR\")\n"+
            ");\n"+
            "CREATE OR REPLACE TRIGGER \"trg_constraints_L_PK_AUTO_INCR\" BEFORE INSERT ON \"constraints\" FOR EACH ROW\n"+
            "BEGIN\n"+
            "  SELECT \"seq_constraints_L_PK_AUTO_INCR\".nextval INTO :new.\"COL_PK_AUTO_INCR\" FROM dual;\n"+
            "END;\n"+
            "CREATE OR REPLACE TRIGGER \"trg_constraints_COL_AUTO_INCR\" BEFORE INSERT ON \"constraints\" FOR EACH ROW\n"+
            "BEGIN\n"+
            "  SELECT \"seq_constraints_COL_AUTO_INCR\".nextval INTO :new.\"COL_AUTO_INCR\" FROM dual;\n"+
            "END;\n",
            createTestDatabase(COLUMN_CONSTRAINT_TEST_SCHEMA));
    }

    /**
     * Tests the table constraints.
     */
    public void testTableConstraints() throws Exception
    {
        assertEqualsIgnoringWhitespaces(
            "DROP TABLE \"table3\" CASCADE CONSTRAINTS;\n"+
            "DROP TABLE \"table2\" CASCADE CONSTRAINTS;\n"+
            "DROP TABLE \"table1\" CASCADE CONSTRAINTS;\n"+
            "CREATE TABLE \"table1\"\n"+
            "(\n"+
            "    \"COL_PK_1\"    VARCHAR2(32) NOT NULL,\n"+
            "    \"COL_PK_2\"    NUMBER(20,0),\n"+
            "    \"COL_INDEX_1\" RAW(100) NOT NULL,\n"+
            "    \"COL_INDEX_2\" NUMBER(38) NOT NULL,\n"+
            "    \"COL_INDEX_3\" CHAR(4),\n"+
            "    PRIMARY KEY (\"COL_PK_1\", \"COL_PK_2\")\n"+
            ");\n"+
            "CREATE INDEX \"testindex1\" ON \"table1\" (\"COL_INDEX_2\");\n"+
            "CREATE UNIQUE INDEX \"testindex2\" ON \"table1\" (\"COL_INDEX_3\", \"COL_INDEX_1\");\n"+
            "CREATE TABLE \"table2\"\n"+
            "(\n"+
            "    \"COL_PK\"   NUMBER(20,0),\n"+
            "    \"COL_FK_1\" NUMBER(20,0),\n"+
            "    \"COL_FK_2\" VARCHAR2(32) NOT NULL,\n"+
            "    PRIMARY KEY (\"COL_PK\")\n"+
            ");\n"+
            "CREATE TABLE \"table3\"\n"+
            "(\n"+
            "    \"COL_PK\" VARCHAR2(16),\n"+
            "    \"COL_FK\" NUMBER(20,0) NOT NULL,\n"+
            "    PRIMARY KEY (\"COL_PK\")\n"+
            ");\n"+
            "ALTER TABLE \"table2\" ADD CONSTRAINT \"table2_FK_COL_F_OL_FK_2_table1\" FOREIGN KEY (\"COL_FK_1\", \"COL_FK_2\") REFERENCES \"table1\" (\"COL_PK_2\", \"COL_PK_1\");\n"+
            "ALTER TABLE \"table3\" ADD CONSTRAINT \"testfk\" FOREIGN KEY (\"COL_FK\") REFERENCES \"table2\" (\"COL_PK\");\n",
            createTestDatabase(TABLE_CONSTRAINT_TEST_SCHEMA));
    }
}
