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
import org.apache.commons.sql.model.Index;
import org.apache.commons.sql.model.Table;

/**
 * An SQL Builder for MS SQL
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision$
 */
public class MSSqlBuilder extends SqlBuilder
{
    public MSSqlBuilder()
    {
        setEmbeddedForeignKeysNamed(true);
        setForeignKeysEmbedded(false);
        //setCommentPrefix("#");
        addNativeTypeMapping(Types.BLOB,          "IMAGE");
        addNativeTypeMapping(Types.BOOLEAN,       "BIT");
        addNativeTypeMapping(Types.CHAR,          "NCHAR");
        addNativeTypeMapping(Types.CLOB,          "NTEXT");
        addNativeTypeMapping(Types.DATE,          "DATETIME");
        addNativeTypeMapping(Types.DOUBLE,        "FLOAT");
        addNativeTypeMapping(Types.INTEGER,       "INT");
        addNativeTypeMapping(Types.LONGVARBINARY, "IMAGE");
        addNativeTypeMapping(Types.LONGVARCHAR,   "TEXT");
        addNativeTypeMapping(Types.TIME,          "DATETIME");
        addNativeTypeMapping(Types.TIMESTAMP,     "DATETIME");
        addNativeTypeMapping(Types.VARCHAR,       "VARCHAR");
    }

    /* (non-Javadoc)
     * @see org.apache.commons.sql.builder.SqlBuilder#getDatabaseName()
     */
    public String getDatabaseName()
    {
        return "MS SQL Server";
    }

    /* (non-Javadoc)
     * @see org.apache.commons.sql.builder.SqlBuilder#dropExternalForeignKey(org.apache.commons.sql.model.Table, org.apache.commons.sql.model.ForeignKey, int)
     */
    protected void writeExternalForeignKeyDropStmt(Table table, ForeignKey foreignKey, int numKey) throws IOException
    {
        String constraintName = getConstraintName(null, table, "FK", Integer.toString(numKey));

        print("IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='");
        print(constraintName);
        println("'");
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
        String tableName = getTableName(table);

        print( "IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = '");
        print(tableName);
        println("')");
        println("BEGIN");
        println("     DECLARE @reftable nvarchar(60), @constraintname nvarchar(60)");
        println("     DECLARE refcursor CURSOR FOR");
        println("     select reftables.name tablename, cons.name constraitname");
        println("      from sysobjects tables,");
        println("           sysobjects reftables,");
        println("           sysobjects cons,");
        println("           sysreferences ref");
        println("       where tables.id = ref.rkeyid");
        println("         and cons.id = ref.constid");
        println("         and reftables.id = ref.fkeyid");
        print("         and tables.name = '");
        print(tableName);
        println("'");
        println("     OPEN refcursor");
        println("     FETCH NEXT from refcursor into @reftable, @constraintname");
        println("     while @@FETCH_STATUS = 0");
        println("     BEGIN");
        println("       exec ('alter table '+@reftable+' drop constraint '+@constraintname)");
        println("       FETCH NEXT from refcursor into @reftable, @constraintname");
        println("     END");
        println("     CLOSE refcursor");
        println("     DEALLOCATE refcursor");
        print("     DROP TABLE ");
        println(tableName);
        print("END");
        printEndOfStatement();
    }
    
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        print("IDENTITY (1,1) ");
    }

    protected boolean shouldGeneratePrimaryKeys(java.util.List primaryKeyColumns) {
        /*
         * requires primary key indication for autoincrement key columns
         * I'm not sure why the default skips the pk statement if all are identity
         */
        return primaryKeyColumns.size() > 0;
    }

    public void writeExternalIndexDropStmt(Table table, Index index) throws IOException
    {
        print("DROP INDEX ");
        print( getTableName(table) );
        print( "." );
        print( getIndexName(index) );
        printEndOfStatement();
    }

    public void writeColumnAlterStmt(Table table, Column column, boolean isNewColumn) throws IOException
    {
        writeTableAlterStmt(table);
        print(isNewColumn ? "ADD " : "ALTER COLUMN ");
        writeColumn(table, column);
        printEndOfStatement();
    }
}
