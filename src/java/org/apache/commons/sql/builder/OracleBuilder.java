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
public class OracleBuilder extends SqlBuilder {

    public OracleBuilder() {
        setPrimaryKeyEmbedded(false);
        setForeignKeyConstraintsNamed(true);
    }

    public void dropTable(Table table) throws IOException {
        print( "drop table " );
        print( table.getName() );
        print( " CASCADE CONSTRAINTS" );
        printEndOfStatement();
    }

    // there's no real need to print comments like this, just preserving
    // backwards compatibility with the old Torque Velocity scripts
    protected void printComment(String text) throws IOException {
        print( "--" );
        if (! text.startsWith( "-" ) ) {
            print(" ");
        }
        println( text );
    }

    public void createTable(Table table) throws IOException {
        // lets create any sequences
        Column column = table.getAutoIncrementColumn();
        if (column != null) {
            createSequence(table, column);
        }
        super.createTable(table);
        if (column != null) {
            createSequenceTrigger(table, column);
        }
    }


    protected void printAutoIncrementColumn(Table table, Column column) throws IOException {
        //print( "AUTO_INCREMENT" );
    }

    /**
     * Creates a sequence so that values can be auto incremented
     */
    protected void createSequence(Table table, Column column) throws IOException {
        print( "create sequence " );
        print( table.getName() );
        print( "_seq" );
        printEndOfStatement();
    }

    /**
     * Creates a trigger to auto-increment values
     */
    protected void createSequenceTrigger(Table table, Column column) throws IOException {
        print( "create or replace trigger " );
        print( table.getName() );
        print( "_trg before insert on " );
        println( table.getName() );
        println( "for each row" );
        println( "begin" );
        print( "select " );
        print( table.getName() );
        print( "_seq.nextval into :new." );
        print( column.getName() );
        println( " from dual;" );
        print( "end" );
        printEndOfStatement();
    }


    /**
     * @return the full SQL type string, including size where appropriate.
     * Where necessary, translate for Oracle specific DDL requirements.
     */
    protected String getSqlType(Column column) {
      switch (column.getTypeCode())
      {
        case java.sql.Types.INTEGER:
          return "INTEGER";
        case java.sql.Types.DATE:
        case java.sql.Types.TIME:
        case java.sql.Types.TIMESTAMP:
          return "DATE";
        default: return column.getType();
      }
    }
}
