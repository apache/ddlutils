package org.apache.commons.sql.builder;

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

import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.Table;

/**
 * An SQL Builder for PostgresSql
 * 
 * @author <a href="mailto:john@zenplex.com">John Thorhauer</a>
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 1.11 $
 */
public class PostgreSqlBuilder extends SqlBuilder
{
    public PostgreSqlBuilder() 
    {
        addNativeTypeMapping(Types.BINARY,        "BYTEA");
        addNativeTypeMapping(Types.BLOB,          "BYTEA");
        addNativeTypeMapping(Types.CLOB,          "TEXT");
        addNativeTypeMapping(Types.DOUBLE,        "DOUBLE PRECISION");
        addNativeTypeMapping(Types.FLOAT,         "REAL");
        addNativeTypeMapping(Types.LONGVARBINARY, "BYTEA");
        addNativeTypeMapping(Types.LONGVARCHAR,   "TEXT");
        addNativeTypeMapping(Types.TINYINT,       "SMALLINT");
        addNativeTypeMapping(Types.VARBINARY,     "BYTEA");
    }

    /* (non-Javadoc)
     * @see org.apache.commons.sql.builder.SqlBuilder#getDatabaseName()
     */
    public String getDatabaseName()
    {
        return "PostgreSql";
    }

    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        print(" SERIAL ");
    }

    /** 
     * Outputs the DDL to add a column to a table.
     */
    public void writeColumn(Table table, Column column) throws IOException
    {
        print(column.getName());
        print(" ");
        if (column.isAutoIncrement())
        {
            writeColumnAutoIncrementStmt(table, column);
        }
        else
        {
            print(getSqlType(column));
            print(" ");

            if (column.getDefaultValue() != null)
            {
                print("DEFAULT '");
                print(column.getDefaultValue());
                print("' ");
            }
            if (column.isRequired())
            {
                writeColumnNotNullableStmt();
            }
            else
            {
                writeColumnNullableStmt();
            }
            print(" ");
        }
    }
}
