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
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Table;

/**
 * An SQL Builder for Sybase
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision$
 */
public class SybaseBuilder extends SqlBuilder
{
    /** Database name of this builder */
    public static final String DATABASENAME     = "Sybase";
    /** The standard Sybase jdbc driver */
    public static final String JDBC_DRIVER      = "com.sybase.jdbc2.jdbc.SybDriver";
    /** The old Sybase jdbc driver */
    public static final String JDBC_DRIVER_OLD  = "com.sybase.jdbc.SybDriver";
    /** The subprotocol used by the standard Sybase driver */
    public static final String JDBC_SUBPROTOCOL = "sybase:Tds";

    public SybaseBuilder()
    {
        setMaxIdentifierLength(30);
        setRequiringNullAsDefaultValue(false);
        setPrimaryKeyEmbedded(true);
        setForeignKeysEmbedded(false);
        setIndicesEmbedded(false);
        setCommentPrefix("/*");
        setCommentSuffix("*/");

        addNativeTypeMapping(Types.ARRAY,         "IMAGE");
        addNativeTypeMapping(Types.BIGINT,        "DECIMAL(19,0)");
        addNativeTypeMapping(Types.BLOB,          "IMAGE");
        addNativeTypeMapping(Types.CLOB,          "TEXT");
        addNativeTypeMapping(Types.DATE,          "DATETIME");
        addNativeTypeMapping(Types.DISTINCT,      "IMAGE");
        addNativeTypeMapping(Types.DOUBLE,        "DOUBLE PRECISION");
        addNativeTypeMapping(Types.FLOAT,         "DOUBLE PRECISION");
        addNativeTypeMapping(Types.INTEGER,       "INT");
        addNativeTypeMapping(Types.JAVA_OBJECT,   "IMAGE");
        addNativeTypeMapping(Types.LONGVARBINARY, "IMAGE");
        addNativeTypeMapping(Types.LONGVARCHAR,   "TEXT");
        addNativeTypeMapping(Types.NULL,          "IMAGE");
        addNativeTypeMapping(Types.OTHER,         "IMAGE");
        addNativeTypeMapping(Types.REF,           "IMAGE");
        addNativeTypeMapping(Types.STRUCT,        "IMAGE");
        addNativeTypeMapping(Types.TIME,          "DATETIME");
        addNativeTypeMapping(Types.TIMESTAMP,     "DATETIME");
        addNativeTypeMapping(Types.TINYINT,       "SMALLINT");

        // These types are only available since 1.4 so we're using the safe mapping method
        addNativeTypeMapping("BOOLEAN",  "BIT");
        addNativeTypeMapping("DATALINK", "IMAGE");
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#getDatabaseName()
     */
    public String getDatabaseName()
    {
        return DATABASENAME;
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#dropExternalForeignKey(org.apache.ddlutils.model.Table, org.apache.ddlutils.model.ForeignKey, int)
     */
    protected void writeExternalForeignKeyDropStmt(Table table, ForeignKey foreignKey, int numKey) throws IOException
    {
        String constraintName = getConstraintName(null, table, "FK", Integer.toString(numKey));

        print("IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name=''");
        print(constraintName);
        println("')");
        printIndent();
        print("ALTER TABLE ");
        print(getTableName(table));
        print(" DROP CONSTRAINT ");
        print(constraintName);
        printEndOfStatement();
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.SqlBuilder#dropTable(org.apache.ddlutils.model.Table)
     */
    public void dropTable(Table table) throws IOException
    { 
        print("IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = '");
        print(getTableName(table));
        println("')");
        println("BEGIN");
        printIndent();
        print("DROP TABLE ");
        println(getTableName(table));
        print("END");
        printEndOfStatement();
    }

    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        print("IDENTITY");
    }
}
