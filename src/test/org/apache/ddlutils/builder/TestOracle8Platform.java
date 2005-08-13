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
import org.apache.ddlutils.platform.Oracle8Platform;

/**
 * Tests the Oracle platform.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 231110 $
 */
public class TestOracle8Platform extends TestPlatformBase
{
    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.TestBuilderBase#getDatabaseName()
     */
    protected String getDatabaseName()
    {
        return Oracle8Platform.DATABASENAME;
    }

    /**
     * Tests the column types.
     */
    public void testColumnTypes() throws Exception
    {
        assertEqualsIgnoringWhitespaces(
            "DROP TABLE coltype CASCADE CONSTRAINTS;\n"+
            "CREATE TABLE coltype\n"+
            "(\n"+
            "    COL_ARRAY           BLOB,\n"+
            "    COL_BIGINT          NUMBER(38,0),\n"+
            "    COL_BINARY          RAW(254),\n"+
            "    COL_BIT             NUMBER(1,0),\n"+
            "    COL_BLOB            BLOB,\n"+
            "    COL_BOOLEAN         NUMBER(1,0),\n"+
            "    COL_CHAR            CHAR(15),\n"+
            "    COL_CLOB            CLOB,\n"+
            "    COL_DATALINK        BLOB,\n"+
            "    COL_DATE            DATE,\n"+
            "    COL_DECIMAL         NUMBER(15,3),\n"+
            "    COL_DECIMAL_NOSCALE NUMBER(15,0),\n"+
            "    COL_DISTINCT        BLOB,\n"+
            "    COL_DOUBLE          NUMBER(38),\n"+
            "    COL_FLOAT           NUMBER(38),\n"+
            "    COL_INTEGER         NUMBER(20,0),\n"+
            "    COL_JAVA_OBJECT     BLOB,\n"+
            "    COL_LONGVARBINARY   BLOB,\n"+
            "    COL_LONGVARCHAR     CLOB,\n"+
            "    COL_NULL            BLOB,\n"+
            "    COL_NUMERIC         NUMBER(15,0),\n"+
            "    COL_OTHER           BLOB,\n"+
            "    COL_REAL            NUMBER(18),\n"+
            "    COL_REF             BLOB,\n"+
            "    COL_SMALLINT        NUMBER(5,0),\n"+
            "    COL_STRUCT          BLOB,\n"+
            "    COL_TIME            DATE,\n"+
            "    COL_TIMESTAMP       DATE,\n"+
            "    COL_TINYINT         NUMBER(3,0),\n"+
            "    COL_VARBINARY       RAW(15),\n"+
            "    COL_VARCHAR         VARCHAR2(15)\n"+
            ");\n",
            createTestDatabase());
    }
}
