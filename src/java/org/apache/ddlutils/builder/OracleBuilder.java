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

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

/**
 * An SQL Builder for Oracle.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision$
 */
public class OracleBuilder extends SqlBuilder
{
    /** Database name of this builder */
    public static final String DATABASENAME = "Oracle";

    public OracleBuilder()
    {
        setMaxIdentifierLength(30);
        setRequiringNullAsDefaultValue(false);
        setPrimaryKeyEmbedded(true);
        setForeignKeysEmbedded(false);
        setIndicesEmbedded(false);

        addNativeTypeMapping(Types.ARRAY,         "BLOB");
        addNativeTypeMapping(Types.BIGINT,        "NUMBER(38,0)");
        addNativeTypeMapping(Types.BINARY,        "RAW");
        addNativeTypeMapping(Types.BIT,           "NUMBER(1,0)");
        addNativeTypeMapping(Types.DECIMAL,       "NUMBER");
        addNativeTypeMapping(Types.DISTINCT,      "BLOB");
        addNativeTypeMapping(Types.DOUBLE,        "NUMBER(38)");
        addNativeTypeMapping(Types.FLOAT,         "NUMBER(38)");
        addNativeTypeMapping(Types.INTEGER,       "NUMBER(20,0)");
        addNativeTypeMapping(Types.JAVA_OBJECT,   "BLOB");
        addNativeTypeMapping(Types.LONGVARBINARY, "BLOB");
        addNativeTypeMapping(Types.LONGVARCHAR,   "CLOB");
        addNativeTypeMapping(Types.NULL,          "BLOB");
        addNativeTypeMapping(Types.NUMERIC,       "NUMBER");
        addNativeTypeMapping(Types.OTHER,         "BLOB");
        addNativeTypeMapping(Types.REAL,          "NUMBER(18)");
        addNativeTypeMapping(Types.REF,           "BLOB");
        addNativeTypeMapping(Types.SMALLINT,      "NUMBER(5,0)");
        addNativeTypeMapping(Types.STRUCT,        "BLOB");
        addNativeTypeMapping(Types.TIME,          "DATE");
        // TODO: This should really be controlled by an option since beginning with Oracle 9,
        //       the special TIMESTAMP type should be used
        addNativeTypeMapping(Types.TIMESTAMP,     "DATE");
        addNativeTypeMapping(Types.TINYINT,       "NUMBER(3,0)");
        addNativeTypeMapping(Types.VARBINARY,     "RAW");
        addNativeTypeMapping(Types.VARCHAR,       "VARCHAR2");

        // These types are only available since 1.4 so we're using the safe mapping method
        addNativeTypeMapping("BOOLEAN",  "NUMBER(1,0)");
        addNativeTypeMapping("DATALINK", "BLOB");
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#getDatabaseName()
     */
    public String getDatabaseName()
    {
        return DATABASENAME;
    }

    public void dropTable(Table table) throws IOException
    {
        print("DROP TABLE ");
        print(getTableName(table));
        print(" CASCADE CONSTRAINTS");
        printEndOfStatement();
    }

    public void createTable(Database database, Table table) throws IOException
    {
        // lets create any sequences
        Column column = table.getAutoIncrementColumn();

        if (column != null)
        {
            createSequence(table, column);
        }
        super.createTable(database, table);
        if (column != null)
        {
            createSequenceTrigger(table, column);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#getSqlType(org.apache.ddlutils.model.Column)
     */
    protected String getSqlType(Column column)
    {
        switch (column.getTypeCode())
        {
            // we need to always specify a size for these types
            case Types.BINARY:
            case Types.VARCHAR:
                String result = super.getSqlType(column);

                if (column.getSize() == null)
                {
                    result += "(254)";
                }
                return result;
            default:
                return super.getSqlType(column);
        }
    }

    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
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
        print(getConstraintName(null, table, "seq", null));
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
        print(getConstraintName(null, table, "trg", null));
        print(" BEFORE INSERT ON ");
        println(getTableName(table));
        println("FOR EACH ROW");
        println("BEGIN");
        print("SELECT ");
        print(getConstraintName(null, table, "seq", null));
        print(".nextval INTO :new.");
        print(getColumnName(column));
        println(" FROM dual;");
        print("END");
        printEndOfStatement();
    }
}
