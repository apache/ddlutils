package org.apache.ddlutils.platform;

/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import java.sql.Types;

import org.apache.ddlutils.PlatformInfo;

/**
 * The platform implementation for the Microsoft SQL Server database.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 231306 $
 */
public class MSSqlPlatform extends PlatformImplBase
{
    /** Database name of this platform. */
    public static final String DATABASENAME     = "MsSql";
    /** The standard SQLServer jdbc driver. */
    public static final String JDBC_DRIVER      = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
    /** The subprotocol used by the standard SQLServer driver. */
    public static final String JDBC_SUBPROTOCOL = "microsoft:sqlserver";

    /**
     * Creates a new platform instance.
     */
    public MSSqlPlatform()
    {
        PlatformInfo info = new PlatformInfo();

        info.setMaxIdentifierLength(128);
        info.setRequiringNullAsDefaultValue(false);
        info.setPrimaryKeyEmbedded(true);
        info.setForeignKeysEmbedded(false);
        info.setIndicesEmbedded(false);
        //info.setCommentPrefix("#");

        info.addNativeTypeMapping(Types.ARRAY,         "IMAGE");
        info.addNativeTypeMapping(Types.BIGINT,        "DECIMAL(19,0)");
        info.addNativeTypeMapping(Types.BLOB,          "IMAGE");
        info.addNativeTypeMapping(Types.CLOB,          "TEXT");
        info.addNativeTypeMapping(Types.DATE,          "DATETIME");
        info.addNativeTypeMapping(Types.DISTINCT,      "IMAGE");
        info.addNativeTypeMapping(Types.DOUBLE,        "FLOAT");
        info.addNativeTypeMapping(Types.INTEGER,       "INT");
        info.addNativeTypeMapping(Types.JAVA_OBJECT,   "IMAGE");
        info.addNativeTypeMapping(Types.LONGVARBINARY, "IMAGE");
        info.addNativeTypeMapping(Types.LONGVARCHAR,   "TEXT");
        info.addNativeTypeMapping(Types.NULL,          "IMAGE");
        info.addNativeTypeMapping(Types.OTHER,         "IMAGE");
        info.addNativeTypeMapping(Types.REF,           "IMAGE");
        info.addNativeTypeMapping(Types.STRUCT,        "IMAGE");
        info.addNativeTypeMapping(Types.TIME,          "DATETIME");
        info.addNativeTypeMapping(Types.TIMESTAMP,     "DATETIME");
        info.addNativeTypeMapping(Types.TINYINT,       "SMALLINT");
        info.addNativeTypeMapping("BOOLEAN",  "BIT");
        info.addNativeTypeMapping("DATALINK", "IMAGE");

        setSqlBuilder(new MSSqlBuilder(info));
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return DATABASENAME;
    }
}
