/*
 * $Header: /home/cvs/jakarta-commons-sandbox/jelly/src/java/org/apache/commons/jelly/CompilableTag.java,v 1.5 2002/05/17 15:18:12 jstrachan Exp $
 * $Revision: 1.5 $
 * $Date: 2002/05/17 15:18:12 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 * 
 * $Id: CompilableTag.java,v 1.5 2002/05/17 15:18:12 jstrachan Exp $
 */
package org.apache.commons.sql.builder;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.Database;
import org.apache.commons.sql.model.ForeignKey;
import org.apache.commons.sql.model.Index;
import org.apache.commons.sql.model.IndexColumn;
import org.apache.commons.sql.model.Reference;
import org.apache.commons.sql.model.Table;
import org.apache.commons.sql.model.TypeMap;

/**
 * This class is a collection of Strategy methods for creating the DDL required to create and drop 
 * databases and tables.
 * 
 * It is hoped that just a single implementation of this class, for each database should make creating DDL
 * for each physical database fairly straightforward.
 * 
 * An implementation of this class can always delegate down to some templating technology such as Velocity if
 * it requires. Though often that can be quite complex when attempting to reuse code across many databases.
 * Hopefully only a small amount code needs to be changed on a per database basis.
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.14 $
 */
public class SqlBuilder {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(SqlBuilder.class);
    
    /** The current Writer used to output the SQL to */
    private Writer writer;
    
    /** A counter used to count the constraints */
    private int counter;
    
    /** The indentation used to indent commands */
    private String indent = "    ";
    
    /** Whether or not primary key constraints are embedded inside the create table statement */
    private boolean primaryKeyEmbedded = true;
    
    /** Whether or not foreign key constraints are embedded inside the create table statement */
    private boolean foreignKeysEmbedded;

    /** Whether or not indexes are embedded inside the create table statement */
    private boolean indexesEmbedded;

    /** Should foreign key constraints be explicitly named */
    private boolean foreignKeyConstraintsNamed;

    /** The current Table we're working on */
    protected Table table;
    
    /** The current Column we're working on */
    protected Column column;
        
    public SqlBuilder() {
    }

    /**
     * Outputs the DDL required to drop and recreate the database 
     */
    public void createDatabase(Database database) throws IOException {
        createDatabase(database, true);
    }

    /**
     * Outputs the DDL required to drop and recreate the database 
     */
    public void createDatabase(Database database, boolean dropTable) throws IOException {
        
        // lets drop the tables in reverse order as its less likely to cause
        // problems with referential constraints
        
        if (dropTable) {
            List tables = database.getTables();
            for (int i = tables.size() - 1; i >= 0; i-- ) {
                table = (Table) tables.get(i);
                dropTable(table);
            }
        }
            
        for (Iterator iter = database.getTables().iterator(); iter.hasNext(); ) {
            table = (Table) iter.next();
            tableComment(table);
            createTable(table);
        }
    }

    /**
     * Outputs the DDL required to drop the database 
     */
    public void dropDatabase(Database database) throws IOException {
        
        // lets drop the tables in reverse order
        List tables = database.getTables();
        for (int i = tables.size() - 1; i >= 0; i-- ) {
            table = (Table) tables.get(i);
            tableComment(table);
            dropTable(table);
        }
    }

    /** 
     * Outputs a comment for the table
     */
    public void tableComment(Table table) throws IOException {
        printComment("-----------------------------------------------------------------------");
        printComment(table.getName());
        printComment("-----------------------------------------------------------------------");
        println();
    }

    /**
     * Outputs the DDL to drop the table
     */
    public void dropTable(Table table) throws IOException {
        print("drop table ");
        print(table.getName());
        printEndOfStatement();
    }

    /** 
     * Outputs the DDL to create the table along with any constraints
     */
    public void createTable(Table table) throws IOException {
        this.table = table;
        print("create table ");
        println(table.getName());
        println("(");

        writeColumnTypes(table);

        if (isPrimaryKeyEmbedded()) {
            writePrimaryKeys(table);
        }
        if (isForeignKeysEmbedded()) {
            writeForeignKeys(table);
        }
        if (isIndexesEmbedded()) {
            writeEmbeddedIndexes(table);
        }
        println();
        print(")");
        printEndOfStatement();

        if (!isPrimaryKeyEmbedded()) {
            writePrimaryKeysAlterTable(table);
        }
        if (!isForeignKeysEmbedded()) {
            writeForeignKeysAlterTable(table);
        }
        if (!isIndexesEmbedded()) {
            writeIndexes(table);
        }
    }

    /** 
     * Outputs the DDL to add a column to a table.
     */
    public void createColumn(Column column) throws IOException {
        this.column = column;
        print(column.getName());
        print(" ");
        print(getSqlType(column));
        print(" ");

        if (column.getDefaultValue() != null)
        {
          print("DEFAULT '" + column.getDefaultValue() + "' ");
        }
        if (column.isRequired()) {
            printNotNullable();
        }
        else {
            printNullable();
        }
        print(" ");
        if (column.isAutoIncrement()) {
            printAutoIncrementColumn();
        }
    }
    

    // Properties
    //-------------------------------------------------------------------------                

    /**
     * @return the Writer used to print the DDL to
     */
    public Writer getWriter() {
        return writer;
    }

    /**
     * Sets the writer used to print the DDL to
     */
    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    /** 
     * @return the indentation used to indent the SQL
     */
    public String getIndent() {
        return indent;
    }

    /**
     * Sets the indentation used to indent the SQL
     */
    public void setIndent(String indent) {
        this.indent = indent;
    }

    /**
     * @return whether the primary key constraint is embedded in the create 
     * table clause or as a seperate alter table.
     * The default is true.
     */
    public boolean isPrimaryKeyEmbedded() {
        return primaryKeyEmbedded;
    }

    /**
     * Sets whether the primary key constraint is embedded in the create 
     * table clause or as a seperate alter table.
     * The default is true.
     */
    public void setPrimaryKeyEmbedded(boolean primaryKeyEmbedded) {
        this.primaryKeyEmbedded = primaryKeyEmbedded;
    }

    /**
     * @return whether the foreign key constraints are embedded in the create 
     * table clause or as a seperate alter table statements.
     * The default is false.
     */
    public boolean isForeignKeysEmbedded() {
        return foreignKeysEmbedded;
    }

    /**
     * Sets whether the foreign key constraints are embedded in the create 
     * table clause or as a seperate alter table statements.
     * The default is false.
     */
    public void setForeignKeysEmbedded(boolean foreignKeysEmbedded) {
        this.foreignKeysEmbedded = foreignKeysEmbedded;
    }

    /**
     * @return whether the indexes are embedded in the create 
     * table clause or as seperate statements.
     * The default is false.
     */
    public boolean isIndexesEmbedded() {
        return indexesEmbedded;
    }

    /**
     * Sets whether the indexes are embedded in the create 
     * table clause or as seperate statements.
     * The default is false.
     */
    public void setIndexesEmbedded(boolean indexesEmbedded) {
        this.indexesEmbedded = indexesEmbedded;
    }

    /**
     * Returns whether foreign key constraints should be named when they are embedded inside
     * a create table clause.
     * @return boolean
     */
    public boolean isForeignKeyConstraintsNamed() {
        return foreignKeyConstraintsNamed;
    }

    /**
     * Sets whether foreign key constraints should be named when they are embedded inside
     * a create table clause.
     * @param foreignKeyConstraintsNamed The foreignKeyConstraintsNamed to set
     */
    public void setForeignKeyConstraintsNamed(boolean foreignKeyConstraintsNamed) {
        this.foreignKeyConstraintsNamed = foreignKeyConstraintsNamed;
    }


    // Implementation methods
    //-------------------------------------------------------------------------                

    /**
     * @return the current Table we're working on
     */
    protected Table getTable() {
        return table;
    }
    
    /**
     * @return the current Column we're working on
     */
    protected Column getColumn() {
        return column;
    }
    
    /**
     * @return true if we should generate a primary key constraint for the given
     *  primary key columns. By default if there are no primary keys or the column(s) are 
     *  all auto increment (identity) columns then there is no need to generate a primary key 
     *  constraint.
     */
    protected boolean shouldGeneratePrimaryKeys(List primaryKeyColumns) {
        for (Iterator iter = primaryKeyColumns.iterator(); iter.hasNext(); ) {
            Column column = (Column) iter.next();
            if (! column.isAutoIncrement()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the full SQL type string including the size
     */
    protected String getSqlType(Column column) {
        StringBuffer sqlType = new StringBuffer(getNativeType(column));
        if ( column.getSize() > 0 ) {
            sqlType.append(" (");
            sqlType.append(column.getSize());
            if ( TypeMap.isDecimalType(column.getType()) ){
                sqlType.append(",");
                sqlType.append(column.getScale());
            }
            sqlType.append(")");
        }
        return sqlType.toString();
    }
    
    /**
     * Writes the column types for a table 
     */
    protected void writeColumnTypes(Table table) throws IOException {
        boolean first = true;
        for (Iterator iter = table.getColumns().iterator(); iter.hasNext();) {
            Column column = (Column) iter.next();
            if (first) {
                first = false;
            }
            else {
                println(",");
            }
            printIndent();
            createColumn(column);
        }
    }

    /**
     * Writes the primary key constraints inside a create table () clause.
     */
    protected void writePrimaryKeys(Table table) throws IOException {
        List primaryKeyColumns = table.getPrimaryKeyColumns();
        if (primaryKeyColumns.size() > 0 && shouldGeneratePrimaryKeys(primaryKeyColumns)) {
            println(",");
            printIndent();
            writePrimaryKeyStatement(primaryKeyColumns);
        }
    }

    /**
     * Writes the primary key constraints as an AlterTable clause.
     */
    protected void writePrimaryKeysAlterTable(Table table) throws IOException {
        List primaryKeyColumns = table.getPrimaryKeyColumns();
        if (primaryKeyColumns.size() > 0 && shouldGeneratePrimaryKeys(primaryKeyColumns)) {
            print("ALTER TABLE ");
            println(table.getName());

            printIndent();
            print("ADD CONSTRAINT ");
            print(table.getName());
            println("_PK");
            writePrimaryKeyStatement(primaryKeyColumns);
            printEndOfStatement();
            println();
        }
    }

    /**
     * Writes the 'PRIMARY KEY(A,B,...,N)' statement
     */
    protected void writePrimaryKeyStatement(List primaryKeyColumns)
        throws IOException {
        print("PRIMARY KEY (");

        boolean first = true;
        for (Iterator iter = primaryKeyColumns.iterator(); iter.hasNext();) {
            Column column = (Column) iter.next();
            if (first) {
                first = false;
            }
            else {
                print(", ");
            }
            print(column.getName());
        }
        print(")");
    }

    /**
     * Writes the foreign key constraints inside a create table () clause.
     */
    protected void writeForeignKeys(Table table) throws IOException {
        for (Iterator keyIter = table.getForeignKeys().iterator();
            keyIter.hasNext();
            ) {
            ForeignKey key = (ForeignKey) keyIter.next();
            if (key.getForeignTable() == null) {
                log.warn( "Foreign key table is null for key: " + key);
            }
            else {
                println(",");

                printIndent();
                
                if (isForeignKeyConstraintsNamed()) {
                    print("CONSTRAINT ");
                    print(table.getName());
                    print("_FK_");
                    print(Integer.toString(++counter));
                    print(" " );
                }
                print("FOREIGN KEY (");
                writeLocalReferences(key);
                println(")");

                printIndent();
                print("REFERENCES ");
                print(key.getForeignTable());
                print(" (");
                writeForeignReferences(key);
                println(")");
            }
        }
    }

    /**
     * Writes the foreign key constraints as an AlterTable clause.
     */
    protected void writeForeignKeysAlterTable(Table table) throws IOException {
        counter = 0;
        for (Iterator keyIter = table.getForeignKeys().iterator();
            keyIter.hasNext();
            ) {
            ForeignKey key = (ForeignKey) keyIter.next();
            if (key.getForeignTable() == null) {
                log.warn( "Foreign key table is null for key: " + key);
            }
            else {
                print("ALTER TABLE ");
                println(table.getName());

                printIndent();
                print("ADD CONSTRAINT ");
                print(table.getName());
                print("_FK_");
                print(Integer.toString(++counter));
                print(" FOREIGN KEY (");
                writeLocalReferences(key);
                println(")");

                printIndent();
                print("REFERENCES ");
                print(key.getForeignTable());
                print(" (");
                writeForeignReferences(key);
                println(")");
                printEndOfStatement();
            }
        }
    }

    /**
     * Writes the indexes.
     */
    protected void writeIndexes(Table table) throws IOException{
        for (Iterator indexIter = table.getIndexes().iterator();
            indexIter.hasNext();
            ) {
            Index index = (Index) indexIter.next();
            if (index.getName() == null) {
                log.warn( "Index Name is null for index: " + index);
            }
            else {
                print("CREATE INDEX ");
                print(index.getName());
                print(" ON ");
                print(table.getName());

                print(" (");
                
                for (Iterator idxColumnIter = index.getIndexColumns().iterator();
                    idxColumnIter.hasNext();
                    ) 
                {
                    IndexColumn idxColumn = (IndexColumn)idxColumnIter.next();
                    if (idxColumnIter.hasNext())
                    {
                        print(idxColumn.getName() + ", ");             
                    }
                    else
                    {
                        print(idxColumn.getName());
                    }
                }

                print(")");
                printEndOfStatement();
            }
        }
    }


    /**
     * Writes the indexes embedded within the create table statement. not
     * yet implemented
     */
    protected void writeEmbeddedIndexes(Table table) throws IOException 
    {
    }

    /**
     * Writes a list of local references for the givek key
     */
    protected void writeLocalReferences(ForeignKey key) throws IOException {
        boolean first = true;
        for (Iterator iter = key.getReferences().iterator(); iter.hasNext();) {
            Reference reference = (Reference) iter.next();
            if (first) {
                first = false;
            }
            else {
                print(", ");
            }
            print(reference.getLocal());
        }
    }

    /**
     * Writes a list of foreign references for the given key
     */
    protected void writeForeignReferences(ForeignKey key) throws IOException {
        boolean first = true;
        for (Iterator iter = key.getReferences().iterator(); iter.hasNext();) {
            Reference reference = (Reference) iter.next();
            if (first) {
                first = false;
            }
            else {
                print(", ");
            }
            print(reference.getForeign());
        }
    }
   
   
    /**
     * Prints an SQL comment to the current stream
     */
    protected void printComment(String text) throws IOException { 
        print( "--" );
        
       // MySql insists on a space after the first 2 dashes.
       // http://www.mysql.com/documentation/mysql/bychapter/manual_Reference.html#Comments
       // dunno if this is a common thing
        print(" ");
        println( text );
    }

    /**
     * Prints that a column is nullable 
     */
    protected void printNullable() throws IOException {
        print("NULL");
    }
    
    /**
     * Prints that a column is not nullable
     */
    protected void printNotNullable() throws IOException {
        print("NOT NULL");
    }
    
    /** 
     * Prints the end of statement text, which is typically a semi colon followed by 
     * a carriage return
     */
    protected void printEndOfStatement() throws IOException {
        println(";");
        println();
    }

    /** 
     * Prints a new line
     */
    protected void println() throws IOException {
        print("\n");
    }

    /**
     * Prints some text
     */
    protected void print(String text) throws IOException {
        writer.write(text);
    }

    /**
     * Prints some text then a newline
     */
    protected void println(String text) throws IOException {
        print(text);
        println();
    }

    /**
     * Prints the indentation used to indent SQL
     */
    protected void printIndent() throws IOException {
        print(getIndent());
    }

    /**
     * Outputs the fact that this column is an auto increment column.
     */ 
    protected void printAutoIncrementColumn() throws IOException {
        print( "IDENTITY" );
    }

    protected String getNativeType(Column column){
        return column.getType();
    }

}
