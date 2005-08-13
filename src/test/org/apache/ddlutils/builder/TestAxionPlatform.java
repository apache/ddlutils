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

import org.apache.ddlutils.TestPlatformBase;
import org.apache.ddlutils.platform.AxionPlatform;

/**
 * Tests the Axion builder.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 231110 $
 */
public class TestAxionPlatform extends TestPlatformBase
{
    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.TestPlatformBase#getDatabaseName()
     */
    protected String getDatabaseName()
    {
        return AxionPlatform.DATABASENAME;
    }

    /**
     * Tests the column types.
     */
    public void testColumnTypes() throws Exception
    {
        assertEqualsIgnoringWhitespaces(
            "DROP TABLE IF EXISTS coltype;\n"+
            "CREATE TABLE coltype\n"+
            "(\n"+
            "    COL_ARRAY           BLOB,\n"+
            "    COL_BIGINT          BIGINT,\n"+
            "    COL_BINARY          VARBINARY,\n"+
            "    COL_BIT             BOOLEAN,\n"+
            "    COL_BLOB            BLOB,\n"+
            "    COL_BOOLEAN         BOOLEAN,\n"+
            "    COL_CHAR            CHAR(15),\n"+
            "    COL_CLOB            CLOB,\n"+
            "    COL_DATALINK        VARBINARY,\n"+
            "    COL_DATE            DATE,\n"+
            "    COL_DECIMAL         NUMBER(15,3),\n"+
            "    COL_DECIMAL_NOSCALE NUMBER(15,0),\n"+
            "    COL_DISTINCT        VARBINARY,\n"+
            "    COL_DOUBLE          FLOAT,\n"+
            "    COL_FLOAT           FLOAT,\n"+
            "    COL_INTEGER         INTEGER,\n"+
            "    COL_JAVA_OBJECT     JAVA_OBJECT,\n"+
            "    COL_LONGVARBINARY   VARBINARY,\n"+
            "    COL_LONGVARCHAR     VARCHAR,\n"+
            "    COL_NULL            VARBINARY,\n"+
            "    COL_NUMERIC         NUMBER(15,0),\n"+
            "    COL_OTHER           BLOB,\n"+
            "    COL_REAL            FLOAT,\n"+
            "    COL_REF             VARBINARY,\n"+
            "    COL_SMALLINT        SHORT,\n"+
            "    COL_STRUCT          VARBINARY,\n"+
            "    COL_TIME            TIME,\n"+
            "    COL_TIMESTAMP       TIMESTAMP,\n"+
            "    COL_TINYINT         SHORT,\n"+
            "    COL_VARBINARY       VARBINARY(15),\n"+
            "    COL_VARCHAR         VARCHAR(15)\n"+
            ");\n",
            createTestDatabase());
    }
}
