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
import java.util.Iterator;

import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.ForeignKey;
import org.apache.commons.sql.model.Table;

/**
 * An SQL Builder for Sybase
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.14 $
 */
public class SybaseBuilder extends SqlBuilder {
    
    public SybaseBuilder() {
        setForeignKeyConstraintsNamed(true);
    }
    
    public void dropTable(Table table) throws IOException { 
        String tableName = table.getName();

        // drop the foreign key contraints
        int counter = 1;
        for (Iterator iter = table.getForeignKeys().iterator(); iter.hasNext(); ) {
            ForeignKey key = (ForeignKey) iter.next();
            
            String constraintName = tableName + "_FK_" + counter;
            println("IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name=''" 
                + constraintName + "')"
            );
            printIndent();
            print("ALTER TABLE " + tableName + " DROP CONSTRAINT " + constraintName );
            printEndOfStatement();
        }
        
        // now drop the table
        println( "IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = '" 
            + tableName + "')" 
        );
        println( "BEGIN" );
        printIndent();
        println( "DROP TABLE " + tableName );
        print( "END" );
        printEndOfStatement();
    }

    protected void printComment(String text) throws IOException { 
        print( "/* " );
        print( text );
        println( " */" );
    }
    
    protected void printAutoIncrementColumn(Table table, Column column) throws IOException {
        //print( "AUTO_INCREMENT" );
    }
}
