package org.apache.ddlutils.platform.sybase;

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
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.SqlBuilder;
import org.apache.ddlutils.util.StringUtilsExt;

/**
 * The SQL Builder for Sybase.
 * 
 * @version $Revision$
 */
public class SybaseBuilder extends SqlBuilder
{
    /**
     * Creates a new builder instance.
     * 
     * @param platform The plaftform this builder belongs to
     */
    public SybaseBuilder(Platform platform)
    {
        super(platform);
        addEscapedCharSequence("'", "''");
    }

    /**
     * {@inheritDoc}
     */
    public void createTable(Database database, Table table, Map parameters) throws IOException
    {
        turnOnQuotation();
        super.createTable(database, table, parameters);
    }

    /**
     * {@inheritDoc}
     */
	protected void writeTableCreationStmtEnding(Table table, Map parameters) throws IOException
    {
        if (parameters != null)
        {
            // We support
            // - 'lock'
            // - 'at'
            // - 'external table at'
            // - 'on'
            // - with parameters as name value pairs

            String lockValue            = (String)parameters.get("lock");
            String atValue              = (String)parameters.get("at");
            String externalTableAtValue = (String)parameters.get("external table at");
            String onValue              = (String)parameters.get("on");

            if (lockValue != null)
            {
                print(" lock ");
                print(lockValue);
            }

            boolean writtenWithParameters = false;

            for (Iterator it = parameters.entrySet().iterator(); it.hasNext();)
            {
                Map.Entry entry = (Map.Entry)it.next();
                String    name  = entry.getKey().toString();

                if (!"lock".equals(name) && !"at".equals(name) && !"external table at".equals(name) && !"on".equals(name))
                {
                    if (!writtenWithParameters)
                    {
                        print(" with ");
                        writtenWithParameters = true;
                    }
                    else
                    {
                        print(", ");
                    }
                    print(name);
                    if (entry.getValue() != null)
                    {
                        print("=");
                        print(entry.getValue().toString());
                    }
                }
            }
            if (onValue != null)
            {
                print(" on ");
                print(onValue);
            }
            if (externalTableAtValue != null)
            {
                print(" external table at \"");
                print(externalTableAtValue);
                print("\"");
            }
            else if (atValue != null)
            {
                print(" at \"");
                print(atValue);
                print("\"");
            }
        }
        super.writeTableCreationStmtEnding(table, parameters);
    }

    /**
	 * {@inheritDoc}
	 */
	protected void writeColumn(Table table, Column column) throws IOException
	{
        printIdentifier(getColumnName(column));
        print(" ");
        print(getSqlType(column));
        writeColumnDefaultValueStmt(table, column);
        // Sybase does not like NULL/NOT NULL and IDENTITY together
        if (column.isAutoIncrement())
        {
            print(" ");
            writeColumnAutoIncrementStmt(table, column);
        }
        else
        {
            print(" ");
            if (column.isRequired())
            {
                writeColumnNotNullableStmt();
            }
            else
            {
                // we'll write a NULL for all columns that are not required 
                writeColumnNullableStmt();
            }
        }
	}

	/**
     * {@inheritDoc}
     */
    protected String getNativeDefaultValue(Column column)
    {
        if ((column.getTypeCode() == Types.BIT) || (column.getTypeCode() == Types.BOOLEAN))
        {
            return getDefaultValueHelper().convert(column.getDefaultValue(), column.getTypeCode(), Types.SMALLINT);
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
        turnOnQuotation();
        print("IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = ");
        printAlwaysSingleQuotedIdentifier(getTableName(table));
        println(")");
        println("BEGIN");
        printIndent();
        print("DROP TABLE ");
        printlnIdentifier(getTableName(table));
        print("END");
        printEndOfStatement();
    }

    /**
     * {@inheritDoc}
     */
    public void dropForeignKey(Table table, ForeignKey foreignKey) throws IOException
    {
        String constraintName = getForeignKeyName(table, foreignKey);

        print("IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'RI' AND name = ");
        printAlwaysSingleQuotedIdentifier(constraintName);
        println(")");
        printIndent();
        print("ALTER TABLE ");
        printIdentifier(getTableName(table));
        print(" DROP CONSTRAINT ");
        printIdentifier(constraintName);
        printEndOfStatement();
    }

    /**
     * {@inheritDoc}
     */
    public void dropIndex(Table table, Index index) throws IOException
    {
        print("DROP INDEX ");
        printIdentifier(getTableName(table));
        print(".");
        printIdentifier(getIndexName(index));
        printEndOfStatement();
    }

    /**
     * {@inheritDoc}
     */
    public void dropForeignKeys(Table table) throws IOException
    {
        turnOnQuotation();
        super.dropForeignKeys(table);
    }

    /**
     * {@inheritDoc}
     */
    public String getSelectLastIdentityValues(Table table)
    {
        return "SELECT @@IDENTITY";
    }

    /**
     * Returns the SQL to enable identity override mode.
     * 
     * @param table The table to enable the mode for
     * @return The SQL
     */
    protected String getEnableIdentityOverrideSql(Table table)
    {
        StringBuffer result = new StringBuffer();

        result.append("SET IDENTITY_INSERT ");
        result.append(getDelimitedIdentifier(getTableName(table)));
        result.append(" ON");

        return result.toString();
    }

    /**
     * Returns the SQL to disable identity override mode.
     * 
     * @param table The table to disable the mode for
     * @return The SQL
     */
    protected String getDisableIdentityOverrideSql(Table table)
    {
        StringBuffer result = new StringBuffer();

        result.append("SET IDENTITY_INSERT ");
        result.append(getDelimitedIdentifier(getTableName(table)));
        result.append(" OFF");

        return result.toString();
    }

    /**
     * Returns the statement that turns on the ability to write delimited identifiers.
     * 
     * @return The quotation-on statement
     */
    protected String getQuotationOnStatement()
    {
        if (getPlatform().isDelimitedIdentifierModeOn())
        {
            return "SET quoted_identifier on";
        }
        else
        {
            return "";
        }
    }

    /**
     * Writes the statement that turns on the ability to write delimited identifiers.
     */
    public void turnOnQuotation() throws IOException
    {
        String quotationStmt = getQuotationOnStatement();

        if (!StringUtilsExt.isEmpty(quotationStmt))
        {
            print(quotationStmt);
            printEndOfStatement();
        }
    }

    /**
     * Writes the statement that turns on identity override mode.
     * 
     * @param table The table to enable the mode for
     */
    public void turnOnIdentityOverride(Table table) throws IOException
    {
        print(getEnableIdentityOverrideSql(table));
        printEndOfStatement();
    }

    /**
     * Writes the statement that turns off identity override mode.
     * 
     * @param table The table to disable the mode for
     */
    public void turnOffIdentityOverride(Table table) throws IOException
    {
        print(getDisableIdentityOverrideSql(table));
        printEndOfStatement();
    }

    /**
     * Prints the given identifier with enforced single quotes around it regardless of whether 
     * delimited identifiers are turned on or not.
     * 
     * @param identifier The identifier
     */
    private void printAlwaysSingleQuotedIdentifier(String identifier) throws IOException
    {
        print("'");
        print(identifier);
        print("'");
    }

    /**
     * {@inheritDoc}
     */
    protected void copyData(Table sourceTable, Table targetTable) throws IOException
    {
        // We need to turn on identity override except when the identity column was added to the column
        Column[] targetAutoIncrCols   = targetTable.getAutoIncrementColumns();
        boolean  needIdentityOverride = false;

        if (targetAutoIncrCols.length > 0)
        {
            needIdentityOverride = true;
            // Sybase only allows for one identity column per table
            if (sourceTable.findColumn(targetAutoIncrCols[0].getName(), getPlatform().isDelimitedIdentifierModeOn()) == null)
            {
                needIdentityOverride = false;
            }
        }
        if (needIdentityOverride)
        {
            print(getEnableIdentityOverrideSql(targetTable));
            printEndOfStatement();
        }
        super.copyData(sourceTable, targetTable);
        if (needIdentityOverride)
        {
            print(getDisableIdentityOverrideSql(targetTable));
            printEndOfStatement();
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void writeCastExpression(Column sourceColumn, Column targetColumn) throws IOException
    {
        String sourceNativeType = getBareNativeType(sourceColumn);
        String targetNativeType = getBareNativeType(targetColumn);

        if (sourceNativeType.equals(targetNativeType))
        {
            printIdentifier(getColumnName(sourceColumn));
        }
        else
        {
            print("CONVERT(");
            print(getNativeType(targetColumn));
            print(",");
            printIdentifier(getColumnName(sourceColumn));
            print(")");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addColumn(Database model, Table table, Column newColumn) throws IOException
    {
        print("ALTER TABLE ");
        printlnIdentifier(getTableName(table));
        printIndent();
        print("ADD ");
        writeColumn(table, newColumn);
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
        print("DROP ");
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
        // this would be easier when named primary keys are supported
        // because then we can use ALTER TABLE DROP
        String tableName         = getTableName(table);
        String tableNameVar      = "tn" + createUniqueIdentifier();
        String constraintNameVar = "cn" + createUniqueIdentifier();

        println("BEGIN");
        println("  DECLARE @" + tableNameVar + " nvarchar(60), @" + constraintNameVar + " nvarchar(60)");
        println("  WHILE EXISTS(SELECT sysindexes.name");
        println("                 FROM sysindexes, sysobjects");
        print("                 WHERE sysobjects.name = ");
        printAlwaysSingleQuotedIdentifier(tableName);
        println(" AND sysobjects.id = sysindexes.id AND (sysindexes.status & 2048) > 0)");
        println("  BEGIN");
        println("    SELECT @" + tableNameVar + " = sysobjects.name, @" + constraintNameVar + " = sysindexes.name");
        println("      FROM sysindexes, sysobjects");
        print("      WHERE sysobjects.name = ");
        printAlwaysSingleQuotedIdentifier(tableName);
        print(" AND sysobjects.id = sysindexes.id AND (sysindexes.status & 2048) > 0");
        println("    EXEC ('ALTER TABLE '+@" + tableNameVar + "+' DROP CONSTRAINT '+@" + constraintNameVar + ")");
        println("  END");
        print("END");
        printEndOfStatement();
    }

    /**
     * Writes the SQL to set the default value of the given column.
     * 
     * @param table           The table
     * @param column          The column to change
     * @param newDefaultValue The new default value
     */
    public void changeColumnDefaultValue(Table table, Column column, String newDefaultValue) throws IOException
    {
        print("ALTER TABLE ");
        printlnIdentifier(getTableName(table));
        printIndent();
        print("REPLACE ");
        printIdentifier(getColumnName(column));
        print(" DEFAULT ");
        if (isValidDefaultValue(newDefaultValue, column.getTypeCode()))
        {
            printDefaultValue(newDefaultValue, column.getTypeCode());
        }
        else
        {
            print("NULL");
        }
        printEndOfStatement();
    }

    /**
     * Writes the SQL to change the given column.
     * 
     * @param table     The table
     * @param column    The column to change
     * @param newColumn The new column definition
     */
    public void changeColumn(Table table, Column column, Column newColumn) throws IOException
    {
        Object oldParsedDefault = column.getParsedDefaultValue();
        Object newParsedDefault = newColumn.getParsedDefaultValue();
        String newDefault       = newColumn.getDefaultValue();
        boolean defaultChanges  = ((oldParsedDefault == null) && (newParsedDefault != null)) ||
                                  ((oldParsedDefault != null) && !oldParsedDefault.equals(newParsedDefault));

        // Sybase does not like it if there is a default spec in the ALTER TABLE ALTER
        // statement; thus we have to change the default afterwards
        if (defaultChanges)
        {
            // we're first removing the default as it might make problems when the
            // datatype changes
            print("ALTER TABLE ");
            printlnIdentifier(getTableName(table));
            printIndent();
            print("REPLACE ");
            printIdentifier(getColumnName(column));
            print(" DEFAULT NULL");
            printEndOfStatement();
        }
        print("ALTER TABLE ");
        printlnIdentifier(getTableName(table));
        printIndent();
        print("MODIFY ");
        if (newDefault != null)
        {
            newColumn.setDefaultValue(null);
        }
        writeColumn(table, newColumn);
        if (newDefault != null)
        {
            newColumn.setDefaultValue(newDefault);
        }
        printEndOfStatement();
        if (defaultChanges)
        {
            print("ALTER TABLE ");
            printlnIdentifier(getTableName(table));
            printIndent();
            print("REPLACE ");
            printIdentifier(getColumnName(column));
            if (newDefault != null)
            {
                writeColumnDefaultValueStmt(table, newColumn);
            }
            else
            {
                print(" DEFAULT NULL");
            }
            printEndOfStatement();
        }
   }
}
