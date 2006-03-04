package org.apache.ddlutils.platform.sybase;

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
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Types;
import java.util.Map;

import org.apache.ddlutils.DynaSqlException;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.SqlBuilder;
import org.apache.ddlutils.util.Jdbc3Utils;

/**
 * The SQL Builder for Sybase.
 * 
 * @author James Strachan
 * @author Thomas Dudziak
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
        writeQuotationOnStatement();
        super.createTable(database, table, parameters);
    }

	/**
	 * {@inheritDoc}
	 */
	protected void writeColumn(Table table, Column column) throws IOException
	{
        printIdentifier(getColumnName(column));
        print(" ");
        print(getSqlType(column));

        if (column.getDefaultValue() != null)
        {
            if (!getPlatformInfo().isDefaultValuesForLongTypesSupported() && 
                ((column.getTypeCode() == Types.LONGVARBINARY) || (column.getTypeCode() == Types.LONGVARCHAR)))
            {
                throw new DynaSqlException("The platform does not support default values for LONGVARCHAR or LONGVARBINARY columns");
            }
            print(" DEFAULT ");
            writeColumnDefaultValue(table, column);
        }
        // Sybase does not like NOT NULL and IDENTITY together
        if (column.isRequired() && !column.isAutoIncrement())
        {
            print(" ");
            writeColumnNotNullableStmt();
        }
        if (column.isAutoIncrement())
        {
            print(" ");
            writeColumnAutoIncrementStmt(table, column);
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
    protected void alterTable(Database currentModel, Table currentTable, Database desiredModel, Table desiredTable, boolean doDrops, boolean modifyColumns) throws IOException
    {
    	// we only want to generate the quotation start statement if there is something to write
    	// thus we write the alteration commands into a temporary writer
    	// and only if something was written, write the quotation start statement and the
    	// alteration commands to the original writer
    	Writer       originalWriter = getWriter();
    	StringWriter tempWriter     = new StringWriter();

    	setWriter(tempWriter);
        super.alterTable(currentModel, currentTable, desiredModel, desiredTable, doDrops, modifyColumns);
        setWriter(originalWriter);

        String alterationCommands = tempWriter.toString();

        if (alterationCommands.trim().length() > 0)
        {
        	writeQuotationOnStatement();
        	getWriter().write(alterationCommands);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void dropTable(Table table) throws IOException
    {
        writeQuotationOnStatement();
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
    protected void writeExternalForeignKeyDropStmt(Table table, ForeignKey foreignKey) throws IOException
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
    public void dropExternalForeignKeys(Table table) throws IOException
    {
        writeQuotationOnStatement();
        super.dropExternalForeignKeys(table);
    }

    /**
     * Writes the statement that turns on the ability to write delimited identifiers.
     */
    private void writeQuotationOnStatement() throws IOException
    {
        if (getPlatform().isDelimitedIdentifierModeOn())
        {
            print("SET quoted_identifier on");
            printEndOfStatement();
        }
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
}
