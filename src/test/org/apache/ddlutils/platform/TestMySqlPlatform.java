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
import org.apache.ddlutils.platform.MySqlPlatform;

/**
 * Tests the MySQL platform.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 231110 $
 */
public class TestMySqlPlatform extends TestPlatformBase
{
    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.TestPlatformBase#getDatabaseName()
     */
    protected String getDatabaseName()
    {
        return MySqlPlatform.DATABASENAME;
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
            "    COL_ARRAY           LONGBLOB,\n"+
            "    COL_BIGINT          BIGINT,\n"+
            "    COL_BINARY          CHAR(254) BINARY,\n"+
            "    COL_BIT             TINYINT(1),\n"+
            "    COL_BLOB            LONGBLOB,\n"+
            "    COL_BOOLEAN         TINYINT(1),\n"+
            "    COL_CHAR            CHAR(15),\n"+
            "    COL_CLOB            LONGTEXT,\n"+
            "    COL_DATALINK        MEDIUMBLOB,\n"+
            "    COL_DATE            DATE,\n"+
            "    COL_DECIMAL         DECIMAL(15,3),\n"+
            "    COL_DECIMAL_NOSCALE DECIMAL(15,0),\n"+
            "    COL_DISTINCT        LONGBLOB,\n"+
            "    COL_DOUBLE          DOUBLE,\n"+
            "    COL_FLOAT           DOUBLE,\n"+
            "    COL_INTEGER         INTEGER,\n"+
            "    COL_JAVA_OBJECT     LONGBLOB,\n"+
            "    COL_LONGVARBINARY   MEDIUMBLOB,\n"+
            "    COL_LONGVARCHAR     MEDIUMTEXT,\n"+
            "    COL_NULL            MEDIUMBLOB,\n"+
            "    COL_NUMERIC         DECIMAL(15,0),\n"+
            "    COL_OTHER           LONGBLOB,\n"+
            "    COL_REAL            FLOAT,\n"+
            "    COL_REF             MEDIUMBLOB,\n"+
            "    COL_SMALLINT        SMALLINT,\n"+
            "    COL_STRUCT          LONGBLOB,\n"+
            "    COL_TIME            TIME,\n"+
            "    COL_TIMESTAMP       DATETIME,\n"+
            "    COL_TINYINT         TINYINT,\n"+
            "    COL_VARBINARY       VARCHAR(15) BINARY,\n"+
            "    COL_VARCHAR         VARCHAR(15)\n"+
            ");\n",
            createTestDatabase());
    }
}
