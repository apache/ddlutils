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

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

/**
 * The SQL Builder for PostgresSql.
 * 
 * @author <a href="mailto:john@zenplex.com">John Thorhauer</a>
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
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

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#dropTable(Table)
     */
    public void dropTable(Table table) throws IOException
    { 
        print("DROP TABLE ");
        print(getTableName(table));
        print(" CASCADE");
        printEndOfStatement();
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#createTable(org.apache.ddlutils.model.Database, org.apache.ddlutils.model.Table)
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
        print(getConstraintName(null, table, column.getName(), "seq"));
        printEndOfStatement();
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#writeColumnAutoIncrementStmt(org.apache.ddlutils.model.Table, org.apache.ddlutils.model.Column)
     */
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        print("UNIQUE DEFAULT nextval('");
        print(getConstraintName(null, table, column.getName(), "seq"));
        print("')");
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#getSelectLastInsertId(org.apache.ddlutils.model.Table)
     */
    public String getSelectLastInsertId(Table table)
    {
        Column autoIncrColumn = table.getAutoIncrementColumn();

        if (autoIncrColumn == null)
        {
            return null;
        }
        else
        {
            return "SELECT CURRVAL('" + getConstraintName(null, table, autoIncrColumn.getName(), "seq") + "') AS " + autoIncrColumn.getName();
        }
    }
}
