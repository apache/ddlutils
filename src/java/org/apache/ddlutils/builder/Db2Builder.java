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
 * An SQL Builder for DB2.
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision$
 */
public class Db2Builder extends SqlBuilder
{
    /** Database name of this builder */
    public static final String DATABASENAME = "DB2";

    public Db2Builder()
    {
        setPrimaryKeyEmbedded(false);
        setEmbeddedForeignKeysNamed(true);
        // the BINARY types are also handled by getSqlType(Column)
        addNativeTypeMapping(Types.BINARY,        "CHAR");
        addNativeTypeMapping(Types.BIT,           "DECIMAL(1,0)");
        addNativeTypeMapping(Types.BOOLEAN,       "DECIMAL(1,0)");
        addNativeTypeMapping(Types.LONGVARBINARY, "LONG VARCHAR FOR BIT DATA");
        addNativeTypeMapping(Types.LONGVARCHAR,   "LONG VARCHAR");
        addNativeTypeMapping(Types.TINYINT,       "SMALLINT");
        addNativeTypeMapping(Types.VARBINARY,     "VARCHAR");

        // Types.BOOLEAN is only available since 1.4 so we're using the safe mapping method
        addNativeTypeMapping("BOOLEAN", "DECIMAL(1,0)");
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

    public void dropTable(Table table) throws IOException
    { 
        super.dropTable(table);
        print("DROP SEQUENCE IF EXISTS ");
        print(getTableName(table));
        print(".SequenceName");
        printEndOfStatement();
    }
    
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        print("GENERATED ALWAYS AS IDENTITY");
    }
}
