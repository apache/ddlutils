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

package org.apache.commons.sql.builder;

import java.io.IOException;

import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.Table;

/**
 * An SQL Builder for Oracle
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.14 $
 */
public class Db2Builder extends SqlBuilder {
    
    public Db2Builder() {
        setPrimaryKeyEmbedded(false);
        setForeignKeyConstraintsNamed(true);
    }
    
    public void dropTable(Table table) throws IOException { 
        super.dropTable(table);
        print( "drop sequence if exists  " );
        print( table.getName() );
        print( ".SequenceName" );
        printEndOfStatement();
    }
    
    protected void printAutoIncrementColumn(Table table, Column column) throws IOException {
        print( "GENERATED ALWAYS AS IDENTITY" );
    }
}
