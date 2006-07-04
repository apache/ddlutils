package org.apache.ddlutils.platform.interbase;

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

import java.io.IOException;
import java.sql.Types;
import java.util.Map;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.SqlBuilder;
import org.apache.ddlutils.util.Jdbc3Utils;

/**
 * The SQL Builder for the Interbase database.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 231306 $
 */
public class InterbaseBuilder extends SqlBuilder
{
    /**
     * Creates a new builder instance.
     * 
     * @param platform The plaftform this builder belongs to
     */
    public InterbaseBuilder(Platform platform)
    {
        super(platform);
        addEscapedCharSequence("'", "''");
    }

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
     */
    protected void writeExternalForeignKeyDropStmt(Table table, ForeignKey foreignKey) throws IOException
    {
        super.writeExternalForeignKeyDropStmt(table, foreignKey);
        print("COMMIT");
        printEndOfStatement();
    }

    /**
     * {@inheritDoc}
     */
    public void createTable(Database database, Table table, Map parameters) throws IOException
    {
        super.createTable(database, table, parameters);

        // creating generator and trigger for auto-increment
        Column[] columns = table.getAutoIncrementColumns();

        for (int idx = 0; idx < columns.length; idx++)
        {
            writeAutoIncrementCreateStmts(database, table, columns[idx]);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected String getNativeDefaultValue(Column column)
    {
        if ((column.getTypeCode() == Types.BIT) ||
            (Jdbc3Utils.supportsJava14JdbcTypes() && (column.getTypeCode() == Jdbc3Utils.determineBooleanTypeCode())))
        {
            return getDefaultValueHelper().convert(column.getDefaultValue(), column.getTypeCode(), Types.SMALLINT).toString();
        }
        else
        {
            return super.getNativeDefaultValue(column);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void dropTable(Table table) throws IOException
    {
        // dropping generators for auto-increment
        Column[] columns = table.getAutoIncrementColumns();

        for (int idx = 0; idx < columns.length; idx++)
        {
            writeAutoIncrementDropStmts(table, columns[idx]);
        }
        super.dropTable(table);
    }

    /**
     * Writes the creation statements to make the given column an auto-increment column.
     * 
     * @param database The database model
     * @param table    The table
     * @param column   The column to make auto-increment
     */
    private void writeAutoIncrementCreateStmts(Database database, Table table, Column column) throws IOException
    {
        print("CREATE GENERATOR ");
        printIdentifier(getGeneratorName(table, column));
        printEndOfStatement();

        print("CREATE TRIGGER ");
        printIdentifier(getConstraintName("trg", table, column.getName(), null));
        print(" FOR ");
        printlnIdentifier(getTableName(table));
        println("ACTIVE BEFORE INSERT POSITION 0 AS");
        print("BEGIN IF (NEW.");
        printIdentifier(getColumnName(column));
        print(" IS NULL) THEN NEW.");
        printIdentifier(getColumnName(column));
        print(" = GEN_ID(");
        printIdentifier(getGeneratorName(table, column));
        print(", 1); END");
        printEndOfStatement();
    }

    /**
     * Writes the statements to drop the auto-increment status for the given column.
     * 
     * @param table  The table
     * @param column The column to remove the auto-increment status for
     */
    private void writeAutoIncrementDropStmts(Table table, Column column) throws IOException
    {
        print("DROP TRIGGER ");
        printIdentifier(getConstraintName("trg", table, column.getName(), null));
        printEndOfStatement();

        print("DROP GENERATOR ");
        printIdentifier(getGeneratorName(table, column));
        printEndOfStatement();
    }

    /**
     * Determines the name of the generator for an auto-increment column.
     * 
     * @param table  The table
     * @param column The auto-increment column
     * @return The generator name
     */
    protected String getGeneratorName(Table table, Column column)
    {
        return getConstraintName("gen", table, column.getName(), null);
    }

    /**
     * {@inheritDoc}
     */
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        // we're using a generator
    }

    /**
     * {@inheritDoc}
     */
    public String getSelectLastIdentityValues(Table table)
    {
        Column[] columns = table.getAutoIncrementColumns();

        if (columns.length == 0)
        {
            return null;
        }
        else
        {
            StringBuffer result = new StringBuffer();
    
            result.append("SELECT ");
            for (int idx = 0; idx < columns.length; idx++)
            {
                result.append("GEN_ID(");
                result.append(getGeneratorName(table, columns[idx]));
                result.append(", 0)");
            }
            result.append(" FROM RDB$DATABASE");
            return result.toString();
        }
    }
}
