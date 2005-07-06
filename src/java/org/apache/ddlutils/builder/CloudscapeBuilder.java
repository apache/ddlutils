package org.apache.ddlutils.builder;

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

import java.io.IOException;
import java.sql.Types;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;

/**
 * An SQL Builder for Cloudscape.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 */
public class CloudscapeBuilder extends SqlBuilder
{
    /** Database name of this builder */
    public static final String DATABASENAME = "Cloudscape";

    public CloudscapeBuilder()
    {
        setRequiringNullAsDefaultValue(false);
        setPrimaryKeyEmbedded(true);
        setForeignKeysEmbedded(false);
        setIndicesEmbedded(false);
        setMaxIdentifierLength(128);
        // binary and varbinary are also handled by getSqlType
        addNativeTypeMapping(Types.ARRAY,         "BLOB");
        addNativeTypeMapping(Types.BINARY,        "CHAR");
        addNativeTypeMapping(Types.BIT,           "CHAR FOR BIT DATA");
        addNativeTypeMapping(Types.DISTINCT,      "BLOB");
        addNativeTypeMapping(Types.DOUBLE,        "DOUBLE PRECISION");
        addNativeTypeMapping(Types.FLOAT,         "DOUBLE PRECISION");
        addNativeTypeMapping(Types.JAVA_OBJECT,   "BLOB");
        addNativeTypeMapping(Types.LONGVARBINARY, "LONG VARCHAR FOR BIT DATA");
        addNativeTypeMapping(Types.LONGVARCHAR,   "LONG VARCHAR");
        addNativeTypeMapping(Types.OTHER,         "BLOB");
        addNativeTypeMapping(Types.NULL,          "LONG VARCHAR FOR BIT DATA");
        addNativeTypeMapping(Types.REF,           "LONG VARCHAR FOR BIT DATA");
        addNativeTypeMapping(Types.STRUCT,        "BLOB");
        addNativeTypeMapping(Types.TINYINT,       "SMALLINT");
        addNativeTypeMapping(Types.VARBINARY,     "VARCHAR");

        addNativeTypeMapping("BOOLEAN",  "CHAR FOR BIT DATA");
        addNativeTypeMapping("DATALINK", "LONG VARCHAR FOR BIT DATA");
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
            case Types.BINARY:
            case Types.VARBINARY:
                StringBuffer sqlType = new StringBuffer();
                
                sqlType.append(getNativeType(column));
                sqlType.append(" (");
                if (column.getSize() != null)
                {
                    sqlType.append(column.getSize());
                }
                else
                {
                    sqlType.append("254");
                }
                sqlType.append(") FOR BIT DATA");
                return sqlType.toString();
            default:
                return super.getSqlType(column);
        }
    }
    
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        print("GENERATED ALWAYS AS IDENTITY");
    }
}
