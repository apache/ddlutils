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
import org.apache.ddlutils.builder.MySqlBuilder;

/**
 * The platform implementation for MySQL
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 231306 $
 */
public class MySqlPlatform extends PlatformImplBase
{
    /** Database name of this platform */
    public static final String DATABASENAME     = "MySQL";
    /** The standard MySQL jdbc driver */
    public static final String JDBC_DRIVER      = "com.mysql.jdbc.Driver";
    /** The old MySQL jdbc driver */
    public static final String JDBC_DRIVER_OLD  = "org.gjt.mm.mysql.Driver";
    /** The subprotocol used by the standard MySQL driver */
    public static final String JDBC_SUBPROTOCOL = "mysql";

    /**
     * Creates a new platform instance.
     */
    public MySqlPlatform()
    {
        PlatformInfo info = new PlatformInfo();

        info.setMaxIdentifierLength(64);
        info.setRequiringNullAsDefaultValue(false);
        info.setPrimaryKeyEmbedded(true);
        info.setForeignKeysEmbedded(false);
        info.setIndicesEmbedded(false);
        info.setCommentPrefix("#");
        // the BINARY types are also handled by MySqlBuilder.getSqlType(Column)
        info.addNativeTypeMapping(Types.ARRAY,         "LONGBLOB");
        info.addNativeTypeMapping(Types.BINARY,        "CHAR");
        info.addNativeTypeMapping(Types.BIT,           "TINYINT(1)");
        info.addNativeTypeMapping(Types.BLOB,          "LONGBLOB");
        info.addNativeTypeMapping(Types.CLOB,          "LONGTEXT");
        info.addNativeTypeMapping(Types.DISTINCT,      "LONGBLOB");
        info.addNativeTypeMapping(Types.FLOAT,         "DOUBLE");
        info.addNativeTypeMapping(Types.JAVA_OBJECT,   "LONGBLOB");
        info.addNativeTypeMapping(Types.LONGVARBINARY, "MEDIUMBLOB");
        info.addNativeTypeMapping(Types.LONGVARCHAR,   "MEDIUMTEXT");
        info.addNativeTypeMapping(Types.NULL,          "MEDIUMBLOB");
        info.addNativeTypeMapping(Types.NUMERIC,       "DECIMAL");
        info.addNativeTypeMapping(Types.OTHER,         "LONGBLOB");
        info.addNativeTypeMapping(Types.REAL,          "FLOAT");
        info.addNativeTypeMapping(Types.REF,           "MEDIUMBLOB");
        info.addNativeTypeMapping(Types.STRUCT,        "LONGBLOB");
        info.addNativeTypeMapping(Types.VARBINARY,     "VARCHAR");
        info.addNativeTypeMapping("BOOLEAN",  "TINYINT(1)");
        info.addNativeTypeMapping("DATALINK", "MEDIUMBLOB");

        setSqlBuilder(new MySqlBuilder(info));
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.Platform#getDatabaseName()
     */
    public String getDatabaseName()
    {
        return DATABASENAME;
    }
}
