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
import java.sql.Types;
import java.util.List;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;

/**
 * An SQL Builder for MySQL
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author John Marshall/Connectria
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision$
 */
public class MySqlBuilder extends SqlBuilder
{
    public MySqlBuilder()
    {
        setForeignKeysEmbedded(true);
        // TODO: Not yet supported:
        //setIndicesEmbedded(true);
        setCommentPrefix("#");
        addNativeTypeMapping(Types.BINARY,        "BLOB");
        addNativeTypeMapping(Types.BLOB,          "LONGBLOB");
        addNativeTypeMapping(Types.BOOLEAN,       "BIT");
        addNativeTypeMapping(Types.CLOB,          "LONGTEXT");
        addNativeTypeMapping(Types.LONGVARBINARY, "LONGBLOB");
        addNativeTypeMapping(Types.LONGVARCHAR,   "LONGTEXT");
        addNativeTypeMapping(Types.REAL,          "FLOAT");
        addNativeTypeMapping(Types.VARBINARY,     "MEDIUMBLOB");
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#getDatabaseName()
     */
    public String getDatabaseName()
    {
        return "MySQL";
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#dropTable(Table)
     */
    public void dropTable(Table table) throws IOException
    { 
        print("DROP TABLE IF EXISTS ");
        print(getTableName(table));
        printEndOfStatement();
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#printAutoIncrementColumn(Table,Column)
     */
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        print("AUTO_INCREMENT");
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#shouldGeneratePrimaryKeys(List)
     */
    protected boolean shouldGeneratePrimaryKeys(List primaryKeyColumns)
    {
        /*
         * mySQL requires primary key indication for autoincrement key columns
         * I'm not sure why the default skips the pk statement if all are identity
         */
        return true;
    }
}
