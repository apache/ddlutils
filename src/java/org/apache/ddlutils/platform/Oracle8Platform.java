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
 * The platform for Oracle 8.
 *
 * @author James Strachan
 * @author Thomas Dudziak
 * @version $Revision: 231306 $
 */
public class Oracle8Platform extends PlatformImplBase
{
    /** Database name of this platform. */
    public static final String DATABASENAME              = "Oracle";
    /** The standard Oracle jdbc driver. */
    public static final String JDBC_DRIVER               = "oracle.jdbc.driver.OracleDriver";
    /** The old Oracle jdbc driver. */
    public static final String JDBC_DRIVER_OLD           = "oracle.jdbc.dnlddriver.OracleDriver";
    /** The thin subprotocol used by the standard Oracle driver. */
    public static final String JDBC_SUBPROTOCOL_THIN     = "oracle:thin";
    /** The thin subprotocol used by the standard Oracle driver. */
    public static final String JDBC_SUBPROTOCOL_OCI8     = "oracle:oci8";
    /** The thin subprotocol used by the standard Oracle driver. */
    public static final String JDBC_SUBPROTOCOL_THIN_OLD = "oracle:dnldthin";

    /**
     * Creates a new platform instance.
     */
    public Oracle8Platform()
    {
        PlatformInfo info = new PlatformInfo();

        info.setMaxIdentifierLength(30);
        info.setRequiringNullAsDefaultValue(false);
        info.setPrimaryKeyEmbedded(true);
        info.setForeignKeysEmbedded(false);
        info.setIndicesEmbedded(false);

        info.addNativeTypeMapping(Types.ARRAY,         "BLOB");
        info.addNativeTypeMapping(Types.BIGINT,        "NUMBER(38,0)");
        info.addNativeTypeMapping(Types.BINARY,        "RAW");
        info.addNativeTypeMapping(Types.BIT,           "NUMBER(1,0)");
        info.addNativeTypeMapping(Types.DECIMAL,       "NUMBER");
        info.addNativeTypeMapping(Types.DISTINCT,      "BLOB");
        info.addNativeTypeMapping(Types.DOUBLE,        "NUMBER(38)");
        info.addNativeTypeMapping(Types.FLOAT,         "NUMBER(38)");
        info.addNativeTypeMapping(Types.INTEGER,       "NUMBER(20,0)");
        info.addNativeTypeMapping(Types.JAVA_OBJECT,   "BLOB");
        info.addNativeTypeMapping(Types.LONGVARBINARY, "BLOB");
        info.addNativeTypeMapping(Types.LONGVARCHAR,   "CLOB");
        info.addNativeTypeMapping(Types.NULL,          "BLOB");
        info.addNativeTypeMapping(Types.NUMERIC,       "NUMBER");
        info.addNativeTypeMapping(Types.OTHER,         "BLOB");
        info.addNativeTypeMapping(Types.REAL,          "NUMBER(18)");
        info.addNativeTypeMapping(Types.REF,           "BLOB");
        info.addNativeTypeMapping(Types.SMALLINT,      "NUMBER(5,0)");
        info.addNativeTypeMapping(Types.STRUCT,        "BLOB");
        info.addNativeTypeMapping(Types.TIME,          "DATE");
        info.addNativeTypeMapping(Types.TIMESTAMP,     "DATE");
        info.addNativeTypeMapping(Types.TINYINT,       "NUMBER(3,0)");
        info.addNativeTypeMapping(Types.VARBINARY,     "RAW");
        info.addNativeTypeMapping(Types.VARCHAR,       "VARCHAR2");
        info.addNativeTypeMapping("BOOLEAN",  "NUMBER(1,0)");
        info.addNativeTypeMapping("DATALINK", "BLOB");

        setSqlBuilder(new OracleBuilder(info));
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return DATABASENAME;
    }
}
