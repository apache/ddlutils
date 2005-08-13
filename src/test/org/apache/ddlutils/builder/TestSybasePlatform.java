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
import org.apache.ddlutils.platform.SybasePlatform;

/**
 * Tests the Sybase platform.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 231110 $
 */
public class TestSybasePlatform extends TestPlatformBase
{
    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.TestPlatformBase#getDatabaseName()
     */
    protected String getDatabaseName()
    {
        return SybasePlatform.DATABASENAME;
    }

    /**
     * Tests the column types.
     */
    public void testColumnTypes() throws Exception
    {
        assertEqualsIgnoringWhitespaces(
            "IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'coltype')\n"+
            "BEGIN\n"+
            "    DROP TABLE coltype\n"+
            "END;\n"+
            "CREATE TABLE coltype\n"+
            "(\n"+
            "    COL_ARRAY           IMAGE,\n"+
            "    COL_BIGINT          DECIMAL(19,0),\n"+
            "    COL_BINARY          BINARY,\n"+
            "    COL_BIT             BIT,\n"+
            "    COL_BLOB            IMAGE,\n"+
            "    COL_BOOLEAN         BIT,\n"+
            "    COL_CHAR            CHAR(15),\n"+
            "    COL_CLOB            TEXT,\n"+
            "    COL_DATALINK        IMAGE,\n"+
            "    COL_DATE            DATETIME,\n"+
            "    COL_DECIMAL         DECIMAL(15,3),\n"+
            "    COL_DECIMAL_NOSCALE DECIMAL(15,0),\n"+
            "    COL_DISTINCT        IMAGE,\n"+
            "    COL_DOUBLE          DOUBLE PRECISION,\n"+
            "    COL_FLOAT           DOUBLE PRECISION,\n"+
            "    COL_INTEGER         INT,\n"+
            "    COL_JAVA_OBJECT     IMAGE,\n"+
            "    COL_LONGVARBINARY   IMAGE,\n"+
            "    COL_LONGVARCHAR     TEXT,\n"+
            "    COL_NULL            IMAGE,\n"+
            "    COL_NUMERIC         NUMERIC(15,0),\n"+
            "    COL_OTHER           IMAGE,\n"+
            "    COL_REAL            REAL,\n"+
            "    COL_REF             IMAGE,\n"+
            "    COL_SMALLINT        SMALLINT,\n"+
            "    COL_STRUCT          IMAGE,\n"+
            "    COL_TIME            DATETIME,\n"+
            "    COL_TIMESTAMP       DATETIME,\n"+
            "    COL_TINYINT         SMALLINT,\n"+
            "    COL_VARBINARY       VARBINARY(15),\n"+
            "    COL_VARCHAR         VARCHAR(15)\n"+
            ");\n",
            createTestDatabase());
    }
}
