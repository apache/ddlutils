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
import org.apache.ddlutils.builder.HsqlDbBuilder;

/**
 * The platform implementation for the HsqlDb database.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 231306 $
 */
public class HsqlDbPlatform extends PlatformImplBase
{
    /** Database name of this platform */
    public static final String DATABASENAME     = "HsqlDb";
    /** The standard Hsqldb jdbc driver */
    public static final String JDBC_DRIVER      = "org.hsqldb.jdbcDriver";
    /** The subprotocol used by the standard Hsqldb driver */
    public static final String JDBC_SUBPROTOCOL = "hsqldb";

    /**
     * Creates a new instance of the Hsqldb platform.
     */
    public HsqlDbPlatform()
    {
        PlatformInfo info = new PlatformInfo();

        info.setRequiringNullAsDefaultValue(false);
        info.setPrimaryKeyEmbedded(true);
        info.setForeignKeysEmbedded(false);
        info.setIndicesEmbedded(false);
        info.addNativeTypeMapping(Types.ARRAY,       "LONGVARBINARY");
        info.addNativeTypeMapping(Types.BLOB,        "LONGVARBINARY");
        info.addNativeTypeMapping(Types.CLOB,        "LONGVARCHAR");
        info.addNativeTypeMapping(Types.DISTINCT,    "LONGVARBINARY");
        info.addNativeTypeMapping(Types.FLOAT,       "DOUBLE");
        info.addNativeTypeMapping(Types.JAVA_OBJECT, "OBJECT");
        info.addNativeTypeMapping(Types.NULL,        "LONGVARBINARY");
        info.addNativeTypeMapping(Types.OTHER,       "OTHER");
        info.addNativeTypeMapping(Types.REF,         "LONGVARBINARY");
        info.addNativeTypeMapping(Types.STRUCT,      "LONGVARBINARY");
        info.addNativeTypeMapping("BOOLEAN",  "BIT");
        info.addNativeTypeMapping("DATALINK", "LONGVARBINARY");

        setSqlBuilder(new HsqlDbBuilder(info));
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.Platform#getName()
     */
    public String getName()
    {
        return DATABASENAME;
    }
}
