package org.apache.ddlutils.platform.interbase;

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
import org.apache.ddlutils.platform.PlatformImplBase;

/**
 * The platform implementation for the Interbase database.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 231306 $
 */
public class InterbasePlatform extends PlatformImplBase
{
    /** Database name of this platform. */
    public static final String DATABASENAME     = "Interbase";
    /** The interbase jdbc driver. */
    public static final String JDBC_DRIVER      = "interbase.interclient.Driver";
    /** The subprotocol used by the interbase driver. */
    public static final String JDBC_SUBPROTOCOL = "interbase";

    /**
     * Creates a new platform instance.
     */
    public InterbasePlatform()
    {
        PlatformInfo info = new PlatformInfo();

        info.setMaxIdentifierLength(31);
        info.setRequiringNullAsDefaultValue(false);
        info.setPrimaryKeyEmbedded(true);
        info.setForeignKeysEmbedded(false);
        info.setIndicesEmbedded(false);
        info.setCommentPrefix("/*");
        info.setCommentSuffix("*/");

        // BINARY and VARBINARY are also handled by the InterbaseBuilder.getSqlType method
        info.addNativeTypeMapping(Types.ARRAY,         "BLOB");
        info.addNativeTypeMapping(Types.BIGINT,        "DECIMAL(38,0)");
        info.addNativeTypeMapping(Types.BINARY,        "CHAR {0} CHARACTER SET OCTETS");
        info.addNativeTypeMapping(Types.BIT,           "DECIMAL(1,0)");
        info.addNativeTypeMapping(Types.CLOB,          "BLOB SUB_TYPE TEXT");
        info.addNativeTypeMapping(Types.DISTINCT,      "BLOB");
        info.addNativeTypeMapping(Types.DOUBLE,        "DOUBLE PRECISION");
        info.addNativeTypeMapping(Types.FLOAT,         "DOUBLE PRECISION");
        info.addNativeTypeMapping(Types.JAVA_OBJECT,   "BLOB");
        info.addNativeTypeMapping(Types.LONGVARBINARY, "BLOB");
        info.addNativeTypeMapping(Types.LONGVARCHAR,   "BLOB SUB_TYPE TEXT");
        info.addNativeTypeMapping(Types.NULL,          "BLOB");
        info.addNativeTypeMapping(Types.OTHER,         "BLOB");
        info.addNativeTypeMapping(Types.REAL,          "FLOAT");
        info.addNativeTypeMapping(Types.TINYINT,       "SMALLINT");
        info.addNativeTypeMapping(Types.REF,           "BLOB");
        info.addNativeTypeMapping(Types.STRUCT,        "BLOB");
        info.addNativeTypeMapping(Types.VARBINARY,     "VARCHAR {0} CHARACTER SET OCTETS");
        info.addNativeTypeMapping("BOOLEAN",  "DECIMAL(1,0)");
        info.addNativeTypeMapping("DATALINK", "BLOB");

        setSqlBuilder(new InterbaseBuilder(info));
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return DATABASENAME;
    }
}
