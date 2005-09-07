package org.apache.ddlutils.platform;

/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
import org.apache.ddlutils.builder.SapDbBuilder;

/**
 * The SapDB platform implementation.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 231306 $
 */
public class SapDbPlatform extends PlatformImplBase
{
    /** Database name of this platform */
    public static final String DATABASENAME     = "SapDB";
    /** The standard SapDB/MaxDB jdbc driver */
    public static final String JDBC_DRIVER      = "com.sap.dbtech.jdbc.DriverSapDB";
    /** The subprotocol used by the standard SapDB/MaxDB driver */
    public static final String JDBC_SUBPROTOCOL = "sapdb";

    /**
     * Creates a new platform instance.
     */
    public SapDbPlatform()
    {
        PlatformInfo info = new PlatformInfo();

        info.setMaxIdentifierLength(32);
        info.setRequiringNullAsDefaultValue(false);
        info.setPrimaryKeyEmbedded(true);
        info.setForeignKeysEmbedded(false);
        info.setIndicesEmbedded(false);
        info.setCommentPrefix("/*");
        info.setCommentSuffix("*/");

        info.addNativeTypeMapping(Types.ARRAY,         "LONG BYTE");
        info.addNativeTypeMapping(Types.BIGINT,        "FIXED(38,0)");
        info.addNativeTypeMapping(Types.BINARY,        "LONG BYTE");
        info.addNativeTypeMapping(Types.BIT,           "BOOLEAN");
        info.addNativeTypeMapping(Types.BLOB,          "LONG BYTE");
        info.addNativeTypeMapping(Types.CLOB,          "LONG");
        info.addNativeTypeMapping(Types.DISTINCT,      "LONG BYTE");
        info.addNativeTypeMapping(Types.DOUBLE,        "DOUBLE PRECISION");
        info.addNativeTypeMapping(Types.FLOAT,         "DOUBLE PRECISION");
        info.addNativeTypeMapping(Types.JAVA_OBJECT,   "LONG BYTE");
        info.addNativeTypeMapping(Types.LONGVARBINARY, "LONG BYTE");
        info.addNativeTypeMapping(Types.LONGVARCHAR,   "LONG VARCHAR");
        info.addNativeTypeMapping(Types.NULL,          "LONG BYTE");
        info.addNativeTypeMapping(Types.NUMERIC,       "DECIMAL");
        info.addNativeTypeMapping(Types.OTHER,         "LONG BYTE");
        info.addNativeTypeMapping(Types.REF,           "LONG BYTE");
        info.addNativeTypeMapping(Types.STRUCT,        "LONG BYTE");
        info.addNativeTypeMapping(Types.TINYINT,       "SMALLINT");
        info.addNativeTypeMapping(Types.VARBINARY,     "LONG BYTE");
        info.addNativeTypeMapping("DATALINK", "LONG BYTE");

        // no support for specifying the size for these types
        info.setHasSize(Types.BINARY, false);
        info.setHasSize(Types.VARBINARY, false);

        setSqlBuilder(new SapDbBuilder(info));
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.Platform#getDatabaseName()
     */
    public String getDatabaseName()
    {
        return DATABASENAME;
    }
}
