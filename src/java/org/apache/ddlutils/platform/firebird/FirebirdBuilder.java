package org.apache.ddlutils.platform.firebird;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.SqlBuilder;
import org.apache.ddlutils.util.Jdbc3Utils;

/**
 * The SQL Builder for the FireBird database.
 * 
 * @author Martin van den Bemt
 * @author Thomas Dudziak
 * @version $Revision: 231306 $
 */
public class FirebirdBuilder extends SqlBuilder
{
    /** Denotes the string used via SET TERM for delimiting commands that need to be executed in one go. */
    public static final String TERM_COMMAND = "--TERM--";

    /**
     * Creates a new builder instance.
     * 
     * @param info The platform info
     */
    public FirebirdBuilder(PlatformInfo info)
    {
        super(info);
        addEscapedCharSequence("'", "''");
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
            print("CREATE GENERATOR ");
            printIdentifier(getGeneratorName(table, columns[idx]));
            printEndOfStatement();
            print(TERM_COMMAND);
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
            println("END;");
            print(TERM_COMMAND);
            printEndOfStatement();
        }
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
    public void dropTable(Table table) throws IOException
    {
        super.dropTable(table);
        // dropping generators for auto-increment
        Column[] columns = table.getAutoIncrementColumns();

        for (int idx = 0; idx < columns.length; idx++)
        {
            print("DROP GENERATOR ");
            printIdentifier(getConstraintName("gen", table, columns[idx].getName(), null));
            printEndOfStatement();
        }
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
     * @todo : we are kind of stuck here, since last insert id needs the database name..
     */
    public String getSelectLastInsertId(Table table)
    {
        Column[] columns = table.getAutoIncrementColumns();

        if (columns.length == 0)
        {
            return null;
        }
        else
        {
            StringBuffer result = new StringBuffer();
    
            for (int idx = 0; idx < columns.length; idx++)
            {
                result.append("SELECT GEN_ID (");
                result.append(getConstraintName("gen", table, columns[idx].getName(), null));
                result.append(",0 ) FROM RDB$DATABASE");
                result.append(table.getName());
                result.append(getDelimitedIdentifier(columns[idx].getName()));
            }
            return result.toString();
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
     * 
     * {@inheritDoc}
     */
    public void createExternalForeignKeys(Database database) throws IOException
    {
        for (int idx = 0; idx < database.getTableCount(); idx++)
        {
            createExternalForeignKeys(database, database.getTable(idx));
        }
    }

}
