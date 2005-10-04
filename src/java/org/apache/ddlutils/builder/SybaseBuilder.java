package org.apache.ddlutils.builder;

/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
import java.util.HashMap;

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Table;

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
     * @param info The platform info
     */
    public SybaseBuilder(PlatformInfo info)
    {
        super(info);
    }

    /**
     * {@inheritDoc}
     */
    public void createTable(Database database, Table table) throws IOException
    {
        writeQuotationOnStatement();
        super.createTable(database, table);
    }

    /**
     * {@inheritDoc}
     */
    protected void alterTable(Database currentModel, Table currentTable, Database desiredModel, Table desiredTable, boolean doDrops, boolean modifyColumns) throws IOException
    {
        writeQuotationOnStatement();
        super.alterTable(currentModel, currentTable, desiredModel, desiredTable, doDrops, modifyColumns);
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
        String constraintName = foreignKey.getName() == null ? getConstraintName(null, table, "FK", getForeignKeyName(foreignKey)) : foreignKey.getName();

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
        if (getPlatformInfo().isUseDelimitedIdentifiers())
        {
            print("SET quoted_identifier on");
            printEndOfStatement();
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getDeleteSql(Table table, HashMap pkValues, boolean genPlaceholders)
    {
        return getQuotationOnStatement() + super.getDeleteSql(table, pkValues, genPlaceholders);
    }

    /**
     * {@inheritDoc}
     */
    public String getInsertSql(Table table, HashMap columnValues, boolean genPlaceholders)
    {
        return getQuotationOnStatement() + super.getInsertSql(table, columnValues, genPlaceholders);
    }

    /**
     * {@inheritDoc}
     */
    public String getUpdateSql(Table table, HashMap columnValues, boolean genPlaceholders)
    {
        return getQuotationOnStatement() + super.getUpdateSql(table, columnValues, genPlaceholders);
    }

    /**
     * Returns the statement that turns on the ability to write delimited identifiers.
     * 
     * @return The quotation-on statement
     */
    private String getQuotationOnStatement()
    {
        if (getPlatformInfo().isUseDelimitedIdentifiers())
        {
            return "SET quoted_identifier on" + getPlatformInfo().getSqlCommandDelimiter() + "\n";
        }
        else
        {
            return "";
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
