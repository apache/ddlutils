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
 * An SQL Builder for Oracle
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 1.14 $
 */
public class OracleBuilder extends SqlBuilder
{
    public OracleBuilder()
    {
        setPrimaryKeyEmbedded(false);
        setEmbeddedForeignKeysNamed(true);
        setForeignKeysEmbedded(false);
        addNativeTypeMapping(Types.BIGINT,        "NUMBER(38,0)");
        addNativeTypeMapping(Types.BINARY,        "BLOB");
        addNativeTypeMapping(Types.BIT,           "NUMBER(1,0)");
        addNativeTypeMapping(Types.BOOLEAN,       "NUMBER(1,0)");
        addNativeTypeMapping(Types.DECIMAL,       "NUMBER");
        addNativeTypeMapping(Types.DOUBLE,        "FLOAT");
        addNativeTypeMapping(Types.INTEGER,       "NUMBER(20)");
        addNativeTypeMapping(Types.LONGVARBINARY, "BLOB");
        addNativeTypeMapping(Types.LONGVARCHAR,   "CLOB");
        addNativeTypeMapping(Types.NUMERIC,       "NUMBER");
        addNativeTypeMapping(Types.REAL,          "FLOAT");
        addNativeTypeMapping(Types.SMALLINT,      "NUMBER(5,0)");
        addNativeTypeMapping(Types.TIME,          "DATE");
        addNativeTypeMapping(Types.TIMESTAMP,     "DATE");
        addNativeTypeMapping(Types.TINYINT,       "NUMBER(3,0)");
        addNativeTypeMapping(Types.VARBINARY,     "BLOB");
        addNativeTypeMapping(Types.VARCHAR,       "VARCHAR2");
    }

    /* (non-Javadoc)
     * @see org.apache.commons.sql.builder.SqlBuilder#getDatabaseName()
     */
    public String getDatabaseName()
    {
        return "Oracle";
    }

    public void dropTable(Table table) throws IOException
    {
        print("DROP TABLE ");
        print(table.getName());
        print(" CASCADE CONSTRAINTS");
        printEndOfStatement();
    }

    public void createTable(Table table) throws IOException
    {
        // lets create any sequences
        Column column = table.getAutoIncrementColumn();

        if (column != null)
        {
            createSequence(table, column);
        }
        super.createTable(table);
        if (column != null)
        {
            createSequenceTrigger(table, column);
        }
    }

    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        //print( "AUTO_INCREMENT" );
    }

    /**
     * Creates a sequence so that values can be auto incremented.
     * 
     * @param table  The table
     * @param column The column
     */
    protected void createSequence(Table table, Column column) throws IOException
    {
        print("CREATE SEQUENCE ");
        print(table.getName());
        print("_seq");
        printEndOfStatement();
    }

    /**
     * Creates a trigger for the auto-increment sequence.
     * 
     * @param table  The table
     * @param column The column
     */
    protected void createSequenceTrigger(Table table, Column column) throws IOException
    {
        print("CREATE OR REPLACE TRIGGER ");
        print(table.getName());
        print("_trg BEFORE INSERT ON ");
        println(table.getName());
        println("FOR EACH ROW");
        println("BEGIN");
        print("SELECT ");
        print(table.getName());
        print("_seq.nextval INTO :new.");
        print(column.getName());
        println(" FROM dual;");
        print("END");
        printEndOfStatement();
    }
}
