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
import org.apache.ddlutils.builder.SybaseBuilder;

/**
 * The platform implementation for Sybase.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 231306 $
 */
public class SybasePlatform extends PlatformImplBase
{
    /** Database name of this platform. */
    public static final String DATABASENAME     = "Sybase";
    /** The standard Sybase jdbc driver. */
    public static final String JDBC_DRIVER      = "com.sybase.jdbc2.jdbc.SybDriver";
    /** The old Sybase jdbc driver. */
    public static final String JDBC_DRIVER_OLD  = "com.sybase.jdbc.SybDriver";
    /** The subprotocol used by the standard Sybase driver. */
    public static final String JDBC_SUBPROTOCOL = "sybase:Tds";

    /**
     * Creates a new platform instance.
     */
    public SybasePlatform()
    {
        PlatformInfo info = new PlatformInfo();

        info.setMaxIdentifierLength(30);
        info.setRequiringNullAsDefaultValue(false);
        info.setPrimaryKeyEmbedded(true);
        info.setForeignKeysEmbedded(false);
        info.setIndicesEmbedded(false);
        info.setCommentPrefix("/*");
        info.setCommentSuffix("*/");

        info.addNativeTypeMapping(Types.ARRAY,         "IMAGE");
        info.addNativeTypeMapping(Types.BIGINT,        "DECIMAL(19,0)");
        info.addNativeTypeMapping(Types.BLOB,          "IMAGE");
        info.addNativeTypeMapping(Types.CLOB,          "TEXT");
        info.addNativeTypeMapping(Types.DATE,          "DATETIME");
        info.addNativeTypeMapping(Types.DISTINCT,      "IMAGE");
        info.addNativeTypeMapping(Types.DOUBLE,        "DOUBLE PRECISION");
        info.addNativeTypeMapping(Types.FLOAT,         "DOUBLE PRECISION");
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

        setSqlBuilder(new SybaseBuilder(info));
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return DATABASENAME;
    }
}
