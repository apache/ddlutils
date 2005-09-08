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
import org.apache.ddlutils.platform.HsqlDbPlatform;

/**
 * Tests the Hsqldb platform.
 * 
 * @author David Carlson
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 231110 $
 */
public class TestHsqlDbPlatform extends TestPlatformBase
{
    /*
     * (non-Javadoc)
     * @see org.apache.ddlutils.builder.TestPlatformBase#getDatabaseName()
     */
    protected String getDatabaseName()
    {
        return HsqlDbPlatform.DATABASENAME;
    }

    /**
     * Tests the column types.
     */
    public void testColumnTypes() throws Exception
    {
        assertEqualsIgnoringWhitespaces(
            "DROP TABLE coltype IF EXISTS;\n" + //
            "CREATE TABLE coltype\n" + //
            "(\n" + //
            "    COL_ARRAY           LONGVARBINARY,\n"+
            "    COL_BIGINT          BIGINT,\n"+
            "    COL_BINARY          BINARY,\n"+
            "    COL_BIT             BIT,\n"+
            "    COL_BLOB            LONGVARBINARY,\n"+
            "    COL_BOOLEAN         BIT,\n"+
            "    COL_CHAR            CHAR(15),\n"+
            "    COL_CLOB            LONGVARCHAR,\n"+
            "    COL_DATALINK        LONGVARBINARY,\n"+
            "    COL_DATE            DATE,\n"+
            "    COL_DECIMAL         DECIMAL(15,3),\n"+
            "    COL_DECIMAL_NOSCALE DECIMAL(15,0),\n"+
            "    COL_DISTINCT        LONGVARBINARY,\n"+
            "    COL_DOUBLE          DOUBLE,\n"+
            "    COL_FLOAT           DOUBLE,\n"+
            "    COL_INTEGER         INTEGER,\n"+
            "    COL_JAVA_OBJECT     OBJECT,\n"+
            "    COL_LONGVARBINARY   LONGVARBINARY,\n"+
            "    COL_LONGVARCHAR     LONGVARCHAR,\n"+
            "    COL_NULL            LONGVARBINARY,\n"+
            "    COL_NUMERIC         NUMERIC(15,0),\n"+
            "    COL_OTHER           OTHER,\n"+
            "    COL_REAL            REAL,\n"+
            "    COL_REF             LONGVARBINARY,\n"+
            "    COL_SMALLINT        SMALLINT,\n"+
            "    COL_STRUCT          LONGVARBINARY,\n"+
            "    COL_TIME            TIME,\n"+
            "    COL_TIMESTAMP       TIMESTAMP,\n"+
            "    COL_TINYINT         TINYINT,\n"+
            "    COL_VARBINARY       VARBINARY(15),\n"+
            "    COL_VARCHAR         VARCHAR(15)\n"+
            ");\n",
            createTestDatabase());
    }
}
