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
 * An SQL Builder for DB2.
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 1.14 $
 */
public class Db2Builder extends SqlBuilder
{
    public Db2Builder()
    {
        setPrimaryKeyEmbedded(false);
        setEmbeddedForeignKeysNamed(true);
        addNativeTypeMapping(Types.BINARY,        "CHAR FOR BIT DATA");
        addNativeTypeMapping(Types.BIT,           "DECIMAL(1,0)");
        addNativeTypeMapping(Types.BLOB,          "BLOB");
        addNativeTypeMapping(Types.BOOLEAN,       "DECIMAL(1,0)");
        addNativeTypeMapping(Types.FLOAT,         "FLOAT");
        addNativeTypeMapping(Types.LONGVARBINARY, "LONG VARCHAR FOR BIT DATA");
        addNativeTypeMapping(Types.LONGVARCHAR,   "LONG VARCHAR");
        addNativeTypeMapping(Types.TINYINT,       "SMALLINT");
        addNativeTypeMapping(Types.VARBINARY,     "VARCHAR FOR BIT DATA");
    }

    public void dropTable(Table table) throws IOException
    { 
        super.dropTable(table);
        print("DROP SEQUENCE IF EXISTS ");
        print(table.getName());
        print(".SequenceName");
        printEndOfStatement();
    }
    
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        print("GENERATED ALWAYS AS IDENTITY");
    }
}
