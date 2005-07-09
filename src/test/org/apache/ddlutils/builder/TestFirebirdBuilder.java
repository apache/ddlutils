package org.apache.ddlutils.builder;

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

import org.apache.ddlutils.model.Database;

/**
 * Tests the Firebird builder.
 */
public class TestFirebirdBuilder extends TestBuilderBase
{
    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.TestBuilderBase#getDatabaseName()
     */
    protected String getDatabaseName()
    {
        return FirebirdBuilder.DATABASENAME;
    }

    /**
     * Tests the column types.
     */
    public void testColumnTypes() throws Exception
    {
        Database testDb = parseDatabaseFromString(COLUMN_TEST_SCHEMA);

        // we're turning the comment creation off to make testing easier
        getBuilder().setCommentsSupported(false);
        getBuilder().createDatabase(testDb);

        assertEqualsIgnoringWhitespaces(
            "DROP TABLE coltype;\n"+
            "COMMIT;\n"+
            "CREATE TABLE coltype\n"+
            "(\n"+
            "    COL_ARRAY           BLOB ,\n"+
            "    COL_BIGINT          DECIMAL(38,0),\n"+
            "    COL_BINARY          CHAR CHARACTER SET OCTETS,\n"+
            "    COL_BIT             DECIMAL(1,0),\n"+
            "    COL_BLOB            BLOB ,\n"+
            "    COL_BOOLEAN         DECIMAL(1,0),\n"+
            "    COL_CHAR            CHAR(15),\n"+
            "    COL_CLOB            BLOB SUB_TYPE TEXT,\n"+
            "    COL_DATALINK        BLOB,\n"+
            "    COL_DATE            DATE,\n"+
            "    COL_DECIMAL         DECIMAL(15,3),\n"+
            "    COL_DECIMAL_NOSCALE DECIMAL(15,0),\n"+
            "    COL_DISTINCT        BLOB,\n"+
            "    COL_DOUBLE          DOUBLE PRECISION,\n"+
            "    COL_FLOAT           DOUBLE PRECISION,\n"+
            "    COL_INTEGER         INTEGER,\n"+
            "    COL_JAVA_OBJECT     BLOB,\n"+
            "    COL_LONGVARBINARY   BLOB,\n"+
            "    COL_LONGVARCHAR     BLOB SUB_TYPE TEXT,\n"+
            "    COL_NULL            BLOB,\n"+
            "    COL_NUMERIC         NUMERIC(15,0),\n"+
            "    COL_OTHER           BLOB,\n"+
            "    COL_REAL            FLOAT,\n"+
            "    COL_REF             BLOB,\n"+
            "    COL_SMALLINT        SMALLINT,\n"+
            "    COL_STRUCT          BLOB,\n"+
            "    COL_TIME            TIME,\n"+
            "    COL_TIMESTAMP       TIMESTAMP,\n"+
            "    COL_TINYINT         SMALLINT,\n"+
            "    COL_VARBINARY       VARCHAR(15) CHARACTER SET OCTETS,\n"+
            "    COL_VARCHAR         VARCHAR(15)\n"+
            ");\n"+
            "COMMIT;\n",
            getBuilderOutput());
    }
}
