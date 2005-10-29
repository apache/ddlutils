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
 * The Mckoi database platform implementation.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 231306 $
 */
public class MckoiPlatform extends PlatformImplBase
{
    /** Database name of this platform. */
    public static final String DATABASENAME     = "McKoi";
    /** The standard McKoi jdbc driver. */
    public static final String JDBC_DRIVER      = "com.mckoi.JDBCDriver";
    /** The subprotocol used by the standard McKoi driver. */
    public static final String JDBC_SUBPROTOCOL = "mckoi";

    /**
     * Creates a new platform instance.
     */
    public MckoiPlatform()
    {
        PlatformInfo info = new PlatformInfo();

        info.setRequiringNullAsDefaultValue(false);
        info.setPrimaryKeyEmbedded(true);
        info.setForeignKeysEmbedded(false);
        info.setIndicesEmbedded(false);

        info.addNativeTypeMapping(Types.ARRAY,    "BLOB");
        info.addNativeTypeMapping(Types.BIT,      "BOOLEAN");
        info.addNativeTypeMapping(Types.DISTINCT, "BLOB");
        info.addNativeTypeMapping(Types.FLOAT,    "DOUBLE");
        info.addNativeTypeMapping(Types.NULL,     "BLOB");
        info.addNativeTypeMapping(Types.OTHER,    "BLOB");
        info.addNativeTypeMapping(Types.REF,      "BLOB");
        info.addNativeTypeMapping(Types.STRUCT,   "BLOB");
        info.addNativeTypeMapping("DATALINK", "BLOB");

        setSqlBuilder(new MckoiBuilder(info));
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return DATABASENAME;
    }
}
