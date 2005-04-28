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
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Table;

/**
 * An SQL Builder for the Firebird database.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 */
public class FirebirdBuilder extends SqlBuilder
{
    public FirebirdBuilder()
    {
        setPrimaryKeyEmbedded(true);
        setForeignKeysEmbedded(false);
        setCommentPrefix("/*");
        setCommentSuffix("*/");
        addNativeTypeMapping(Types.BIGINT,        "DECIMAL(18,0)");
        addNativeTypeMapping(Types.BINARY,        "BLOB");
        addNativeTypeMapping(Types.BIT,           "DECIMAL(1,0)");
        addNativeTypeMapping(Types.BOOLEAN,       "DECIMAL(1,0)");
        addNativeTypeMapping(Types.CLOB,          "BLOB SUB_TYPE TEXT");
        addNativeTypeMapping(Types.DOUBLE,        "DOUBLE PRECISION");
        addNativeTypeMapping(Types.LONGVARBINARY, "BLOB");
        addNativeTypeMapping(Types.LONGVARCHAR,   "BLOB SUB_TYPE TEXT");
        addNativeTypeMapping(Types.REAL,          "FLOAT");
        addNativeTypeMapping(Types.TINYINT,       "SMALLINT");
        addNativeTypeMapping(Types.VARBINARY,     "BLOB");
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#getDatabaseName()
     */
    public String getDatabaseName()
    {
        return "Firebird";
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#dropDatabase(org.apache.ddlutils.model.Database)
     */
    public void dropDatabase(Database database) throws IOException
    {
        super.dropDatabase(database);
        print("COMMIT");
        printEndOfStatement();
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#createTables(org.apache.ddlutils.model.Database)
     */
    public void createTables(Database database) throws IOException
    {
        super.createTables(database);
        print("COMMIT");
        printEndOfStatement();
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#writeExternalForeignKeyCreateStmt(org.apache.ddlutils.model.Table, org.apache.ddlutils.model.ForeignKey)
     */
    protected void writeExternalForeignKeyCreateStmt(Database database, Table table, ForeignKey key) throws IOException
    {
        super.writeExternalForeignKeyCreateStmt(database, table, key);
        if (key.getForeignTable() != null)
        {
            print("COMMIT");
            printEndOfStatement();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#createTable(org.apache.ddlutils.model.Table)
     */
    public void createTable(Database database, Table table) throws IOException
    {
        super.createTable(database, table);

        // creating generator and trigger for auto-increment
        Column column = table.getAutoIncrementColumn();

        if (column != null)
        {
            print("CREATE GENERATOR ");
            print(getConstraintName("gen", table, column.getName(), null));
            printEndOfStatement();
            print("CREATE TRIGGER trg_");
            print(getConstraintName("trg", table, column.getName(), null));
            print(" FOR ");
            println(getTableName(table));
            println("ACTIVE BEFORE INSERT POSITION 0");
            println("AS");
            println("BEGIN");
            print("IF (NEW.");
            print(getColumnName(column));
            println(" IS NULL) THEN");
            print("NEW.");
            print(getColumnName(column));
            print(" = GEN_ID(");
            print(getConstraintName("gen", table, column.getName(), null));
            println(", 1);");
            print("END");
            printEndOfStatement();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#dropTable(org.apache.ddlutils.model.Table)
     */
    public void dropTable(Table table) throws IOException
    {
        // dropping generator and trigger for auto-increment
        Column column = table.getAutoIncrementColumn();

        if (column != null)
        {
            print("DROP TRIGGER trg_");
            print(getConstraintName("trg", table, column.getName(), null));
            printEndOfStatement();
            print("DROP GENERATOR gen_");
            print(getConstraintName("gen", table, column.getName(), null));
            printEndOfStatement();
        }
        super.dropTable(table);
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#printAutoIncrementColumn(Table,Column)
     */
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        // we're using a generator
    }
}
