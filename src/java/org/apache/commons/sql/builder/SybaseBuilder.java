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
import org.apache.commons.sql.model.ForeignKey;
import org.apache.commons.sql.model.Table;

/**
 * An SQL Builder for Sybase
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision$
 */
public class SybaseBuilder extends SqlBuilder
{
    public SybaseBuilder()
    {
        // For Sybase a present NULL allows it to save initial storage, so we better create it 
        setRequiringNullAsDefaultValue(true);
        setEmbeddedForeignKeysNamed(true);
        setForeignKeysEmbedded(false);
        setCommentPrefix("/*");
        setCommentSuffix("*/");
        addNativeTypeMapping(Types.BLOB,          "IMAGE");
        addNativeTypeMapping(Types.BOOLEAN,       "BIT");
        addNativeTypeMapping(Types.CLOB,          "TEXT");
        addNativeTypeMapping(Types.DOUBLE,        "DOUBLE PRECISION");
        addNativeTypeMapping(Types.LONGVARBINARY, "IMAGE");
        addNativeTypeMapping(Types.LONGVARCHAR,   "TEXT");
        addNativeTypeMapping(Types.TIMESTAMP,     "DATETIME");
    }

    /* (non-Javadoc)
     * @see org.apache.commons.sql.builder.SqlBuilder#getDatabaseName()
     */
    public String getDatabaseName()
    {
        return "Sybase";
    }

    /* (non-Javadoc)
     * @see org.apache.commons.sql.builder.SqlBuilder#dropExternalForeignKey(org.apache.commons.sql.model.Table, org.apache.commons.sql.model.ForeignKey, int)
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
     * @see org.apache.commons.sql.builder.SqlBuilder#dropTable(org.apache.commons.sql.model.Table)
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
        //print( "AUTO_INCREMENT" );
    }
}
