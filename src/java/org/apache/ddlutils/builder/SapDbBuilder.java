package org.apache.ddlutils.builder;

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

import java.io.IOException;
import java.sql.Types;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;

/**
 * An SQL Builder for SapDB.
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision$
 */
public class SapDbBuilder extends SqlBuilder
{
    /** Database name of this builder */
    public static final String DATABASENAME     = "SapDB";

    // Note that SapDB and MaxDB currently use the same jdbc driver

    /** The standard SapDB/MaxDB jdbc driver */
    public static final String JDBC_DRIVER      = "com.sap.dbtech.jdbc.DriverSapDB";
    /** The subprotocol used by the standard SapDB/MaxDB driver */
    public static final String JDBC_SUBPROTOCOL = "sapdb";

    
    public SapDbBuilder()
    {
        setMaxIdentifierLength(32);
        setRequiringNullAsDefaultValue(false);
        setPrimaryKeyEmbedded(true);
        setForeignKeysEmbedded(false);
        setIndicesEmbedded(false);
        setCommentPrefix("/*");
        setCommentSuffix("*/");

        addNativeTypeMapping(Types.ARRAY,         "LONG BYTE");
        addNativeTypeMapping(Types.BIGINT,        "FIXED(38,0)");
        addNativeTypeMapping(Types.BINARY,        "LONG BYTE");
        addNativeTypeMapping(Types.BIT,           "BOOLEAN");
        addNativeTypeMapping(Types.BLOB,          "LONG BYTE");
        addNativeTypeMapping(Types.CLOB,          "LONG");
        addNativeTypeMapping(Types.DISTINCT,      "LONG BYTE");
        addNativeTypeMapping(Types.DOUBLE,        "DOUBLE PRECISION");
        addNativeTypeMapping(Types.FLOAT,         "DOUBLE PRECISION");
        addNativeTypeMapping(Types.JAVA_OBJECT,   "LONG BYTE");
        addNativeTypeMapping(Types.LONGVARBINARY, "LONG BYTE");
        addNativeTypeMapping(Types.LONGVARCHAR,   "LONG VARCHAR");
        addNativeTypeMapping(Types.NULL,          "LONG BYTE");
        addNativeTypeMapping(Types.NUMERIC,       "DECIMAL");
        addNativeTypeMapping(Types.OTHER,         "LONG BYTE");
        addNativeTypeMapping(Types.REF,           "LONG BYTE");
        addNativeTypeMapping(Types.STRUCT,        "LONG BYTE");
        addNativeTypeMapping(Types.TINYINT,       "SMALLINT");
        addNativeTypeMapping(Types.VARBINARY,     "LONG BYTE");

        // Types.DATALINK is only available since 1.4 so we're using the safe mapping method
        addNativeTypeMapping("DATALINK", "LONG BYTE");
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#getDatabaseName()
     */
    public String getDatabaseName()
    {
        return DATABASENAME;
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#getSqlType(org.apache.ddlutils.model.Column)
     */
    protected String getSqlType(Column column)
    {
        switch (column.getTypeCode())
        {
            // no support for specifying the size for these types:
            case Types.BINARY:
            case Types.VARBINARY:
                return getNativeType(column);
            default:
                return super.getSqlType(column);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#dropTable(Table)
     */
    public void dropTable(Table table) throws IOException
    { 
        print("DROP TABLE ");
        print(getTableName(table));
        print(" CASCADE");
        printEndOfStatement();
    }
}
