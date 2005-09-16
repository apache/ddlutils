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

import java.sql.Types;

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.builder.AxionBuilder;

/**
 * The platform for the Axion database.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 231306 $
 */
public class AxionPlatform extends PlatformImplBase
{
    /** Database name of this platform */
    public static final String DATABASENAME     = "Axion";
    /** The axion jdbc driver */
    public static final String JDBC_DRIVER      = "org.axiondb.jdbc.AxionDriver";
    /** The subprotocol used by the axion driver */
    public static final String JDBC_SUBPROTOCOL = "axiondb";

    /**
     * Creates a new axion platform instance.
     */
    public AxionPlatform()
    {
        PlatformInfo info = new PlatformInfo();

        info.setRequiringNullAsDefaultValue(false);
        info.setPrimaryKeyEmbedded(true);
        info.setForeignKeysEmbedded(false);
        info.setIndicesEmbedded(true);
        info.addNativeTypeMapping(Types.ARRAY,         "BLOB");
        info.addNativeTypeMapping(Types.BINARY,        "VARBINARY");
        info.addNativeTypeMapping(Types.BIT,           "BOOLEAN");
        info.addNativeTypeMapping(Types.DECIMAL,       "NUMBER");
        info.addNativeTypeMapping(Types.DISTINCT,      "VARBINARY");
        info.addNativeTypeMapping(Types.DOUBLE,        "FLOAT");
        info.addNativeTypeMapping(Types.LONGVARBINARY, "VARBINARY");
        info.addNativeTypeMapping(Types.LONGVARCHAR,   "VARCHAR");
        info.addNativeTypeMapping(Types.NULL,          "VARBINARY");
        info.addNativeTypeMapping(Types.NUMERIC,       "NUMBER");
        info.addNativeTypeMapping(Types.OTHER,         "BLOB");
        info.addNativeTypeMapping(Types.REAL,          "FLOAT");
        info.addNativeTypeMapping(Types.REF,           "VARBINARY");
        info.addNativeTypeMapping(Types.SMALLINT,      "SHORT");
        info.addNativeTypeMapping(Types.STRUCT,        "VARBINARY");
        info.addNativeTypeMapping(Types.TINYINT,       "SHORT");
        info.addNativeTypeMapping("DATALINK", "VARBINARY");

        setSqlBuilder(new AxionBuilder(info));
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.Platform#getName()
     */
    public String getName()
    {
        return DATABASENAME;
    }
}
