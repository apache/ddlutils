package org.apache.ddlutils.platform.mysql;

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
import java.util.Iterator;
import java.util.Map;

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.SqlBuilder;

/**
 * The SQL Builder for MySQL.
 * 
 * @author James Strachan
 * @author John Marshall/Connectria
 * @author Thomas Dudziak
 * @version $Revision$
 */
public class MySqlBuilder extends SqlBuilder
{
    /**
     * Creates a new builder instance.
     * 
     * @param info The platform info
     */
    public MySqlBuilder(PlatformInfo info)
    {
        super(info);
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
     */
    public String getSelectLastInsertId(Table table)
    {
        return "SELECT LAST_INSERT_ID()";
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
     * @see org.apache.ddlutils.platform.SqlBuilder#writeExternalForeignKeyDropStmt(org.apache.ddlutils.model.Table, org.apache.ddlutils.model.ForeignKey)
     */
    protected void writeExternalForeignKeyDropStmt(Table table, ForeignKey foreignKey) throws IOException
    {
        writeTableAlterStmt(table);
        print("DROP FOREIGN KEY ");
        String foreignKeyName = foreignKey.getName();
        if (foreignKeyName == null)
        {
            foreignKeyName = getConstraintName(null, table, "FK", getForeignKeyName(table, foreignKey));
        }
        printIdentifier(foreignKeyName);
        printEndOfStatement();
    }    

}
