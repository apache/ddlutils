package org.apache.ddlutils.platform;

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

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

/**
 * The SQL Builder for PostgresSql.
 * 
 * @author John Thorhauer
 * @author Thomas Dudziak
 * @version $Revision$
 */
public class PostgreSqlBuilder extends SqlBuilder
{
    /**
     * Creates a new builder instance.
     * 
     * @param info The platform info
     */
    public PostgreSqlBuilder(PlatformInfo info)
    {
        super(info);
    }

    /**
     * {@inheritDoc}
     */
    public void dropTable(Table table) throws IOException
    { 
        print("DROP TABLE ");
        printIdentifier(getTableName(table));
        print(" CASCADE");
        printEndOfStatement();

        Column[] columns = table.getAutoIncrementColumns();

        for (int idx = 0; idx < columns.length; idx++)
        {
            print("DROP SEQUENCE ");
            printIdentifier(getConstraintName(null, table, columns[idx].getName(), "seq"));
            printEndOfStatement();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createTable(Database database, Table table) throws IOException
    {
        for (int idx = 0; idx < table.getColumnCount(); idx++)
        {
            Column column = table.getColumn(idx);

            if (column.isAutoIncrement())
            {
                createAutoIncrementSequence(table, column);
            }
        }
        super.createTable(database, table);
    }

    /**
     * Creates the auto-increment sequence that is then used in the column.
     *  
     * @param table  The table
     * @param column The column
     */
    private void createAutoIncrementSequence(Table table, Column column) throws IOException
    {
        print("CREATE SEQUENCE ");
        printIdentifier(getConstraintName(null, table, column.getName(), "seq"));
        printEndOfStatement();
    }

    /**
     * {@inheritDoc}
     */
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        print("UNIQUE DEFAULT nextval(");
        printIdentifier(getConstraintName(null, table, column.getName(), "seq"));
        print(")");
    }

    /**
     * {@inheritDoc}
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
    
            result.append("SELECT ");
            for (int idx = 0; idx < columns.length; idx++)
            {
                if (idx > 0)
                {
                    result.append(", ");
                }
                result.append("CURRVAL(");
                result.append(getDelimitedIdentifier(getConstraintName(null, table, columns[idx].getName(), "seq")));
                result.append(") AS ");
                result.append(getDelimitedIdentifier(columns[idx].getName()));
            }
            return result.toString();
        }
    }
}
