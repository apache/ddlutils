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

import org.apache.ddlutils.TestBuilderBase;
import org.apache.ddlutils.model.Database;

/**
 * Tests the MaxDB builder.
 */
public class TestMaxDbBuilder extends TestBuilderBase
{
    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.TestBuilderBase#getDatabaseName()
     */
    protected String getDatabaseName()
    {
        return MaxDbBuilder.DATABASENAME;
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
            "DROP TABLE coltype CASCADE;\n"+
            "CREATE TABLE coltype\n"+
            "(\n"+
            "    COL_ARRAY           LONG BYTE,\n"+
            "    COL_BIGINT          FIXED(38,0),\n"+
            "    COL_BINARY          LONG BYTE,\n"+
            "    COL_BIT             BOOLEAN,\n"+
            "    COL_BLOB            LONG BYTE,\n"+
            "    COL_BOOLEAN         BOOLEAN,\n"+
            "    COL_CHAR            CHAR(15),\n"+
            "    COL_CLOB            LONG,\n"+
            "    COL_DATALINK        LONG BYTE,\n"+
            "    COL_DATE            DATE,\n"+
            "    COL_DECIMAL         DECIMAL(15,3),\n"+
            "    COL_DECIMAL_NOSCALE DECIMAL(15,0),\n"+
            "    COL_DISTINCT        LONG BYTE,\n"+
            "    COL_DOUBLE          DOUBLE PRECISION,\n"+
            "    COL_FLOAT           DOUBLE PRECISION,\n"+
            "    COL_INTEGER         INTEGER,\n"+
            "    COL_JAVA_OBJECT     LONG BYTE,\n"+
            "    COL_LONGVARBINARY   LONG BYTE,\n"+
            "    COL_LONGVARCHAR     LONG VARCHAR,\n"+
            "    COL_NULL            LONG BYTE,\n"+
            "    COL_NUMERIC         DECIMAL(15,0),\n"+
            "    COL_OTHER           LONG BYTE,\n"+
            "    COL_REAL            REAL,\n"+
            "    COL_REF             LONG BYTE,\n"+
            "    COL_SMALLINT        SMALLINT,\n"+
            "    COL_STRUCT          LONG BYTE,\n"+
            "    COL_TIME            TIME,\n"+
            "    COL_TIMESTAMP       TIMESTAMP,\n"+
            "    COL_TINYINT         SMALLINT,\n"+
            "    COL_VARBINARY       LONG BYTE,\n"+
            "    COL_VARCHAR         VARCHAR(15)\n"+
            ");\n",
            getBuilderOutput());
    }
}
