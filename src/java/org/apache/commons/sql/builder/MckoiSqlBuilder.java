package org.apache.commons.sql.builder;

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

import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.Table;

/**
 * An SQL Builder for the Mckoi database
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision$
 */
public class MckoiSqlBuilder extends SqlBuilder
{
    public MckoiSqlBuilder()
    {
        setForeignKeysEmbedded(false);
        setEmbeddedForeignKeysNamed(true);
        addNativeTypeMapping(Types.BIT,  "TINYINT");
    }

    /* (non-Javadoc)
     * @see org.apache.commons.sql.builder.SqlBuilder#getDatabaseName()
     */
    public String getDatabaseName()
    {
        return "Mckoi";
    }

    public void dropTable(Table table) throws IOException
    { 
        print("DROP TABLE IF EXISTS ");
        print(getTableName(table));
        printEndOfStatement();
    }

    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        print("DEFAULT UNIQUEKEY('");
        print(getTableName(table));
        print("')");
    }
}
