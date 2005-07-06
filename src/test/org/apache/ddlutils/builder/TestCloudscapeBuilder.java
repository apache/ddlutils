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
 * Tests the PostgreSQL builder.
 */
public class TestCloudscapeBuilder extends TestBuilderBase
{
    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.TestBuilderBase#getDatabaseName()
     */
    protected String getDatabaseName()
    {
        return CloudscapeBuilder.DATABASENAME;
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
            "CREATE TABLE coltype\n"+
            "(\n"+
            "    COL_ARRAY           BLOB,\n"+
            "    COL_BIGINT          BIGINT,\n"+
            "    COL_BINARY          CHAR(254) FOR BIT DATA,\n"+
            "    COL_BIT             CHAR FOR BIT DATA,\n"+
            "    COL_BLOB            BLOB,\n"+
            "    COL_BOOLEAN         CHAR FOR BIT DATA,\n"+
            "    COL_CHAR            CHAR(15),\n"+
            "    COL_CLOB            CLOB,\n"+
            "    COL_DATALINK        LONG VARCHAR FOR BIT DATA,\n"+
            "    COL_DATE            DATE,\n"+
            "    COL_DECIMAL         DECIMAL(15,3),\n"+
            "    COL_DECIMAL_NOSCALE DECIMAL(15,0),\n"+
            "    COL_DISTINCT        BLOB,\n"+
            "    COL_DOUBLE          DOUBLE PRECISION,\n"+
            "    COL_FLOAT           DOUBLE PRECISION,\n"+
            "    COL_INTEGER         INTEGER,\n"+
            "    COL_JAVA_OBJECT     BLOB,\n"+
            "    COL_LONGVARBINARY   LONG VARCHAR FOR BIT DATA,\n"+
            "    COL_LONGVARCHAR     LONG VARCHAR,\n"+
            "    COL_NULL            LONG VARCHAR FOR BIT DATA,\n"+
            "    COL_NUMERIC         NUMERIC(15,0),\n"+
            "    COL_OTHER           BLOB,\n"+
            "    COL_REAL            REAL,\n"+
            "    COL_REF             LONG VARCHAR FOR BIT DATA,\n"+
            "    COL_SMALLINT        SMALLINT,\n"+
            "    COL_STRUCT          BLOB,\n"+
            "    COL_TIME            TIME,\n"+
            "    COL_TIMESTAMP       TIMESTAMP,\n"+
            "    COL_TINYINT         SMALLINT,\n"+
            "    COL_VARBINARY       VARCHAR(15) FOR BIT DATA,\n"+
            "    COL_VARCHAR         VARCHAR(15)\n"+
            ");\n",
            getBuilderOutput());
    }
}
