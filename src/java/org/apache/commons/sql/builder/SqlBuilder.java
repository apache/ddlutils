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

    public SqlBuilder() {
    }

    /**
     * Outputs the DDL required to drop and recreate the database 
     */
    public void createDatabase(Database database) throws IOException {
        for (Iterator iter = database.getTables().iterator(); iter.hasNext(); ) {
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
        for (Iterator iter = database.getTables().iterator(); iter.hasNext(); ) {
            Table table = (Table) iter.next();
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
        println();
        print(")");
        printEndOfStatement();

        if (!isPrimaryKeyEmbedded()) {
            writePrimaryKeysAlterTable(table);
        }
        if (!isForeignKeysEmbedded()) {
            writeForeignKeysAlterTable(table);
        }
    }

    /** 
     * Outputs the DDL to add a column to a table.
     */
    public void createColumn(Column column) throws IOException {
        print(column.getName());
        print(" ");
        print(getSqlType(column));
        print(" ");
        if (column.isRequired()) {
            print("NOT NULL");
        }
        else {
            print("NULL");
        }
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
        if (primaryKeyColumns.size() > 0) {
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
        if (primaryKeyColumns.size() > 0) {
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
                System.err.println(
                    "WARN: foreign key table is null for key: " + key);
            }
            else {
                println(",");

                printIndent();
                print("CONSTRAINT ");
                print(table.getName());
                print("_FK_");
                print(Integer.toString(++counter));
                print(" FOREIGN KEY (");
                writeLocalReferences(key);
                print(key.getForeignTable());
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
                System.err.println(
                    "WARN: foreign key table is null for key: " + key);
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
        print("# ");
        println(text);
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
    }



}
