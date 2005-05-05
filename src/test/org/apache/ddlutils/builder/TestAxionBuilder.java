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
 * Tests the Axion builder.
 */
public class TestAxionBuilder extends TestBuilderBase
{
    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.TestBuilderBase#getDatabaseName()
     */
    protected String getDatabaseName()
    {
        return "Axion";
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
            "DROP TABLE IF EXISTS coltype;\n"+
            "CREATE TABLE coltype\n"+
            "(\n"+
            "    COL_ARRAY           ARRAY,\n"+
            "    COL_BIGINT          BIGINT,\n"+
            "    COL_BINARY          VARBINARY,\n"+
            "    COL_BIT             BOOLEAN,\n"+
            "    COL_BLOB            BLOB,\n"+
            "    COL_BOOLEAN         BOOLEAN,\n"+
            "    COL_CHAR            CHAR,\n"+
            "    COL_CLOB            CLOB,\n"+
            "    COL_DATALINK        DATALINK,\n"+
            "    COL_DATE            DATE,\n"+
            "    COL_DECIMAL         NUMBER,\n"+
            "    COL_DECIMAL_NOSCALE NUMBER,\n"+
            "    COL_DISTINCT        DISTINCT,\n"+
            "    COL_DOUBLE          FLOAT,\n"+
            "    COL_FLOAT           FLOAT,\n"+
            "    COL_INTEGER         INTEGER,\n"+
            "    COL_JAVA_OBJECT     JAVA_OBJECT,\n"+
            "    COL_LONGVARBINARY   VARBINARY,\n"+
            "    COL_LONGVARCHAR     VARCHAR,\n"+
            "    COL_NULL            NULL,\n"+
            "    COL_NUMERIC         NUMBER,\n"+
            "    COL_OTHER           OTHER,\n"+
            "    COL_REAL            FLOAT,\n"+
            "    COL_REF             REF,\n"+
            "    COL_SMALLINT        SHORT,\n"+
            "    COL_STRUCT          STRUCT,\n"+
            "    COL_TIME            TIME,\n"+
            "    COL_TIMESTAMP       TIMESTAMP,\n"+
            "    COL_TINYINT         SHORT,\n"+
            "    COL_VARBINARY       VARBINARY,\n"+
            "    COL_VARCHAR         VARCHAR\n"+
            ");\n",
            getBuilderOutput());
    }
}
