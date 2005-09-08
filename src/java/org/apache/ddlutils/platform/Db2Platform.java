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
import org.apache.ddlutils.builder.Db2Builder;

/**
 * The DB2 platform implementation.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 231306 $
 */
public class Db2Platform extends PlatformImplBase
{
    /** Database name of this platform */
    public static final String DATABASENAME     = "DB2";
    /** The standard DB2 jdbc driver */
    public static final String JDBC_DRIVER      = "com.ibm.db2.jcc.DB2Driver";
    /** Older name for the jdbc driver */
    public static final String JDBC_DRIVER_OLD1 = "COM.ibm.db2.jdbc.app.DB2Driver";
    /** Older name for the jdbc driver */
    public static final String JDBC_DRIVER_OLD2 = "COM.ibm.db2os390.sqlj.jdbc.DB2SQLJDriver";
    /** The subprotocol used by the standard DB2 driver */
    public static final String JDBC_SUBPROTOCOL = "db2";
    /** An alternative subprotocol used by the standard DB2 driver on OS/390 */
    public static final String JDBC_SUBPROTOCOL_OS390_1 = "db2os390";
    /** An alternative subprotocol used by the standard DB2 driver on OS/390 */
    public static final String JDBC_SUBPROTOCOL_OS390_2 = "db2os390sqlj";

    /**
     * Creates a new platform instance.
     */
    public Db2Platform()
    {
        PlatformInfo info = new PlatformInfo();

        info.setMaxIdentifierLength(18);
        info.setRequiringNullAsDefaultValue(false);
        info.setPrimaryKeyEmbedded(true);
        info.setForeignKeysEmbedded(false);
        info.setIndicesEmbedded(false);
        // the BINARY types are also handled by Db2Builder.getSqlType(Column)
        info.addNativeTypeMapping(Types.ARRAY,         "BLOB");
        info.addNativeTypeMapping(Types.BINARY,        "CHAR");
        info.addNativeTypeMapping(Types.BIT,           "CHAR FOR BIT DATA");
        info.addNativeTypeMapping(Types.JAVA_OBJECT,   "BLOB");
        info.addNativeTypeMapping(Types.LONGVARBINARY, "LONG VARCHAR FOR BIT DATA");
        info.addNativeTypeMapping(Types.LONGVARCHAR,   "LONG VARCHAR");
        info.addNativeTypeMapping(Types.NULL,          "LONG VARCHAR FOR BIT DATA");
        info.addNativeTypeMapping(Types.OTHER,         "BLOB");
        info.addNativeTypeMapping(Types.STRUCT,        "BLOB");
        info.addNativeTypeMapping(Types.TINYINT,       "SMALLINT");
        info.addNativeTypeMapping(Types.VARBINARY,     "VARCHAR");
        info.addNativeTypeMapping("BOOLEAN", "CHAR FOR BIT DATA");

        setSqlBuilder(new Db2Builder(info));
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.Platform#getDatabaseName()
     */
    public String getDatabaseName()
    {
        return DATABASENAME;
    }
}
