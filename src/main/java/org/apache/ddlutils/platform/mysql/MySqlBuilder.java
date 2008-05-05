package org.apache.ddlutils.platform.mysql;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.sql.Types;
import java.util.Iterator;
import java.util.Map;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.alteration.ColumnDefinitionChange;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.TypeMap;
import org.apache.ddlutils.platform.SqlBuilder;

/**
 * The SQL Builder for MySQL.
 * 
 * @version $Revision$
 */
public class MySqlBuilder extends SqlBuilder
{
    /**
     * Creates a new builder instance.
     * 
     * @param platform The plaftform this builder belongs to
     */
    public MySqlBuilder(Platform platform)
    {
        super(platform);
        // we need to handle the backslash first otherwise the other
        // already escaped sequences would be affected
        addEscapedCharSequence("\\",     "\\\\");
        addEscapedCharSequence("\0",     "\\0");
        addEscapedCharSequence("'",      "\\'");
        addEscapedCharSequence("\"",     "\\\"");
        addEscapedCharSequence("\b",     "\\b");
        addEscapedCharSequence("\n",     "\\n");
        addEscapedCharSequence("\r",     "\\r");
        addEscapedCharSequence("\t",     "\\t");
        addEscapedCharSequence("\u001A", "\\Z");
        addEscapedCharSequence("%",      "\\%");
        addEscapedCharSequence("_",      "\\_");
    }

    /**
     * {@inheritDoc}
     */
    public void dropTable(Table table) throws IOException
    { 
        print("DROP TABLE IF EXISTS ");
        printIdentifier(getTableName(table));
        printEndOfStatement();
    }

    /**
     * {@inheritDoc}
     */
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        print("AUTO_INCREMENT");
    }

    /**
     * {@inheritDoc}
     */
    protected boolean shouldGeneratePrimaryKeys(Column[] primaryKeyColumns)
    {
        // mySQL requires primary key indication for autoincrement key columns
        // I'm not sure why the default skips the pk statement if all are identity
        return true;
    }

    /**
     * {@inheritDoc}
     * Normally mysql will return the LAST_INSERT_ID as the column name for the inserted id.
     * Since ddlutils expects the real column name of the field that is autoincrementing, the
     * column has an alias of that column name.
     */
    public String getSelectLastIdentityValues(Table table)
    {
        String autoIncrementKeyName = "";
        if (table.getAutoIncrementColumns().length > 0)
        {
            autoIncrementKeyName = table.getAutoIncrementColumns()[0].getName();
        }
        return "SELECT LAST_INSERT_ID() " + autoIncrementKeyName;
    }

    /**
     * {@inheritDoc}
     */
    protected void writeTableCreationStmtEnding(Table table, Map parameters) throws IOException
    {
        if (parameters != null)
        {
            print(" ");
            // MySql supports additional table creation options which are appended
            // at the end of the CREATE TABLE statement
            for (Iterator it = parameters.entrySet().iterator(); it.hasNext();)
            {
                Map.Entry entry = (Map.Entry)it.next();

                print(entry.getKey().toString());
                if (entry.getValue() != null)
                {
                    print("=");
                    print(entry.getValue().toString());
                }
                if (it.hasNext())
                {
                    print(" ");
                }
            }
        }
        super.writeTableCreationStmtEnding(table, parameters);
    }

    /**
     * {@inheritDoc}
     */
    public void dropForeignKey(Table table, ForeignKey foreignKey) throws IOException
    {
        writeTableAlterStmt(table);
        print("DROP FOREIGN KEY ");
        printIdentifier(getForeignKeyName(table, foreignKey));
        printEndOfStatement();

        // InnoDB won't drop the auto-index for the foreign key automatically, so we have to do it
        if (foreignKey.isAutoIndexPresent())
        {
            writeTableAlterStmt(table);
            print("DROP INDEX ");
            printIdentifier(getForeignKeyName(table, foreignKey));
            printEndOfStatement();
        }
    }

    /**
     * Writes the SQL to add/insert a column.
     * 
     * @param table      The table
     * @param newColumn  The new column
     * @param prevColumn The column after which the new column shall be added; <code>null</code>
     *                   if the new column is to be inserted at the beginning
     */
    public void insertColumn(Table table, Column newColumn, Column prevColumn) throws IOException
    {
        print("ALTER TABLE ");
        printlnIdentifier(getTableName(table));
        printIndent();
        print("ADD COLUMN ");
        writeColumn(table, newColumn);
        if (prevColumn != null)
        {
            print(" AFTER ");
            printIdentifier(getColumnName(prevColumn));
        }
        else
        {
            print(" FIRST");
        }
        printEndOfStatement();
    }

    /**
     * Writes the SQL to drop a column.
     * 
     * @param table  The table
     * @param column The column to drop
     */
    public void dropColumn(Table table, Column column) throws IOException
    {
        print("ALTER TABLE ");
        printlnIdentifier(getTableName(table));
        printIndent();
        print("DROP COLUMN ");
        printIdentifier(getColumnName(column));
        printEndOfStatement();
    }

    /**
     * Writes the SQL to drop the primary key of the given table.
     * 
     * @param table The table
     */
    public void dropPrimaryKey(Table table) throws IOException
    {
        print("ALTER TABLE ");
        printlnIdentifier(getTableName(table));
        printIndent();
        print("DROP PRIMARY KEY");
        printEndOfStatement();
    }

    /**
     * Writes the SQL to recreate a column, e.g. with a different type.  
     * 
     * @param table  The table
     * @param column The new column definition
     */
    public void recreateColumn(Table table, Column column) throws IOException
    {
        print("ALTER TABLE ");
        printlnIdentifier(getTableName(table));
        printIndent();
        print("MODIFY COLUMN ");
        writeColumn(table, column);
        printEndOfStatement();
    }

    /**
     * {@inheritDoc}
     */
    protected void writeCastExpression(Column sourceColumn, Column targetColumn) throws IOException
    {
        boolean sizeChanged = ColumnDefinitionChange.isSizeChanged(getPlatformInfo(), sourceColumn, targetColumn);
        boolean typeChanged = ColumnDefinitionChange.isTypeChanged(getPlatformInfo(), sourceColumn, targetColumn);

        if (sizeChanged || typeChanged)
        {
            String targetNativeType = getNativeType(targetColumn);

            switch (targetColumn.getTypeCode())
            {
                case Types.BIT:
                case Types.BOOLEAN:
                case Types.TINYINT:
                case Types.SMALLINT:
                case Types.INTEGER:
                case Types.BIGINT:
                    targetNativeType = "SIGNED";
                    break;
                case Types.FLOAT:
                case Types.REAL:
                case Types.DOUBLE:
                    targetNativeType = "SIGNED"; // ?
                    break;
                case Types.DECIMAL:
                case Types.NUMERIC:
                    targetNativeType = "DECIMAL";
                    break;
                case Types.DATE:
                    targetNativeType = "DATE";
                    break;
                case Types.TIMESTAMP:
                    targetNativeType = "DATETIME";
                    break;
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                case Types.CLOB:
                    targetNativeType = "CHAR";
                    break;
                default:
                    targetNativeType = "BINARY";
                    break;
            }

            print("CAST(");
            if (TypeMap.isTextType(sourceColumn.getTypeCode()) && TypeMap.isTextType(targetColumn.getTypeCode()) && sizeChanged)
            {
                print("LEFT(");
                printIdentifier(getColumnName(sourceColumn));
                print(",");
                print(targetColumn.getSize());
                print(")");
            }
            else
            {
                printIdentifier(getColumnName(sourceColumn));
            }
            print(" AS ");
            print(getSqlType(targetColumn, targetNativeType));
            print(")");
        }
        else
        {
            printIdentifier(getColumnName(sourceColumn));
        }
    }
}
