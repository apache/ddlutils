package org.apache.ddlutils.platform.mssql;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.alteration.ColumnDefinitionChange;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.TypeMap;
import org.apache.ddlutils.platform.SqlBuilder;

/**
 * The SQL Builder for the Microsoft SQL Server.
 * 
 * @version $Revision$
 */
public class MSSqlBuilder extends SqlBuilder
{
    /** We use a generic date format. */
    private DateFormat _genericDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    /** We use a generic date format. */
    private DateFormat _genericTimeFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     * Creates a new builder instance.
     * 
     * @param platform The plaftform this builder belongs to
     */
    public MSSqlBuilder(Platform platform)
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
    public void dropTable(Table table) throws IOException
    {
        String tableName         = getTableName(table);
        String tableNameVar      = "tn" + createUniqueIdentifier();
        String constraintNameVar = "cn" + createUniqueIdentifier();

        turnOnQuotation();
        print("IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = ");
        printAlwaysSingleQuotedIdentifier(tableName);
        println(")");
        println("BEGIN");
        println("  DECLARE @" + tableNameVar + " nvarchar(256), @" + constraintNameVar + " nvarchar(256)");
        println("  DECLARE refcursor CURSOR FOR");
        println("  SELECT object_name(objs.parent_obj) tablename, objs.name constraintname");
        println("    FROM sysobjects objs JOIN sysconstraints cons ON objs.id = cons.constid");
        print("    WHERE objs.xtype != 'PK' AND object_name(objs.parent_obj) = ");
        printAlwaysSingleQuotedIdentifier(tableName);
        println("  OPEN refcursor");
        println("  FETCH NEXT FROM refcursor INTO @" + tableNameVar + ", @" + constraintNameVar);
        println("  WHILE @@FETCH_STATUS = 0");
        println("    BEGIN");
        println("      EXEC ('ALTER TABLE '+@" + tableNameVar + "+' DROP CONSTRAINT '+@" + constraintNameVar + ")");
        println("      FETCH NEXT FROM refcursor INTO @" + tableNameVar + ", @" + constraintNameVar);
        println("    END");
        println("  CLOSE refcursor");
        println("  DEALLOCATE refcursor");
        print("  DROP TABLE ");
        printlnIdentifier(tableName);
        print("END");
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
    protected DateFormat getValueDateFormat()
    {
        return _genericDateFormat;
    }

    /**
     * {@inheritDoc}
     */
    protected DateFormat getValueTimeFormat()
    {
        return _genericTimeFormat;
    }

    /**
     * {@inheritDoc}
     */
    protected String getValueAsString(Column column, Object value)
    {
        if (value == null)
        {
            return "NULL";
        }

        StringBuffer result = new StringBuffer();

        switch (column.getTypeCode())
        {
            case Types.REAL:
            case Types.NUMERIC:
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.DECIMAL:
                // SQL Server does not want quotes around the value
                if (!(value instanceof String) && (getValueNumberFormat() != null))
                {
                    result.append(getValueNumberFormat().format(value));
                }
                else
                {
                    result.append(value.toString());
                }
                break;
            case Types.DATE:
                result.append("CAST(");
                result.append(getPlatformInfo().getValueQuoteToken());
                result.append(value instanceof String ? (String)value : getValueDateFormat().format(value));
                result.append(getPlatformInfo().getValueQuoteToken());
                result.append(" AS datetime)");
                break;
            case Types.TIME:
                result.append("CAST(");
                result.append(getPlatformInfo().getValueQuoteToken());
                result.append(value instanceof String ? (String)value : getValueTimeFormat().format(value));
                result.append(getPlatformInfo().getValueQuoteToken());
                result.append(" AS datetime)");
                break;
            case Types.TIMESTAMP:
                result.append("CAST(");
                result.append(getPlatformInfo().getValueQuoteToken());
                result.append(value.toString());
                result.append(getPlatformInfo().getValueQuoteToken());
                result.append(" AS datetime)");
                break;
        }
        return super.getValueAsString(column, value);
    }

    /**
     * {@inheritDoc}
     */
    protected String getNativeDefaultValue(Column column)
    {
    	// Sql Server wants BIT default values as 0 or 1
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
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        print("IDENTITY (1,1) ");
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
    public void dropForeignKey(Table table, ForeignKey foreignKey) throws IOException
    {
        String constraintName = getForeignKeyName(table, foreignKey);

        print("IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'F' AND name = ");
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
     * Returns the statement that turns on the ability to write delimited identifiers.
     * 
     * @return The quotation-on statement
     */
    private String getQuotationOnStatement()
    {
        if (getPlatform().isDelimitedIdentifierModeOn())
        {
            return "SET quoted_identifier on" + getPlatformInfo().getSqlCommandDelimiter() + "\n";
        }
        else
        {
            return "";
        }
    }

    /**
     * If quotation mode is on, then this writes the statement that turns on the ability to write delimited identifiers.
     */
    protected void turnOnQuotation() throws IOException
    {
        print(getQuotationOnStatement());
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

        result.append(getQuotationOnStatement());
        result.append("SET IDENTITY_INSERT ");
        result.append(getDelimitedIdentifier(getTableName(table)));
        result.append(" ON");
        result.append(getPlatformInfo().getSqlCommandDelimiter());

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

        result.append(getQuotationOnStatement());
        result.append("SET IDENTITY_INSERT ");
        result.append(getDelimitedIdentifier(getTableName(table)));
        result.append(" OFF");
        result.append(getPlatformInfo().getSqlCommandDelimiter());

        return result.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String getDeleteSql(Table table, Map pkValues, boolean genPlaceholders)
    {
        return getQuotationOnStatement() + super.getDeleteSql(table, pkValues, genPlaceholders);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getInsertSql(Table table, Map columnValues, boolean genPlaceholders)
    {
        return getQuotationOnStatement() + super.getInsertSql(table, columnValues, genPlaceholders);
    }

    /**
     * {@inheritDoc}
     */
    public String getUpdateSql(Table table, Map columnValues, boolean genPlaceholders)
    {
        return getQuotationOnStatement() + super.getUpdateSql(table, columnValues, genPlaceholders);
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
        // Sql Server per default does not allow us to insert values explicitly into
        // identity columns. However, we can change this behavior
        // We need to this only if
        // - there is a column in both tables that is auto increment only in the target table, or
        // - there is a column in both tables that is auto increment in both tables
        Column[] targetIdentityColumns = targetTable.getAutoIncrementColumns();

        // Sql Server allows only one identity column, so let's take a shortcut here
        boolean needToAllowIdentityInsert = (targetIdentityColumns.length > 0) &&
                                            (sourceTable.findColumn(targetIdentityColumns[0].getName(), getPlatform().isDelimitedIdentifierModeOn()) != null);

        if (needToAllowIdentityInsert)
        {
            print("SET IDENTITY_INSERT ");
            printIdentifier(getTableName(targetTable));
            print(" ON");
            printEndOfStatement();
        }
        super.copyData(sourceTable, targetTable);
        // We have to turn it off ASAP because it can be on only for one table per session
        if (needToAllowIdentityInsert)
        {
            print("SET IDENTITY_INSERT ");
            printIdentifier(getTableName(targetTable));
            print(" OFF");
            printEndOfStatement();
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
     * Generates the SQL to drop a column from a table.
     * 
     * @param table  The table where to drop the column from
     * @param column The column to drop
     */
    public void dropColumn(Table table, Column column) throws IOException
    {
        if (!StringUtils.isEmpty(column.getDefaultValue()))
        {
            writeDropConstraintStatement(table, column, "D");
        }
        print("ALTER TABLE ");
        printlnIdentifier(getTableName(table));
        printIndent();
        print("DROP COLUMN ");
        printIdentifier(getColumnName(column));
        printEndOfStatement();
    }

    /**
     * Writes the SQL for dropping the primary key of the given table.
     * 
     * @param table The table
     */
    public void dropPrimaryKey(Table table) throws IOException
    {
        // this would be easier if named primary keys are supported
        // because for named pks we could use ALTER TABLE DROP
        writeDropConstraintStatement(table, null, "PK");
    }

    /**
     * Writes the SQL to recreate a column, e.g. using a different type or similar.
     * 
     * @param table     The table
     * @param curColumn The current column definition
     * @param newColumn The new column definition
     */
    public void recreateColumn(Table table, Column curColumn, Column newColumn) throws IOException
    {
        boolean hasDefault       = curColumn.getParsedDefaultValue() != null;
        boolean shallHaveDefault = newColumn.getParsedDefaultValue() != null;
        String  newDefault       = newColumn.getDefaultValue();

        // Sql Server does not like it if there is a default spec in the ALTER TABLE ALTER COLUMN
        // statement; thus we have to change the default manually
        if (newDefault != null)
        {
            newColumn.setDefaultValue(null);
        }
        if (hasDefault)
        {
            // we're dropping the old default
            writeDropConstraintStatement(table, curColumn, "D");
        }

        print("ALTER TABLE ");
        printlnIdentifier(getTableName(table));
        printIndent();
        print("ALTER COLUMN ");
        writeColumn(table, newColumn);
        printEndOfStatement();

        if (shallHaveDefault)
        {
            newColumn.setDefaultValue(newDefault);

            // if the column shall have a default, then we have to add it as a constraint
            print("ALTER TABLE ");
            printlnIdentifier(getTableName(table));
            printIndent();
            print("ADD CONSTRAINT ");
            printIdentifier(getConstraintName("DF", table, curColumn.getName(), null));
            writeColumnDefaultValueStmt(table, newColumn);
            print(" FOR ");
            printIdentifier(getColumnName(curColumn));
            printEndOfStatement();
        }
    }

    /**
     * Writes the SQL to drop a constraint, e.g. a primary key or default value constraint.
     * 
     * @param table          The table that the constraint is on
     * @param column         The column that the constraint is on; <code>null</code> for table-level
     *                       constraints
     * @param typeIdentifier The constraint type identifier as is specified for the
     *                       <code>sysobjects</code> system table
     */
    protected void writeDropConstraintStatement(Table table, Column column, String typeIdentifier) throws IOException
    {
        String tableName         = getTableName(table);
        String columnName        = column == null ? null : getColumnName(column);
        String tableNameVar      = "tn" + createUniqueIdentifier();
        String constraintNameVar = "cn" + createUniqueIdentifier();

        println("BEGIN");
        println("  DECLARE @" + tableNameVar + " nvarchar(256), @" + constraintNameVar + " nvarchar(256)");
        println("  DECLARE refcursor CURSOR FOR");
        println("  SELECT object_name(objs.parent_obj) tablename, objs.name constraintname");
        println("    FROM sysobjects objs JOIN sysconstraints cons ON objs.id = cons.constid");
        print("    WHERE objs.xtype = '");
        print(typeIdentifier);
        println("' AND");
        if (columnName != null)
        {
            print("          cons.colid = (SELECT colid FROM syscolumns WHERE id = object_id(");
            printAlwaysSingleQuotedIdentifier(tableName);
            print(") AND name = ");
            printAlwaysSingleQuotedIdentifier(columnName);
            println(") AND");
        }
        print("          object_name(objs.parent_obj) = ");
        printAlwaysSingleQuotedIdentifier(tableName);
        println("  OPEN refcursor");
        println("  FETCH NEXT FROM refcursor INTO @" + tableNameVar + ", @" + constraintNameVar);
        println("  WHILE @@FETCH_STATUS = 0");
        println("    BEGIN");
        println("      EXEC ('ALTER TABLE '+@" + tableNameVar + "+' DROP CONSTRAINT '+@" + constraintNameVar + ")");
        println("      FETCH NEXT FROM refcursor INTO @" + tableNameVar + ", @" + constraintNameVar);
        println("    END");
        println("  CLOSE refcursor");
        println("  DEALLOCATE refcursor");
        print("END");
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
            if (TypeMap.isTextType(targetColumn.getTypeCode()) && sizeChanged &&
                (targetColumn.getSize() != null) && (sourceColumn.getSizeAsInt() > targetColumn.getSizeAsInt()))
            {
                print("SUBSTRING(CAST(");
                printIdentifier(getColumnName(sourceColumn));
                print(" AS ");
                print(getNativeType(targetColumn));
                print("),1,");
                print(getSizeSpec(targetColumn));
                print(")");
            }
            else
            {
                print("CAST(");
                printIdentifier(getColumnName(sourceColumn));
                print(" AS ");
                print(getSqlType(targetColumn));
                print(")");
            }
        }
        else
        {
            printIdentifier(getColumnName(sourceColumn));
        }
    }
}
