package org.apache.commons.sql.builder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.Database;
import org.apache.commons.sql.model.ForeignKey;
import org.apache.commons.sql.model.Reference;
import org.apache.commons.sql.model.Table;

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
    
    protected PrintWriter writer;
    
    /** used for generating sequential constraints */
    protected int counter;
    
    public SqlBuilder() {
    }
    
    
    /**
     * Outputs the DDL required to drop and recreate the database 
     */
    public void createDatabase(Database database) throws IOException {
        for ( Iterator iter = database.getTables().iterator(); iter.hasNext(); ) {
            Table table = (Table) iter.next();
            tableComment(table);
            dropTable(table);
            createTable(table);
        }
    }

    /**
     * Outputs the DDL required to drop the database 
     */
    public void dropDatabase(Database database) throws IOException {
        for ( Iterator iter = database.getTables().iterator(); iter.hasNext(); ) {
            Table table = (Table) iter.next();
            tableComment(table);
            dropTable(table);
        }
    }

    /** 
     * Outputs a comment for the table
     */    
    public void tableComment(Table table) throws IOException { 
        writeComment(  "-----------------------------------------------------------------------" );
        writeComment(  table.getName() );
        writeComment(  "-----------------------------------------------------------------------" );
        writer.println();
    }
    

    /**
     * Outputs the DDL to drop the table
     */
    public void dropTable(Table table) throws IOException { 
        writer.write( "drop table " );
        writer.write( table.getName() );
        writeEndOfStatement();
    }
    
    /** 
     * Outputs the DDL to create the table along with any constraints
     */
    public void createTable(Table table) throws IOException { 
        writer.write( "create table " );
        writer.write( table.getName() );
        writer.println( " (" );

        writeColumnTypes(table);
        
        if (isPrimaryKeyEmbedded()) {
            writePrimaryKeys(table);
        }
        if (isForeignKeysEmbedded()) {
            writeForeignKeys(table);
        }
        writer.println();
        writer.write( ")" );
        writeEndOfStatement();
        
        if (! isPrimaryKeyEmbedded()) {
            writePrimaryKeysAlterTable(table);
        }
        if (! isForeignKeysEmbedded()) {
            writeForeignKeysAlterTable(table);
        }
    }
    
    /** 
     * Outputs the DDL to add a column to a table.
     */
    public void createColumn(Column column) throws IOException {
        writer.write( column.getName() );
        writer.write( " " );
        writer.write( getSqlType( column ) );
        writer.write( column.getType() );
        writer.write( " " );
        if ( column.isAutoIncrement() ) {
            writeAutoIncrementColumn();
        }
        if ( column.isRequired() ) {
            writer.write( "NOT NULL" );
        }
        else {
            writer.write( "NULL" );
        }
    }


    // Properties
    //-------------------------------------------------------------------------                
        
    /**
     * Returns the writer.
     * @return PrintWriter
     */
    public PrintWriter getWriter() {
        return writer;
    }

    /**
     * Sets the writer.
     * @param writer The writer to set
     */
    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }


    // Implementation methods
    //-------------------------------------------------------------------------                
        
    
    /**
     * @return the full SQL type string including the size
     */
    protected String getSqlType(Column column) {
        return column.getTypeString();
    }

    /**
     * Writes the column types for a table 
     */    
    protected void writeColumnTypes(Table table) throws IOException {
        boolean first = true;        
        for (Iterator iter = table.getColumns().iterator(); iter.hasNext(); ) {
            Column column = (Column) iter.next();
            if (first) {
                first = false;
            }
            else {
                writer.println(",");
            }
            writer.write( "  " );
            createColumn(column);
        }
    }
    
    /**
     * Writes the primary key constraints inside a create table () clause.
     */    
    protected void writePrimaryKeys(Table table) throws IOException {
        List primaryKeyColumns = table.getPrimaryKeyColumns();
        if ( primaryKeyColumns.size() > 0 ) {
            writer.println( "," );
            writePrimaryKeyStatement(primaryKeyColumns);
        }
    }
    
    /**
     * Writes the primary key constraints as an AlterTable clause.
     */    
    protected void writePrimaryKeysAlterTable(Table table) throws IOException {
        List primaryKeyColumns = table.getPrimaryKeyColumns();
        if ( primaryKeyColumns.size() > 0 ) {
            writer.write( "ALTER TABLE " );
            writer.println( table.getName() );
            writer.write( "    ADD CONSTRAINT " );
            writer.write( table.getName() );
            writer.println( "_PK" );
            writePrimaryKeyStatement(primaryKeyColumns);
            writeEndOfStatement();
            writer.println();
        }
    }

    /**
     * Writes the 'PRIMARY KEY(A,B,...,N)' statement
     */
    protected void writePrimaryKeyStatement(List primaryKeyColumns) throws IOException {
        writer.write( "PRIMARY KEY (" );
        
        boolean first = true;
        for (Iterator iter = primaryKeyColumns.iterator(); iter.hasNext(); ) {
            Column column = (Column) iter.next();
            if (first) {
                first = false;
            }
            else {
                writer.write(", " );
            }
            writer.write(column.getName());
        }
        writer.write( ")" );
    }
    
    /**
     * Writes the foreign key constraints inside a create table () clause.
     */    
    protected void writeForeignKeys(Table table) throws IOException {
        for (Iterator keyIter = table.getForeignKeys().iterator(); keyIter.hasNext(); ) {
            ForeignKey key = (ForeignKey) keyIter.next();
            if (key.getForeignTable() == null) {
                System.err.println( "WARN: foreign key table is null for key: " + key );
            }
            else {
                writer.println( "," );
    
                writer.write( "  CONSTRAINT " );
                writer.write( table.getName() );
                writer.write( "_FK_" );
                writer.write( Integer.toString(++counter) );
                writer.write( " FOREIGN KEY (" );            
                writeLocalReferences(key);            
                writer.write( key.getForeignTable() );
                writer.println( ")" );
                
                writer.write( "  REFERENCES " );
                writer.write( key.getForeignTable() );
                writer.write( " (" );
                writeForeignReferences(key);
                writer.println( ")" );
            }
        }
    }

    /**
     * Writes the foreign key constraints as an AlterTable clause.
     */    
    protected void writeForeignKeysAlterTable(Table table) throws IOException {
        counter = 0;
        for (Iterator keyIter = table.getForeignKeys().iterator(); keyIter.hasNext(); ) {
            ForeignKey key = (ForeignKey) keyIter.next();
            if (key.getForeignTable() == null) {
                System.err.println( "WARN: foreign key table is null for key: " + key );
            }
            else {
                writer.write( "ALTER TABLE " );
                writer.println( table.getName() );
                
                writer.write( "  ADD CONSTRAINT " );
                writer.write( table.getName() );
                writer.write( "_FK_" );
                writer.write( Integer.toString(++counter) );
                writer.write( " FOREIGN KEY (" );            
                writeLocalReferences(key);            
                writer.println( ")" );
                
                writer.write( "  REFERENCES " );
                writer.write( key.getForeignTable() );
                writer.write( " (" );
                writeForeignReferences(key);
                writer.println( ")" );
                writeEndOfStatement();
            }
        }
    }

    /**
     * Writes a list of local references for the givek key
     */
    protected void writeLocalReferences(ForeignKey key) throws IOException {
        boolean first = true;
        for (Iterator iter = key.getReferences().iterator(); iter.hasNext(); ) {
            Reference reference = (Reference) iter.next();
            if (first) {
                first = false;
            }
            else {
                writer.write( ", " );
            }
            writer.write( reference.getLocal() );
        }
    }
            
    /**
     * Writes a list of foreign references for the given key
     */
    protected void writeForeignReferences(ForeignKey key) throws IOException {
        boolean first = true;
        for (Iterator iter = key.getReferences().iterator(); iter.hasNext(); ) {
            Reference reference = (Reference) iter.next();
            if (first) {
                first = false;
            }
            else {
                writer.write( ", " );
            }
            writer.write( reference.getForeign() );
        }
    }
            
    
    /** 
     * Writes the end of statement text, which is typically a semi colon followed by 
     * a carriage return
     */
    protected void writeEndOfStatement() throws IOException {
        writer.println( ";" );
        writer.println();
    }


    /**
     * Write a comment to the DDL stream
     */    
    protected void writeComment(String text) throws IOException { 
        writer.write( "# " );
        writer.println( text );
    }
    
    /**
     * Outputs that the current column is an auto increment
     */
    protected void writeAutoIncrementColumn() throws IOException { 
    }
    
    /**
     * Should the primary key constraints be embedded inside the create table statement
     */
    protected boolean isPrimaryKeyEmbedded() {
        return false;
    }
    
    /**
     * Should the foreign key constraints be embedded inside the create table statement
     */
    protected boolean isForeignKeysEmbedded() {
        return false;
    }


}
