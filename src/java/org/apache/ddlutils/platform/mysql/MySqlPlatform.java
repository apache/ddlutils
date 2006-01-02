package org.apache.ddlutils.platform.mysql;

/*
 * Copyright 1999-2006 The Apache Software Foundation.
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
 * The platform implementation for MySQL.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 231306 $
 */
public class MySqlPlatform extends PlatformImplBase
{
    /** Database name of this platform. */
    public static final String DATABASENAME     = "MySQL";
    /** The standard MySQL jdbc driver. */
    public static final String JDBC_DRIVER      = "com.mysql.jdbc.Driver";
    /** The old MySQL jdbc driver. */
    public static final String JDBC_DRIVER_OLD  = "org.gjt.mm.mysql.Driver";
    /** The subprotocol used by the standard MySQL driver. */
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
        // Double quotes are only allowed for delimiting identifiers if the server SQL mode includes ANSI_QUOTES 
        info.setDelimiterToken("`");
        // the BINARY types are also handled by MySqlBuilder.getSqlType(Column)
        info.addNativeTypeMapping(Types.ARRAY,         "LONGBLOB");
        info.addNativeTypeMapping(Types.BINARY,        "CHAR {0} BINARY");
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
        // Since TIMESTAMP is not a stable datatype yet, and does not support a higher precision
        // that DATETIME (year to seconds) as of MySQL 5, we map the JDBC type here to DATETIME
        // TODO: Make this configurable
        info.addNativeTypeMapping(Types.TIMESTAMP,     "DATETIME");
        info.addNativeTypeMapping(Types.VARBINARY,     "VARCHAR {0} BINARY");
        info.addNativeTypeMapping("BOOLEAN",  "TINYINT(1)");
        info.addNativeTypeMapping("DATALINK", "MEDIUMBLOB");

        info.addDefaultSize(Types.BINARY,    254);
        info.addDefaultSize(Types.VARBINARY, 254);
        
        setSqlBuilder(new MySqlBuilder(info));
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return DATABASENAME;
    }
}
