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
import java.util.ArrayList;
import java.util.HashMap;
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
 * TODO: It might be useful to add foreignkey analysis for creation/dropping of tables 
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author John Marshall/Connectria
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 1.14 $
 */
public class SqlBuilder {

    private static final String LINE_SEPERATOR = System.getProperty("line.separator", "\n");

    /** The Log to which logging calls will be made. */
    protected final Log _log = LogFactory.getLog(SqlBuilder.class);
    
    /** The current Writer used to output the SQL to */
    private Writer _writer;
    
    /** The indentation used to indent commands */
    private String _indent = "    ";

    /** Whether the database requires the explicit stating of NULL as the default value */
    private boolean _requiringNullAsDefaultValue = false;

    /** Whether primary key constraints are embedded inside the create table statement */
    private boolean _primaryKeyEmbedded = true;
    
    /** Whether foreign key constraints are embedded inside the create table statement */
    private boolean _foreignKeysEmbedded = false;

    /** Whether indices are embedded inside the create table statement */
    private boolean _indicesEmbedded = false;

    /** Whether embedded foreign key constraints are explicitly named */
    private boolean _embeddedForeignKeysNamed = false;

    /** Is an ALTER TABLE needed to drop indexes? */
    private boolean _useAlterTableForDrop = false;

    /** The string that starts a comment */
    private String _commentPrefix = "--";

    /** The string that ends a comment */
    private String _commentSuffix = "";

    /** Contains non-default mappings from jdbc to native types */
    private HashMap _specialTypes = new HashMap();

    //
    // Configuration
    //                

    /**
     * Adds a mapping from jdbc type to database-native type.
     * 
     * @param jdbcTypeCode The jdbc type code as defined by {@link java.sql.Types}
     * @param nativeType   The native type
     */
    protected void addNativeTypeMapping(int jdbcTypeCode, String nativeType)
    {
        _specialTypes.put(new Integer(jdbcTypeCode), nativeType);
    }

    /**
     * Returns the writer that the DDL is printed to.
     * 
     * @return The writer
     */
    public Writer getWriter()
    {
        return _writer;
    }

    /**
     * Sets the writer for printing the DDL to.
     * 
     * @param writer The writer
     */
    public void setWriter(Writer writer)
    {
        _writer = writer;
    }

    /** 
     * Returns the string used to indent the SQL.
     * 
     * @param The indentation string
     */
    public String getIndent()
    {
        return _indent;
    }

    /**
     * Sets the string used to indent the SQL.
     * 
     * @param indent The indentation string
     */
    public void setIndent(String indent)
    {
        _indent = indent;
    }

    /**
     * Determines whether a NULL needs to be explicitly stated when the column
     * has no specified default value. Default is false.
     * 
     * @return <code>true</code> if NULL must be written for empty default values
     */
    public boolean isRequiringNullAsDefaultValue()
    {
        return _requiringNullAsDefaultValue;
    }
    /**
     * Specifies whether a NULL needs to be explicitly stated when the column
     * has no specified default value. Default is false.
     *
     * @param requiresNullAsDefaultValue Whether NULL must be written for empty
     *                                   default values
     */
    public void setRequiringNullAsDefaultValue(boolean requiresNullAsDefaultValue)
    {
        _requiringNullAsDefaultValue = requiresNullAsDefaultValue;
    }

    /**
     * Determines whether primary key constraints are embedded in the create 
     * table clause or as seperate alter table statements. The default is
     * embedded pks.
     * 
     * @return <code>true</code> if pk constraints are embedded
     */
    public boolean isPrimaryKeyEmbedded()
    {
        return _primaryKeyEmbedded;
    }

    /**
     * Specifies whether the primary key constraints are embedded in the create 
     * table clause or as seperate alter table statements.
     * 
     * @param primaryKeyEmbedded Whether pk constraints are embedded
     */
    public void setPrimaryKeyEmbedded(boolean primaryKeyEmbedded)
    {
        _primaryKeyEmbedded = primaryKeyEmbedded;
    }

    /**
     * Determines whether foreign key constraints are embedded in the create 
     * table clause or as seperate alter table statements. Per default,
     * foreign keys are external.
     * 
     * @return <code>true</code> if fk constraints are embedded
     */
    public boolean isForeignKeysEmbedded()
    {
        return _foreignKeysEmbedded;
    }

    /**
     * Specifies whether foreign key constraints are embedded in the create 
     * table clause or as seperate alter table statements.
     * 
     * @param foreignKeysEmbedded Whether fk constraints are embedded
     */
    public void setForeignKeysEmbedded(boolean foreignKeysEmbedded)
    {
        _foreignKeysEmbedded = foreignKeysEmbedded;
    }

    /**
     * Determines whether the indices are embedded in the create table clause
     * or as seperate statements. Per default, indices are external.
     * 
     * @return <code>true</code> if indices are embedded
     */
    public boolean isIndicesEmbedded()
    {
        return _indicesEmbedded;
    }

    /**
     * Specifies whether indices are embedded in the create table clause or
     * as seperate alter table statements.
     * 
     * @param indicesEmbedded Whether indices are embedded
     */
    public void setIndicesEmbedded(boolean indicesEmbedded)
    {
        _indicesEmbedded = indicesEmbedded;
    }

    /**
     * Returns whether embedded foreign key constraints should have a name named.
     * 
     * @return <code>true</code> if embedded fks have name
     */
    public boolean isEmbeddedForeignKeysNamed()
    {
        return _embeddedForeignKeysNamed;
    }

    /**
     * Specifies whether embedded foreign key constraints should be named.
     * 
     * @param embeddedForeignKeysNamed Whether embedded fks shall have a name
     */
    public void setEmbeddedForeignKeysNamed(boolean embeddedForeignKeysNamed) {
        _embeddedForeignKeysNamed = embeddedForeignKeysNamed;
    }

    /**
     * Determinws whether an ALTER TABLE statement shall be used for dropping indices
     * or constraints.  The default is false.
     * 
     * @return <code>true</code> if ALTER TABLE is required
     */
    public boolean isUseAlterTableForDrop()
    {
        return _useAlterTableForDrop;
    }

    /**
     * Specifies whether an ALTER TABLE statement shall be used for dropping indices
     * or constraints.
     * 
     * @param useAlterTableForDrop Whether ALTER TABLE will be used
     */
    public void setUseAlterTableForDrop(boolean useAlterTableForDrop)
    {
        _useAlterTableForDrop = useAlterTableForDrop;
    }

    /**
     * Returns the string that denotes the beginning of a comment.
     *
     * @return The comment prefix
     */
    public String getCommentPrefix()
    {
        return _commentPrefix;
    }

    /**
     * Sets the text that starts a comment.
     * 
     * @param commentPrefix The new comment prefix
     */
    public void setCommentPrefix(String commentPrefix)
    {
        _commentPrefix = (commentPrefix == null ? "" : commentPrefix);
    }

    /**
     * Returns the string that denotes the end of a comment. Note that comments will
     * be always on their own line.
     *
     * @return The comment suffix
     */
    public String getCommentSuffix()
    {
        return _commentSuffix;
    }

    /**
     * Sets the text that ends a comment.
     * 
     * @param commentSuffix The new comment suffix
     */
    public void setCommentSuffix(String commentSuffix)
    {
        _commentSuffix = (commentSuffix == null ? "" : commentSuffix);
    }

    //
    // public interface
    //

    /**
     * Outputs the DDL required to drop and recreate the database.
     * 
     * @param database The database model 
     */
    public void createDatabase(Database database) throws IOException
    {
        createDatabase(database, true);
    }

    /**
     * Outputs the DDL required to drop (if requested) and recreate the database.
     * 
     * @param database   The database
     * @param dropTables Whether to drop tables before creating them
     */
    public void createDatabase(Database database, boolean dropTables) throws IOException
    {
        if (dropTables)
        {
            dropDatabase(database);
        }
            
        for (Iterator it = database.getTables().iterator(); it.hasNext(); )
        {
            Table table = (Table)it.next();

            writeTableComment(table);
            createTable(table);
        }

        // we're writing the external foreignkeys last to ensure that all referenced tables are already defined
        for (Iterator it = database.getTables().iterator(); it.hasNext(); )
        {
            createExternalForeignKeys((Table)it.next());
        }
    }

    /**
     * Generates the DDL to modify an existing database so the schema matches
     * the current specified database schema. Drops and modifications will
     * not be made.
     *
     * @param desiredDb  The desired database schema
     * @param connection A connection to the existing database that shall be modified
     * @throws IOException  If the ddl could notz be written
     * @throws SQLException if there is an error reading the current schema
     */
    public void alterDatabase(Database desiredDb, Connection connection) throws IOException, SQLException
    {
        alterDatabase(desiredDb, connection, false, false);
    }

    /**
     * Generates the DDL to modify an existing database so the schema matches
     * the current specified database schema.
     *
     * @param desiredDb     The desired database schema
     * @param connection    A connection to the existing database that shall be modified
     * @param doDrops       Whether columns and indexes should be dropped if not in the
     *                      new schema
     * @param modifyColumns Whether columns should be altered for datatype, size as required
     * @throws IOException  If the ddl could not be written
     * @throws SQLException If there is an error reading the current schema
     */
    public void alterDatabase(Database desiredDb, Connection connection, boolean doDrops, boolean modifyColumns) throws IOException, SQLException
    {
        Database  currentDb = new JdbcModelReader(connection).getDatabase();
        ArrayList newTables = new ArrayList();

        for (Iterator tableIt = desiredDb.getTables().iterator(); tableIt.hasNext();)
        {
            Table desiredTable = (Table)tableIt.next();
            Table currentTable = currentDb.findTable(desiredTable.getName());

            if (currentTable == null)
            {
                _log.info("Creating table " + desiredTable.getName());
                createTable(desiredTable);
                // we're deferring foreignkey generation
                newTables.add(desiredTable);
            }
            else
            {
                for (Iterator columnIt = desiredTable.getColumns().iterator(); columnIt.hasNext();)
                {
                    Column desiredColumn = (Column)columnIt.next();
                    Column currentColumn = currentTable.findColumn(desiredColumn.getName());

                    if (null == currentColumn)
                    {
                        _log.info("Creating column " + desiredTable.getName() + "." + desiredColumn.getName());
                        writeColumnAlterStmt(desiredTable, desiredColumn, true);
                    }
                    else if (columnsDiffer(desiredColumn, currentColumn))
                    {
                        if (modifyColumns)
                        {
                            _log.info("Altering column " + desiredTable.getName() + "." + desiredColumn.getName());
                            _log.info("  desired = " + desiredColumn.toStringAll());
                            _log.info("  current = " + currentColumn.toStringAll());
                            writeColumnAlterStmt(desiredTable, desiredColumn, false);
                        }
                        else
                        {
                            String text = "Column " + currentColumn.getName() + " in table " + currentTable.getName() + " differs from current specification";

                            _log.info(text);
                            printComment(text);
                        }
                    }
                }

                // TODO: add constraints here...

                // Hmm, m-w.com says indices and indexes are both okay
                // TODO: should we check the index fields for differences?

                for (Iterator indexIt = desiredTable.getIndexes().iterator(); indexIt.hasNext();)
                {
                    Index desiredIndex = (Index)indexIt.next();
                    Index currentIndex = currentTable.findIndex(desiredIndex.getName());

                    if (null == currentIndex)
                    {
                        _log.info("Creating index " + desiredTable.getName() + "." + desiredIndex.getName());
                        writeExternalIndexCreateStmt(desiredTable, desiredIndex);
                    }
                }

                // TODO: drop constraints - probably need names on them for this

                for (Iterator columnIt = currentTable.getColumns().iterator(); columnIt.hasNext();)
                {
                    Column currentColumn = (Column)columnIt.next();
                    Column desiredColumn = desiredTable.findColumn(currentColumn.getName());

                    if (null == desiredColumn)
                    {
                        if (doDrops)
                        {
                            _log.info("Dropping column " + currentTable.getName() + "." + currentColumn.getName());
                            writeColumnDropStmt(currentTable, currentColumn);
                        }
                        else
                        {
                            String text = "Column " + currentColumn.getName() + " can be dropped from table " + currentTable.getName();

                            _log.info(text);
                            printComment(text);
                        }
                    }
                }

                for (Iterator indexIt = currentTable.getIndexes().iterator(); indexIt.hasNext();)
                {
                    Index currentIndex = (Index)indexIt.next();
                    Index desiredIndex = desiredTable.findIndex(currentIndex.getName());

                    if (null == desiredIndex)
                    {
                        // make sure this isn't the primary key index
                        boolean  isPk = true;

                        for (Iterator columnIt = currentIndex.getIndexColumns().iterator(); columnIt.hasNext();)
                        {
                            IndexColumn indexColumn = (IndexColumn)columnIt.next();
                            Column      column      = currentTable.findColumn(indexColumn.getName());

                            if (!column.isPrimaryKey())
                            {
                                isPk = false;
                                break;
                            }
                        }
                        if (!isPk)
                        {
                            _log.info("Dropping non-primary index " + currentTable.getName() + "." + currentIndex.getName());
                            writeExternalIndexDropStmt(currentTable, currentIndex);
                        }
                    }
                }

            } 
        }

        // generating deferred foreignkeys
        for (Iterator fkIt = newTables.iterator(); fkIt.hasNext();)
        {
            createExternalForeignKeys((Table)fkIt.next());
        }

        // check for table drops
        for (Iterator tableIt = currentDb.getTables().iterator(); tableIt.hasNext();)
        {
            Table currentTable = (Table)tableIt.next();
            Table desiredTable = desiredDb.findTable(currentTable.getName());

            if (desiredTable == null)
            {
                if (doDrops)
                {
                    _log.info("Dropping table " + currentTable.getName());
                    dropTable(currentTable);
                }
                else
                {
                    String text = "Table " + currentTable.getName() + " can be dropped";

                    _log.info(text);
                    printComment(text);
                }
            }
        }
    }

    /** 
     * Outputs the DDL to create the table along with any non-external constraints as well
     * as with external primary keys and indices (but not foreign keys).
     * 
     * @param table The table
     */
    public void createTable(Table table) throws IOException 
    {
        print("CREATE TABLE ");
        println(table.getName());
        println("(");

        writeColumns(table);

        if (isPrimaryKeyEmbedded())
        {
            writeEmbeddedPrimaryKeysStmt(table);
        }
        if (isForeignKeysEmbedded())
        {
            writeEmbeddedForeignKeysStmt(table);
        }
        if (isIndicesEmbedded())
        {
            writeEmbeddedIndicesStmt(table);
        }
        println();
        print(")");
        printEndOfStatement();

        if (!isPrimaryKeyEmbedded())
        {
            writeExternalPrimaryKeysCreateStmt(table);
        }
        if (!isIndicesEmbedded())
        {
            writeExternalIndicesCreateStmt(table);
        }
    }

    /**
     * Creates external foreignkey creation statements if necessary.
     * 
     * @param table The table
     */
    public void createExternalForeignKeys(Table table) throws IOException
    {
        if (!isForeignKeysEmbedded())
        {
            int numKey = 1;

            for (Iterator it = table.getForeignKeys().iterator(); it.hasNext(); numKey++)
            {
                writeExternalForeignKeyCreateStmt(table, (ForeignKey)it.next(), numKey);
            }
        }
    }

    /**
     * Outputs the DDL required to drop the database.
     * 
     * @param database The database 
     */
    public void dropDatabase(Database database) throws IOException
    {
        List tables = database.getTables();

        // we're dropping the external foreignkeys first
        for (int idx = tables.size() - 1; idx >= 0; idx--)
        {
            dropExternalForeignKeys((Table)tables.get(idx));
        }

        // Next we drop the tables in reverse order to avoid referencial problems
        for (int idx = tables.size() - 1; idx >= 0; idx--)
        {
            Table table = (Table)tables.get(idx);

            writeTableComment(table);
            dropTable(table);
        }
    }

    /**
     * Outputs the DDL to drop the table.
     * 
     * @param table The table to drop
     */
    public void dropTable(Table table) throws IOException
    {
        print("DROP TABLE ");
        print(table.getName());
        printEndOfStatement();
    }

    /**
     * Creates external foreignkey drop statements.
     * 
     * @param table The table
     */
    public void dropExternalForeignKeys(Table table) throws IOException
    {
        if (!isForeignKeysEmbedded())
        {
            int numKey = 1;

            for (Iterator it = table.getForeignKeys().iterator(); it.hasNext(); numKey++)
            {
                writeExternalForeignKeyDropStmt(table, (ForeignKey)it.next(), numKey);
            }
        }
    }

    //
    // implementation methods that may be overridden by specific database builders
    //

    /** 
     * Outputs a comment for the table.
     * 
     * @param table The table
     */
    protected void writeTableComment(Table table) throws IOException
    {
        printComment("-----------------------------------------------------------------------");
        printComment(table.getName());
        printComment("-----------------------------------------------------------------------");
        println();
    }

    /**
     * Generates the first part of the ALTER TABLE statement including the
     * table name.
     *
     * @param table The table being altered
     */
    protected void writeTableAlterStmt(Table table) throws IOException
    {
        print("ALTER TABLE ");
        println(table.getName());
        printIndent();
    }

    /**
     * Writes the columns of the given table.
     * 
     * @param table The table 
     */
    protected void writeColumns(Table table) throws IOException
    {
        for (Iterator it = table.getColumns().iterator(); it.hasNext();)
        {
            printIndent();
            writeColumn(table, (Column)it.next());
            if (it.hasNext())
            {
                println(",");
            }
        }
    }

    /** 
     * Outputs the DDL for the specified column.
     * 
     * @param table  The table containing the column
     * @param column The column
     */
    protected void writeColumn(Table table, Column column) throws IOException
    {
        //see comments in columnsDiffer about null/"" defaults
        print(column.getName());
        print(" ");
        print(getSqlType(column));

        if (column.getDefaultValue() != null)
        {
            print(" DEFAULT '" + column.getDefaultValue() + "'");
        }
        if (column.isRequired())
        {
            print(" ");
            writeColumnNotNullableStmt();
        }
        else if (isRequiringNullAsDefaultValue() &&
                 (TypeMap.isTextType(column.getTypeCode()) || TypeMap.isBinaryType(column.getTypeCode())))
        {
            print(" ");
            writeColumnNullableStmt();
        }
        if (column.isAutoIncrement())
        {
            print(" ");
            writeColumnAutoIncrementStmt(table, column);
        }
    }

    /**
     * Generates the alter statement to add or modify a single column on a table.
     *
     * @param table       The table the index is on
     * @param column      The column to drop
     * @param isNewColumn Whether the column should be added
     */
    public void writeColumnAlterStmt(Table table, Column column, boolean isNewColumn) throws IOException
    {
        writeTableAlterStmt(table);
        print(isNewColumn ? "ADD " : "MODIFY ");
        writeColumn(table, column);
        printEndOfStatement();
    }

    /**
     * Generates the statement to drop an column from a table.
     *
     * @param table  The table the index is on
     * @param column The column to drop
     */
    public void writeColumnDropStmt(Table table, Column column) throws IOException
    {
        writeTableAlterStmt(table);
        print("DROP COLUMN ");
        print(column.getName());
        printEndOfStatement();
    }

    /**
     * Returns the full SQL type specification (including size and precision/scale) for the
     * given column.
     * 
     * @param column The column
     * @return The full SQL type string including the size
     */
    protected String getSqlType(Column column)
    {
        StringBuffer sqlType = new StringBuffer(getNativeType(column));

        if (column.getSize() != null)
        {
            sqlType.append(" (");
            sqlType.append(column.getSize());
            if (TypeMap.typeHasScaleAndPrecision(column.getType()))
            {
                sqlType.append(",");
                sqlType.append(column.getScale());
            }
            sqlType.append(")");
        }
        return sqlType.toString();
    }

    /**
     * Returns the database-native type for the given column
     * 
     * @param column The column
     * @return The native type
     */
    protected String getNativeType(Column column)
    {
        String nativeType = (String)_specialTypes.get(new Integer(column.getTypeCode()));

        return nativeType == null ? column.getType() : nativeType;
    }

    /**
     * Prints that the column is an auto increment column.
     * 
     * @param table  The table
     * @param column The column
     */ 
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        print("IDENTITY");
    }

    /**
     * Prints that a column is nullable 
     */
    protected void writeColumnNullableStmt() throws IOException
    {
        print("NULL");
    }
    
    /**
     * Prints that a column is not nullable
     */
    protected void writeColumnNotNullableStmt() throws IOException 
    {
        print("NOT NULL");
    }

    /**
     * Helper method to determine if two column specifications represent
     * different types.  Type, nullability, size, scale, default value,
     * and precision radix are the attributes checked.  Currently default
     * values are compared where null and empty string are considered equal.
     * See comments in the method body for explanation.
     *
     * @param columnA First column to compare
     * @param columnB Second column to compare
     * @return <code>true</code> if the columns differ
     */
    protected boolean columnsDiffer(Column columnA, Column columnB)
    {
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

        String  desiredDefault = columnA.getDefaultValue();
        String  currentDefault = columnB.getDefaultValue();
        boolean defaultsEqual  = (desiredDefault == null) || desiredDefault.equals(currentDefault);
        boolean sizeMatters    = (columnA.getSize() != null);

        if ((columnA.getTypeCode() != columnB.getTypeCode()) ||
            (columnA.isRequired() != columnB.isRequired()) ||
            (sizeMatters && (columnA.getSize() != columnB.getSize())) ||
            (columnA.getScale() != columnB.getScale()) ||
            !defaultsEqual ||
            (columnA.getPrecisionRadix() != columnB.getPrecisionRadix()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Writes the primary key constraints of the table inside its definition.
     * 
     * @param table The table
     */
    protected void writeEmbeddedPrimaryKeysStmt(Table table) throws IOException
    {
        List primaryKeyColumns = table.getPrimaryKeyColumns();

        if (!primaryKeyColumns.isEmpty() && shouldGeneratePrimaryKeys(primaryKeyColumns))
        {
            println(",");
            printIndent();
            writePrimaryKeyStmt(primaryKeyColumns);
        }
    }

    /**
     * Writes the primary key constraints of the table as alter table statements.
     * 
     * @param table The table
     */
    protected void writeExternalPrimaryKeysCreateStmt(Table table) throws IOException
    {
        List primaryKeyColumns = table.getPrimaryKeyColumns();

        if (!primaryKeyColumns.isEmpty() && shouldGeneratePrimaryKeys(primaryKeyColumns))
        {
            print("ALTER TABLE ");
            println(table.getName());
            printIndent();
            print("ADD CONSTRAINT ");
            print(table.getName());
            print("_PK ");
            writePrimaryKeyStmt(primaryKeyColumns);
            printEndOfStatement();
        }
    }

    /**
     * Determines whether we should generate a primary key constraint for the given
     * primary key columns. By default if there are no primary keys or the column(s) are 
     * all auto increment (identity) columns then there is no need to generate a primary key 
     * constraint.
     * 
     * @param primaryKeyColumns The pk columns
     * @return <code>true</code> if a pk statement should be generated for the columns
     */
    protected boolean shouldGeneratePrimaryKeys(List primaryKeyColumns)
    {
        for (Iterator it = primaryKeyColumns.iterator(); it.hasNext();)
        {
            if (!((Column)it.next()).isAutoIncrement())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Writes a primary key statement for the given columns.
     * 
     * @param primaryKeyColumns The primary columns
     */
    protected void writePrimaryKeyStmt(List primaryKeyColumns) throws IOException
    {
        print("PRIMARY KEY (");
        for (Iterator it = primaryKeyColumns.iterator(); it.hasNext();)
        {
            print(((Column)it.next()).getName());
            if (it.hasNext())
            {
                print(", ");
            }
        }
        print(")");
    }

    /**
     * Writes the indexes of the given table.
     * 
     * @param table The table
     */
    protected void writeExternalIndicesCreateStmt(Table table) throws IOException
    {
        for (Iterator it = table.getIndexes().iterator(); it.hasNext();)
        {
            writeExternalIndexCreateStmt(table, (Index)it.next());
        }
    }

    /**
     * Writes the indexes embedded within the create table statement.
     * 
     * @param table The table
     */
    protected void writeEmbeddedIndicesStmt(Table table) throws IOException 
    {
        // TODO
    }

    /**
     * Writes the given index of the table.
     * 
     * @param table The table
     * @param index The index
     */
    protected void writeExternalIndexCreateStmt(Table table, Index index) throws IOException
    {
        if (index.getName() == null)
        {
            _log.warn("Cannot write unnamed index " + index);
        }
        else
        {
            print("CREATE");
            if (index.isUnique())
            {
                print(" UNIQUE");
            }
            print(" INDEX ");
            print(index.getName());
            print(" ON ");
            print(table.getName());
            print(" (");

            for (Iterator it = index.getIndexColumns().iterator(); it.hasNext();)
            {
                print(((IndexColumn)it.next()).getName());
                if (it.hasNext())
                {
                    print(", ");
                }
            }

            print(")");
            printEndOfStatement();
        }
    }

    /**
     * Generates the statement to drop a non-embedded index from the database.
     *
     * @param table The table the index is on
     * @param index The index to drop
     */
    public void writeExternalIndexDropStmt(Table table, Index index) throws IOException
    {
        if (isUseAlterTableForDrop())
        {
            writeTableAlterStmt(table);
        }
        print("DROP INDEX ");
        print(index.getName());
        if (!isUseAlterTableForDrop())
        {
            print(" ON ");
            print(table.getName());
        }
        printEndOfStatement();
    }


    /**
     * Writes the foreign key constraints inside a create table () clause.
     * 
     * @param table The table
     */
    protected void writeEmbeddedForeignKeysStmt(Table table) throws IOException
    {
        int numKey = 1;

        for (Iterator it = table.getForeignKeys().iterator(); it.hasNext(); numKey++)
        {
            ForeignKey key = (ForeignKey)it.next();

            if (key.getForeignTable() == null)
            {
                _log.warn("Foreign key table is null for key " + key);
            }
            else
            {
                println(",");
                printIndent();
                
                if (isEmbeddedForeignKeysNamed())
                {
                    print("CONSTRAINT ");
                    print(table.getName());
                    print("_FK_");
                    print(Integer.toString(numKey));
                    print(" ");
                }
                print("FOREIGN KEY (");
                writeLocalReferences(key);
                print(") REFERENCES ");
                print(key.getForeignTable());
                print(" (");
                writeForeignReferences(key);
                print(")");
            }
        }
    }

    /**
     * Writes a single foreign key constraint using a alter table statement.
     * 
     * @param table  The table 
     * @param key    The foreign key
     * @param numKey The number of the key, starting with 1
     */
    protected void writeExternalForeignKeyCreateStmt(Table table, ForeignKey key, int numKey) throws IOException
    {
        if (key.getForeignTable() == null)
        {
            _log.warn("Foreign key table is null for key " + key);
        }
        else
        {
            writeTableAlterStmt(table);

            print("ADD CONSTRAINT ");
            print(table.getName());
            print("_FK_");
            print(Integer.toString(numKey));
            print(" FOREIGN KEY (");
            writeLocalReferences(key);
            print(") REFERENCES ");
            print(key.getForeignTable());
            print(" (");
            writeForeignReferences(key);
            print(")");
            printEndOfStatement();
        }
    }

    /**
     * Writes a list of local references for the given foreign key.
     * 
     * @param key The foreign key
     */
    protected void writeLocalReferences(ForeignKey key) throws IOException
    {
        for (Iterator it = key.getReferences().iterator(); it.hasNext();)
        {
            print(((Reference)it.next()).getLocal());
            if (it.hasNext())
            {
                print(", ");
            }
        }
    }

    /**
     * Writes a list of foreign references for the given foreign key.
     * 
     * @param key The foreign key
     */
    protected void writeForeignReferences(ForeignKey key) throws IOException
    {
        for (Iterator it = key.getReferences().iterator(); it.hasNext();)
        {
            print(((Reference)it.next()).getForeign());
            if (it.hasNext())
            {
                print(", ");
            }
        }
    }

    /**
     * Generates the statement to drop a foreignkey constraint from the database using an
     * alter table statement-
     *
     * @param table  The table 
     * @param key    The foreign key
     * @param numKey The number of the key, starting with 1
     */
    protected void writeExternalForeignKeyDropStmt(Table table, ForeignKey foreignKey, int numKey) throws IOException
    {
        writeTableAlterStmt(table);
        print("DROP CONSTRAINT ");
        print(table.getName());
        print("_FK_");
        print(Integer.toString(numKey));
        printEndOfStatement();
    }

    //
    // Helper methods
    //

    /**
     * Prints an SQL comment to the current stream.
     * 
     * @param text The comment text
     */
    protected void printComment(String text) throws IOException
    { 
        print(getCommentPrefix());
        // Some databases insist on a space after the prefix
        print(" ");
        print(text);
        print(" ");
        print(getCommentSuffix());
        println();
    }
    
    /** 
     * Prints the end of statement text, which is typically a semi colon followed by 
     * a carriage return.
     */
    protected void printEndOfStatement() throws IOException
    {
        println(";");
        println();
    }

    /** 
     * Prints a newline.
     */
    protected void println() throws IOException
    {
        print(LINE_SEPERATOR);
    }

    /**
     * Prints some text.
     * 
     * @param text The text to print
     */
    protected void print(String text) throws IOException
    {
        _writer.write(text);
    }

    /**
     * Prints some text followed by a newline.
     * 
     * @param text The text to print
     */
    protected void println(String text) throws IOException
    {
        print(text);
        println();
    }

    /**
     * Prints the characters used to indent SQL
     */
    protected void printIndent() throws IOException
    {
        print(getIndent());
    }
}
