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
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.sql.io.JdbcModelReader;
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
 * @author John Marshall/Connectria
 * @version $Revision: 1.14 $
 */
public class SqlBuilder {

    private static final String LINE_SEP = System.getProperty( "line.separator", "\n" );

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

    /** Is an ALTER TABLE needed to drop indexes? */
    private boolean alterTableForDrop;

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
                Table table = (Table) tables.get(i);
                dropTable(table);
            }
        }
            
        for (Iterator iter = database.getTables().iterator(); iter.hasNext(); ) {
            Table table = (Table) iter.next();
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
            Table table = (Table) tables.get(i);
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
    public void createColumn(Table table, Column column) throws IOException {
        //see comments in columnsDiffer about null/"" defaults

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
            printAutoIncrementColumn(table, column);
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
            createColumn(table, column);
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
            writeForeignKeyAlterTable( table, key );
        }
    }

    protected void writeForeignKeyAlterTable( Table table, ForeignKey key ) throws IOException {
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

    /**
     * Writes the indexes.
     */
    protected void writeIndexes(Table table) throws IOException{
        for (Iterator indexIter = table.getIndexes().iterator();
            indexIter.hasNext();
            ) {
            Index index = (Index) indexIter.next();
            writeIndex( table, index );
        }
    }

    /**
     * Writes one index for a table
     */
    protected void writeIndex( Table table, Index index ) throws IOException {
        if (index.getName() == null) {
            log.warn( "Index Name is null for index: " + index);
        }
        else {
            print("CREATE");
            if ( index.isUnique() ) {
                print( " UNIQUE" );
            }
            print(" INDEX ");
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
        print( LINE_SEP );
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
    protected void printAutoIncrementColumn(Table table, Column column) throws IOException {
        print( "IDENTITY" );
    }

    protected String getNativeType(Column column){
        return column.getType();
    }


    /**
     * Generates the DDL to modify an existing database so the schema matches
     * the current specified database schema.  Drops and modifications will
     * not be made.
     *
     * @param desiredDb The desired database schema
     * @param cn A connection to the existing database that should be modified
     *
     * @throws IOException if the ddl cannot be output
     * @throws SQLException if there is an error reading the current schema
     */
    public void alterDatabase(Database desiredDb, Connection cn) throws IOException, SQLException {
        alterDatabase( desiredDb, cn, false, false );
    }

    /**
     * Generates the DDL to modify an existing database so the schema matches
     * the current specified database schema.
     *
     * @param desiredDb The desired database schema
     * @param cn A connection to the existing database that should be modified
     * @param doDrops true if columns and indexes should be dropped, false if
     *      just a message should be output
     * @param modifyColumns true if columns should be altered for datatype, size, etc.,
     *      false if just a message should be output
     *
     * @throws IOException if the ddl cannot be output
     * @throws SQLException if there is an error reading the current schema
     */
    public void alterDatabase(Database desiredDb, Connection cn, boolean doDrops, boolean modifyColumns) throws IOException, SQLException {

        Database currentDb = new JdbcModelReader(cn).getDatabase();

        for (Iterator iter = desiredDb.getTables().iterator(); iter.hasNext(); ) {
            Table desiredTable = (Table) iter.next();
            Table currentTable = currentDb.findTable( desiredTable.getName() );

//took out because if there were no changes to be made the execution had
//errors because it tries to execute the comments as a statement
//            tableComment(desiredTable);

            if ( currentTable == null ) {
                log.info( "creating table " + desiredTable.getName() );
                createTable( desiredTable );
            } else {
                //add any columns, indices, or constraints

                Iterator desiredColumns = desiredTable.getColumns().iterator();
                while ( desiredColumns.hasNext() ) {
                	Column desiredColumn = (Column) desiredColumns.next();
                	Column currentColumn = currentTable.findColumn(desiredColumn.getName());
                    if ( null == currentColumn ) {
                        log.info( "creating column " + desiredTable.getName() + "." + desiredColumn.getName() );
                        alterColumn( desiredTable, desiredColumn, true );
                    } else if ( columnsDiffer( desiredColumn, currentColumn ) ) {
                        if ( modifyColumns ) {
                            log.info( "altering column " + desiredTable.getName() + "." + desiredColumn.getName() );
                            log.info( "  desiredColumn=" + desiredColumn.toStringAll() );
                            log.info( "  currentColumn=" + currentColumn.toStringAll() );
                            alterColumn( desiredTable, desiredColumn, false );
                        } else {
                            String text = "Column " + currentColumn.getName() + " in table " + currentTable.getName() + " differs from current specification";
                            log.info( text );
                            printComment( text );
                        }
                    }
                } //for columns

                //@todo add constraints here...

                //hmm, m-w.com says indices and indexes are both okay
                //@todo should we check the index fields for differences?
                Iterator desiredIndexes = desiredTable.getIndexes().iterator();
                while ( desiredIndexes.hasNext() ) {
                    Index desiredIndex = (Index) desiredIndexes.next();
                    Index currentIndex = currentTable.findIndex(desiredIndex.getName());
                    if ( null == currentIndex ) {
                        log.info( "creating index " + desiredTable.getName() + "." + desiredIndex.getName() );
                        writeIndex( desiredTable, desiredIndex );
                    }
                }

                // Drops ///////////////////////
                //@todo drop constraints - probably need names on them for this

                //do any drops of columns
                Iterator currentColumns = currentTable.getColumns().iterator();
                while ( currentColumns.hasNext() ) {
                    Column currentColumn = (Column) currentColumns.next();
                    Column desiredColumn = desiredTable.findColumn(currentColumn.getName());
                    if ( null == desiredColumn ) {
                        if ( doDrops ) {
                            log.info( "dropping column " + currentTable.getName() + "." + currentColumn.getName() );
                            dropColumn( currentTable, currentColumn );
                        } else {
                            String text = "Column " + currentColumn.getName() + " can be dropped from table " + currentTable.getName();
                            log.info( text );
                            printComment( text );
                        }
                    }
                } //for columns

                //drop indexes
                Iterator currentIndexes = currentTable.getIndexes().iterator();
                while ( currentIndexes.hasNext() ) {
                    Index currentIndex = (Index) currentIndexes.next();
                    Index desiredIndex = desiredTable.findIndex(currentIndex.getName());
                    if ( null == desiredIndex ) {
                        //make sure this isn't the primary key index (mySQL reports this at least)

                        Iterator indexColumns = currentIndex.getIndexColumns().iterator();
                        boolean isPk = true;
                        while ( indexColumns.hasNext() ) {
                            IndexColumn ic = (IndexColumn) indexColumns.next();
                            Column c = currentTable.findColumn( ic.getName() );
                            if ( !c.isPrimaryKey() ) {
                                isPk = false;
                                break;
                            }
                        }

                        if ( !isPk ) {
                            log.info( "dropping non-primary index " + currentTable.getName() + "." + currentIndex.getName() );
                            dropIndex( currentTable, currentIndex );
                        }
                    }
                }

            } //table exists?
        } //for tables create

        //check for table drops
        for (Iterator iter = currentDb.getTables().iterator(); iter.hasNext(); ) {
            Table currentTable = (Table) iter.next();
            Table desiredTable = desiredDb.findTable( currentTable.getName() );

            if ( desiredTable == null ) {
                if ( doDrops ) {
                    log.info( "dropping table " + currentTable.getName() );
                    dropTable( currentTable );
                } else {
                    String text = "Table " + currentTable.getName() + " can be dropped";
                    log.info( text );
                    printComment( text );
                }
            }

        } //for tables drops

    }

    /**
     * Generates the alter statement to add or modify a single column on a table.
     *
     * @param table The table the index is on
     * @param column The column to drop
     * @param add true if the column is new, false if it is to be changed
     *
     * @throws IOException if the statement cannot be written
     */
    public void alterColumn( Table table, Column column, boolean add ) throws IOException {

        writeAlterHeader( table );

        print( add ? "ADD " : "MODIFY " );
        createColumn( table, column );
        printEndOfStatement();
    }

    /**
     * Generates the statement to drop an column from a table.
     *
     * @param table The table the index is on
     * @param column The column to drop
     *
     * @throws IOException if the statement cannot be written
     */
    public void dropColumn( Table table, Column column ) throws IOException {

        writeAlterHeader( table );

        print( "DROP COLUMN " );
        print( column.getName() );
        printEndOfStatement();
    }

    /**
     * Generates the first part of the ALTER TABLE statement including the
     * table name.
     *
     * @param table The table being altered
     *
     * @throws IOException if the statement cannot be written
     */
    protected void writeAlterHeader( Table table ) throws IOException {
        print("ALTER TABLE ");
        println(table.getName());

        printIndent();

    }

    /**
     * Generates the statement to drop an index from the database.  The
     * <code>alterTableForDrop</code> property is checked to determine what
     * style of drop is generated.
     *
     * @param table The table the index is on
     * @param index The index to drop
     *
     * @throws IOException if the statement cannot be written
     *
     * @see SqlBuilder#useAlterTableForDrop
     */
    public void dropIndex( Table table, Index index ) throws IOException {

        if ( useAlterTableForDrop() ) {
            writeAlterHeader( table );
        }

        print( "DROP INDEX " );
        print( index.getName() );

        if ( ! useAlterTableForDrop() ) {
            print( " ON " );
            print( table.getName() );
        }

        printEndOfStatement();
    }

    /**
     * Helper method to determine if two column specifications represent
     * different types.  Type, nullability, size, scale, default value,
     * and precision radix are the attributes checked.  Currently default
     * values are compared where null and empty string are considered equal.
     * See comments in the method body for explanation.
     *
     *
     * @param first First column to compare
     * @param second Second column to compare
     * @return true if the columns differ
     */
    protected boolean columnsDiffer( Column desired, Column current ) {
        boolean result = false;

        //The createColumn method leaves off the default clause if column.getDefaultValue()
        //is null.  mySQL interprets this as a default of "" or 0, and thus the columns
        //are always different according to this method.  alterDatabase will generate
        //an alter statement for the column, but it will be the exact same definition
        //as before.  In order to avoid this situation I am ignoring the comparison
        //if the desired default is null.  In order to "un-default" a column you'll
        //have to have a default="" or default="0" in the schema xml.
        //If this is bad for other databases, it is recommended that the createColumn
        //method use a "DEFAULT NULL" statement if that is what is needed.
        //A good way to get this would be to require a defaultValue="<NULL>" in the
        //schema xml if you really want null and not just unspecified.
        String desiredDefault = desired.getDefaultValue();
        String currentDefault = current.getDefaultValue();
        boolean defaultsEqual = desiredDefault == null ||
            desiredDefault.equals(currentDefault);

        boolean sizeMatters = desired.getSize() > 0;

        if ( desired.getTypeCode() != current.getTypeCode() ||
                desired.isRequired() != current.isRequired() ||
                (sizeMatters && desired.getSize() != current.getSize()) ||
                desired.getScale() != current.getScale() ||
                !defaultsEqual ||
                desired.getPrecisionRadix() != current.getPrecisionRadix() )
        {
            result = true;
        }

        return result;
    }

    /**
     * Whether an ALTER TABLE statement is necessary when dropping indexes
     * or constraints.  The default is false.
     * @return true if ALTER TABLE is required
     */
    public boolean useAlterTableForDrop() {
        return alterTableForDrop;
    }

    /**
     * Whether an ALTER TABLE statement is necessary when dropping indexes
     * or constraints.  The default is false.
     * @param alterTableForDrop The new value
     */
    public void setAlterTableForDrop(boolean alterTableForDrop) {
        this.alterTableForDrop = alterTableForDrop;
    }

//used to check for code to be changed when changing signatures
//protected final void printAutoIncrementColumn() throws IOException {};
//protected final void createColumn(Column column) throws IOException {};


}
