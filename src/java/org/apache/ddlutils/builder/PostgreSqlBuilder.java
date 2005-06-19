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
import java.util.Iterator;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

/**
 * An SQL Builder for PostgresSql, based upon version 7.2.
 * 
 * @author <a href="mailto:john@zenplex.com">John Thorhauer</a>
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision$
 */
public class PostgreSqlBuilder extends SqlBuilder
{
    public PostgreSqlBuilder() 
    {
        // this is the default length though it might be changed when building PostgreSQL
        // in file src/include/postgres_ext.h
        setMaxIdentifierLength(31);
        setRequiringNullAsDefaultValue(false);
        setPrimaryKeyEmbedded(true);
        setForeignKeysEmbedded(false);
        setIndicesEmbedded(false);
        addNativeTypeMapping(Types.ARRAY,         "BYTEA");
        addNativeTypeMapping(Types.BINARY,        "BYTEA");
        addNativeTypeMapping(Types.BLOB,          "BYTEA");
        addNativeTypeMapping(Types.CLOB,          "TEXT");
        addNativeTypeMapping(Types.DECIMAL,       "NUMERIC");
        addNativeTypeMapping(Types.DISTINCT,      "BYTEA");
        addNativeTypeMapping(Types.DOUBLE,        "DOUBLE PRECISION");
        addNativeTypeMapping(Types.FLOAT,         "DOUBLE PRECISION");
        addNativeTypeMapping(Types.JAVA_OBJECT,   "BYTEA");
        addNativeTypeMapping(Types.LONGVARBINARY, "BYTEA");
        addNativeTypeMapping(Types.LONGVARCHAR,   "TEXT");
        addNativeTypeMapping(Types.NULL,          "BYTEA");
        addNativeTypeMapping(Types.OTHER,         "BYTEA");
        addNativeTypeMapping(Types.REF,           "BYTEA");
        addNativeTypeMapping(Types.STRUCT,        "BYTEA");
        addNativeTypeMapping(Types.TINYINT,       "SMALLINT");
        addNativeTypeMapping(Types.VARBINARY,     "BYTEA");

        addNativeTypeMapping("DATALINK", "BYTEA");
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#getDatabaseName()
     */
    public String getDatabaseName()
    {
        return "PostgreSql";
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

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#createTable(org.apache.ddlutils.model.Database, org.apache.ddlutils.model.Table)
     */
    public void createTable(Database database, Table table) throws IOException
    {
        for (Iterator it = table.getColumns().iterator(); it.hasNext();)
        {
            Column column = (Column)it.next();

            if (column.isAutoIncrement())
            {
                createAutoIncrementSequence(table, column);
            }
        }
        super.createTable(database, table);
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

    /**
     * Creates the auto-increment sequence that is then used in the column.
     *  
     * @param table  The table
     * @param column The column
     */
    private void createAutoIncrementSequence(Table table, Column column) throws IOException
    {
        print("CREATE SEQUENCE ");
        print(getConstraintName(null, table, column.getName(), "seq"));
        printEndOfStatement();
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#writeColumnAutoIncrementStmt(org.apache.ddlutils.model.Table, org.apache.ddlutils.model.Column)
     */
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        print("UNIQUE DEFAULT nextval('");
        print(getConstraintName(null, table, column.getName(), "seq"));
        print("')");
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#getSelectLastInsertId(org.apache.ddlutils.model.Table)
     */
    public String getSelectLastInsertId(Table table)
    {
        Column autoIncrColumn = table.getAutoIncrementColumn();

        if (autoIncrColumn == null)
        {
            return null;
        }
        else
        {
            return "SELECT CURRVAL('" + getConstraintName(null, table, autoIncrColumn.getName(), "seq") + "') AS " + autoIncrColumn.getName();
        }
    }
}
