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
 * The Cloudscape platform implementation.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 231306 $
 */
public class CloudscapePlatform extends PlatformImplBase
{
    /** Database name of this platform. */
    public static final String DATABASENAME = "Cloudscape";
    /** A subprotocol used by the DB2 network driver. */
    public static final String JDBC_SUBPROTOCOL_1 = "db2j:net";
    /** A subprotocol used by the DB2 network driver. */
    public static final String JDBC_SUBPROTOCOL_2 = "cloudscape:net";

    /**
     * Creates a new platform instance.
     */
    public CloudscapePlatform()
    {
        PlatformInfo info = new PlatformInfo();

        info.setRequiringNullAsDefaultValue(false);
        info.setPrimaryKeyEmbedded(true);
        info.setForeignKeysEmbedded(false);
        info.setIndicesEmbedded(false);
        info.setMaxIdentifierLength(128);
        // BINARY and VARBINARY will also be handled by CloudscapeBuilder.getSqlType
        info.addNativeTypeMapping(Types.ARRAY,         "BLOB");
        info.addNativeTypeMapping(Types.BINARY,        "CHAR");
        info.addNativeTypeMapping(Types.BIT,           "SMALLINT");
        info.addNativeTypeMapping(Types.DISTINCT,      "BLOB");
        info.addNativeTypeMapping(Types.DOUBLE,        "DOUBLE PRECISION");
        info.addNativeTypeMapping(Types.FLOAT,         "DOUBLE PRECISION");
        info.addNativeTypeMapping(Types.JAVA_OBJECT,   "BLOB");
        info.addNativeTypeMapping(Types.LONGVARBINARY, "LONG VARCHAR FOR BIT DATA");
        info.addNativeTypeMapping(Types.LONGVARCHAR,   "LONG VARCHAR");
        info.addNativeTypeMapping(Types.OTHER,         "BLOB");
        info.addNativeTypeMapping(Types.NULL,          "LONG VARCHAR FOR BIT DATA");
        info.addNativeTypeMapping(Types.REF,           "LONG VARCHAR FOR BIT DATA");
        info.addNativeTypeMapping(Types.STRUCT,        "BLOB");
        info.addNativeTypeMapping(Types.TINYINT,       "SMALLINT");
        info.addNativeTypeMapping(Types.VARBINARY,     "VARCHAR");
        info.addNativeTypeMapping("BOOLEAN",  "SMALLINT");
        info.addNativeTypeMapping("DATALINK", "LONG VARCHAR FOR BIT DATA");

        setSqlBuilder(new CloudscapeBuilder(info));
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return DATABASENAME;
    }
}
