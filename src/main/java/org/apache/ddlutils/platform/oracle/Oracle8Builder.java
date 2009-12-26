package org.apache.ddlutils.platform.oracle;

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
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.ddlutils.DdlUtilsException;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.alteration.ColumnDefinitionChange;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.TypeMap;
import org.apache.ddlutils.platform.SqlBuilder;
import org.apache.ddlutils.util.StringUtilsExt;

/**
 * The SQL Builder for Oracle.
 *
 * @version $Revision$
 */
public class Oracle8Builder extends SqlBuilder
{
	/** The regular expression pattern for ISO dates, i.e. 'YYYY-MM-DD'. */
	private Pattern _isoDatePattern;
	/** The regular expression pattern for ISO times, i.e. 'HH:MI:SS'. */
	private Pattern _isoTimePattern;
	/** The regular expression pattern for ISO timestamps, i.e. 'YYYY-MM-DD HH:MI:SS.fffffffff'. */
	private Pattern _isoTimestampPattern;

	/**
     * Creates a new builder instance.
     * 
     * @param platform The plaftform this builder belongs to
     */
    public Oracle8Builder(Platform platform)
    {
        super(platform);
        addEscapedCharSequence("'", "''");

    	try
    	{
            _isoDatePattern      = Pattern.compile("\\d{4}\\-\\d{2}\\-\\d{2}");
            _isoTimePattern      = Pattern.compile("\\d{2}:\\d{2}:\\d{2}");
            _isoTimestampPattern = Pattern.compile("\\d{4}\\-\\d{2}\\-\\d{2} \\d{2}:\\d{2}:\\d{2}[\\.\\d{1,8}]?");
        }
    	catch (PatternSyntaxException ex)
        {
        	throw new DdlUtilsException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createTable(Database database, Table table, Map parameters) throws IOException
    {
        // lets create any sequences
        Column[] columns = table.getAutoIncrementColumns();

        for (int idx = 0; idx < columns.length; idx++)
        {
            createAutoIncrementSequence(table, columns[idx]);
        }

        super.createTable(database, table, parameters);

        for (int idx = 0; idx < columns.length; idx++)
        {
            createAutoIncrementTrigger(table, columns[idx]);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void dropTable(Table table) throws IOException
    {
        Column[] columns = table.getAutoIncrementColumns();

        for (int idx = 0; idx < columns.length; idx++)
        {
            dropAutoIncrementTrigger(table, columns[idx]);
            dropAutoIncrementSequence(table, columns[idx]);
        }

        print("DROP TABLE ");
        printIdentifier(getTableName(table));
        print(" CASCADE CONSTRAINTS");
        printEndOfStatement();
    }

    /**
     * Creates the sequence necessary for the auto-increment of the given column.
     * 
     * @param table  The table
     * @param column The column
     */
    protected void createAutoIncrementSequence(Table  table,
                                               Column column) throws IOException
    {
        print("CREATE SEQUENCE ");
        printIdentifier(getConstraintName("seq", table, column.getName(), null));
        printEndOfStatement();
    }

    /**
     * Creates the trigger necessary for the auto-increment of the given column.
     * 
     * @param table  The table
     * @param column The column
     */
    protected void createAutoIncrementTrigger(Table  table,
                                              Column column) throws IOException
    {
        String columnName  = getColumnName(column);
        String triggerName = getConstraintName("trg", table, column.getName(), null);

        if (getPlatform().isScriptModeOn())
        {
            // For the script, we output a more nicely formatted version
            print("CREATE OR REPLACE TRIGGER ");
            printlnIdentifier(triggerName);
            print("BEFORE INSERT ON ");
            printlnIdentifier(getTableName(table));
            print("FOR EACH ROW WHEN (new.");
            printIdentifier(columnName);
            println(" IS NULL)");
            println("BEGIN");
            print("  SELECT ");
            printIdentifier(getConstraintName("seq", table, column.getName(), null));
            print(".nextval INTO :new.");
            printIdentifier(columnName);
            print(" FROM dual");
            println(getPlatformInfo().getSqlCommandDelimiter());
            print("END");
            println(getPlatformInfo().getSqlCommandDelimiter());
            println("/");
            println();
        }
        else
        {
            // note that the BEGIN ... SELECT ... END; is all in one line and does
            // not contain a semicolon except for the END-one
            // this way, the tokenizer will not split the statement before the END
            print("CREATE OR REPLACE TRIGGER ");
            printIdentifier(triggerName);
            print(" BEFORE INSERT ON ");
            printIdentifier(getTableName(table));
            print(" FOR EACH ROW WHEN (new.");
            printIdentifier(columnName);
            println(" IS NULL)");
            print("BEGIN SELECT ");
            printIdentifier(getConstraintName("seq", table, column.getName(), null));
            print(".nextval INTO :new.");
            printIdentifier(columnName);
            print(" FROM dual");
            print(getPlatformInfo().getSqlCommandDelimiter());
            print(" END");
            // It is important that there is a semicolon at the end of the statement (or more
            // precisely, at the end of the PL/SQL block), and thus we put two semicolons here
            // because the tokenizer will remove the one at the end
            print(getPlatformInfo().getSqlCommandDelimiter());
            printEndOfStatement();
        }
    }

    /**
     * Drops the sequence used for the auto-increment of the given column.
     * 
     * @param table  The table
     * @param column The column
     */
    protected void dropAutoIncrementSequence(Table  table,
                                             Column column) throws IOException
    {
        print("DROP SEQUENCE ");
        printIdentifier(getConstraintName("seq", table, column.getName(), null));
        printEndOfStatement();
    }

    /**
     * Drops the trigger used for the auto-increment of the given column.
     * 
     * @param table  The table
     * @param column The column
     */
    protected void dropAutoIncrementTrigger(Table  table,
                                            Column column) throws IOException
    {
        print("DROP TRIGGER ");
        printIdentifier(getConstraintName("trg", table, column.getName(), null));
        printEndOfStatement();
    }

    /**
     * {@inheritDoc}
     */
    protected void createTemporaryTable(Database database, Table table, Map parameters) throws IOException
    {
        createTable(database, table, parameters);
    }

    /**
     * {@inheritDoc}
     */
    protected void dropTemporaryTable(Database database, Table table) throws IOException
    {
        dropTable(table);
    }

    /**
     * {@inheritDoc}
     */
    public void dropForeignKeys(Table table) throws IOException
    {
        // no need to as we drop the table with CASCASE CONSTRAINTS
    }

    /**
     * {@inheritDoc}
     */
    public void dropIndex(Table table, Index index) throws IOException
    {
        // Index names in Oracle are unique to a schema and hence Oracle does not
        // use the ON <tablename> clause
        print("DROP INDEX ");
        printIdentifier(getIndexName(index));
        printEndOfStatement();
    }

	/**
     * {@inheritDoc}
     */
    protected void printDefaultValue(Object defaultValue, int typeCode) throws IOException
    {
        if (defaultValue != null)
        {
            String  defaultValueStr = defaultValue.toString();
            boolean shouldUseQuotes = !TypeMap.isNumericType(typeCode) && !defaultValueStr.startsWith("TO_DATE(");
    
            if (shouldUseQuotes)
            {
                // characters are only escaped when within a string literal 
                print(getPlatformInfo().getValueQuoteToken());
                print(escapeStringValue(defaultValueStr));
                print(getPlatformInfo().getValueQuoteToken());
            }
            else
            {
                print(defaultValueStr);
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
    	// Oracle does not accept ISO formats, so we have to convert an ISO spec if we find one
    	// But these are the only formats that we make sure work, every other format has to be database-dependent
    	// and thus the user has to ensure that it is correct
        else if (column.getTypeCode() == Types.DATE)
        {
            if (_isoDatePattern.matcher(column.getDefaultValue()).matches())
            {
            	return "TO_DATE('"+column.getDefaultValue()+"', 'YYYY-MM-DD')";
            }
        }
        else if (column.getTypeCode() == Types.TIME)
        {
            if (_isoTimePattern.matcher(column.getDefaultValue()).matches())
            {
            	return "TO_DATE('"+column.getDefaultValue()+"', 'HH24:MI:SS')";
            }
        }
        else if (column.getTypeCode() == Types.TIMESTAMP)
        {
            if (_isoTimestampPattern.matcher(column.getDefaultValue()).matches())
            {
            	return "TO_DATE('"+column.getDefaultValue()+"', 'YYYY-MM-DD HH24:MI:SS')";
            }
        }
        return super.getNativeDefaultValue(column);
    }

    /**
     * {@inheritDoc}
     */
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        // we're using sequences instead
    }

    /**
     * {@inheritDoc}
     */
    public String getSelectLastIdentityValues(Table table)
    {
        Column[] columns = table.getAutoIncrementColumns();

        if (columns.length > 0)
        {
            StringBuffer result = new StringBuffer();

            result.append("SELECT ");
            for (int idx = 0; idx < columns.length; idx++)
            {
                if (idx > 0)
                {
                    result.append(",");
                }
                result.append(getDelimitedIdentifier(getConstraintName("seq", table, columns[idx].getName(), null)));
                result.append(".currval");
            }
            result.append(" FROM dual");
            return result.toString();
        }
        else
        {
            return null;
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
        if (newColumn.isAutoIncrement())
        {
            createAutoIncrementSequence(table, newColumn);
            createAutoIncrementTrigger(table, newColumn);
        }
    }

    /**
     * Writes the SQL to drop a column.
     * 
     * @param table  The table
     * @param column The column to drop
     */
    public void dropColumn(Table table, Column column) throws IOException
    {
        if (column.isAutoIncrement())
        {
            dropAutoIncrementTrigger(table, column);
            dropAutoIncrementSequence(table, column);
        }
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
     * {@inheritDoc}
     */
    protected void writeCastExpression(Column sourceColumn, Column targetColumn) throws IOException
    {
        boolean sizeChanged = TypeMap.isTextType(targetColumn.getTypeCode()) &&
                              ColumnDefinitionChange.isSizeChanged(getPlatformInfo(), sourceColumn, targetColumn) &&
                              !StringUtilsExt.isEmpty(targetColumn.getSize());

        if (sizeChanged)
        {
            print("SUBSTR(");
        }
        if (ColumnDefinitionChange.isTypeChanged(getPlatformInfo(), sourceColumn, targetColumn))
        {
            print("CAST (");
            printIdentifier(getColumnName(sourceColumn));
            print(" AS ");
            print(getSqlType(targetColumn));
            print(")");
        }
        else
        {
            printIdentifier(getColumnName(sourceColumn));
        }
        if (sizeChanged)
        {
            print(",0,");
            print(targetColumn.getSize());
            print(")");
        }
    }
}
