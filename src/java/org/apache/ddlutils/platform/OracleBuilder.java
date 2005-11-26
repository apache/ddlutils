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
import java.sql.Types;
import java.util.Map;

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

/**
 * The SQL Builder for Oracle.
 *
 * @author James Strachan
 * @author Thomas Dudziak
 * @version $Revision$
 */
public class OracleBuilder extends SqlBuilder
{
    /**
     * Creates a new builder instance.
     * 
     * @param info The platform info
     */
    public OracleBuilder(PlatformInfo info)
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
        print(" CASCADE CONSTRAINTS");
        printEndOfStatement();

        Column[] columns = table.getAutoIncrementColumns();

        for (int idx = 0; idx < columns.length; idx++)
        {
            print("DROP TRIGGER ");
            printIdentifier(getConstraintName("trg", table, columns[idx].getName(), null));
            printEndOfStatement();
            print("DROP SEQUENCE ");
            printIdentifier(getConstraintName("seq", table, columns[idx].getName(), null));
            printEndOfStatement();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void dropExternalForeignKeys(Table table) throws IOException
    {
        // no need to as we drop the table with CASCASE CONSTRAINTS
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
            print("CREATE SEQUENCE ");
            printIdentifier(getConstraintName("seq", table, columns[idx].getName(), null));
            printEndOfStatement();
        }

        super.createTable(database, table, parameters);

        for (int idx = 0; idx < columns.length; idx++)
        {
            print("CREATE OR REPLACE TRIGGER ");
            printIdentifier(getConstraintName("trg", table, columns[idx].getName(), null));
            print(" BEFORE INSERT ON ");
            printIdentifier(getTableName(table));
            println(" FOR EACH ROW");
            println("BEGIN");
            print("SELECT ");
            printIdentifier(getConstraintName("seq", table, columns[idx].getName(), null));
            print(".nextval INTO :new.");
            printIdentifier(getColumnName(columns[idx]));
            println(" FROM dual;");
            print("END");
            printEndOfStatement();
        }
    }

    /**
     * {@inheritDoc}
     */
    protected String getSqlType(Column column)
    {
        switch (column.getTypeCode())
        {
            // we need to always specify a size for these types
            case Types.BINARY:
            case Types.VARCHAR:
                StringBuffer sqlType = new StringBuffer();

                sqlType.append(getNativeType(column));
                sqlType.append("(");
                if (column.getSize() == null)
                {
                    sqlType.append("254");
                }
                else
                {
                    sqlType.append(column.getSize());
                }
                sqlType.append(")");
                return sqlType.toString();
            default:
                return super.getSqlType(column);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        // we're using sequences instead
    }
}
