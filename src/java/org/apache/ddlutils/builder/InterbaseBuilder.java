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

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Table;

/**
 * The SQL Builder for the Interbase database.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 231306 $
 */
public class InterbaseBuilder extends SqlBuilder
{
    /**
     * Creates a new builder instance.
     * 
     * @param info The platform info
     */
    public InterbaseBuilder(PlatformInfo info)
    {
        super(info);
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#writeExternalForeignKeyCreateStmt(org.apache.ddlutils.model.Table, org.apache.ddlutils.model.ForeignKey)
     */
    protected void writeExternalForeignKeyCreateStmt(Database database, Table table, ForeignKey key) throws IOException
    {
        super.writeExternalForeignKeyCreateStmt(database, table, key);
        if (key.getForeignTableName() != null)
        {
            print("COMMIT");
            printEndOfStatement();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#writeExternalForeignKeyDropStmt(org.apache.ddlutils.model.Table, org.apache.ddlutils.model.ForeignKey)
     */
    protected void writeExternalForeignKeyDropStmt(Table table, ForeignKey foreignKey) throws IOException
    {
        super.writeExternalForeignKeyDropStmt(table, foreignKey);
        print("COMMIT");
        printEndOfStatement();
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#createTable(org.apache.ddlutils.model.Table)
     */
    public void createTable(Database database, Table table) throws IOException
    {
        super.createTable(database, table);
        print("COMMIT");
        printEndOfStatement();

        // creating generator and trigger for auto-increment
        Column[] columns = table.getAutoIncrementColumn();

        for (int idx = 0; idx < columns.length; idx++)
        {
            print("CREATE GENERATOR ");
            printIdentifier(getConstraintName("gen", table, columns[idx].getName(), null));
            printEndOfStatement();
            print("COMMIT");
            printEndOfStatement();
            print("SET TERM !!");
            printEndOfStatement();
            print("CREATE TRIGGER ");
            printIdentifier(getConstraintName("trg", table, columns[idx].getName(), null));
            print(" FOR ");
            printlnIdentifier(getTableName(table));
            println("ACTIVE BEFORE INSERT POSITION 0 AS");
            println("BEGIN");
            print("IF (NEW.");
            printIdentifier(getColumnName(columns[idx]));
            println(" IS NULL) THEN");
            print("NEW.");
            printIdentifier(getColumnName(columns[idx]));
            print(" = GEN_ID(");
            printIdentifier(getConstraintName("gen", table, columns[idx].getName(), null));
            println(", 1);");
            println("END !!");
            print("SET TERM ");
            print(getPlatformInfo().getSqlCommandDelimiter());
            println(" !!");
            print("COMMIT");
            printEndOfStatement();
        }
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
                return super.getSqlType(column) + " CHARACTER SET OCTETS";
            default:
                return super.getSqlType(column);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#dropTable(org.apache.ddlutils.model.Table)
     */
    public void dropTable(Table table) throws IOException
    {
        // dropping generators for auto-increment
        Column[] columns = table.getAutoIncrementColumn();

        for (int idx = 0; idx < columns.length; idx++)
        {
            print("DELETE FROM RDB$GENERATOR WHERE RDB$GENERATOR_NAME = ");
            printIdentifier(getConstraintName("gen", table, columns[idx].getName(), null));
            printEndOfStatement();
            print("COMMIT");
            printEndOfStatement();
        }
        super.dropTable(table);
        print("COMMIT");
        printEndOfStatement();
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#printAutoIncrementColumn(Table,Column)
     */
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        // we're using a generator
    }
}
