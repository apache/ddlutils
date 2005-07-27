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
import java.io.Writer;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.io.JdbcModelReader;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.TypeMap;

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
 * @version $Revision$
 */
public abstract class SqlBuilder {

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

    /** Specifies the maximum length that an identifier (name of a table, column, constraint etc.)
     *  can have for this database; use -1 if there is no limit */
    private int _maxIdentifierLength = -1;

    /** Whether identifiers are case sensitive or not */
    private boolean _caseSensitive = false;

    /** The string used for escaping values when generating textual SQL statements */
    private String _valueQuoteChar = "'";

    /** An optional locale specification for number and date formatting */
    private String _valueLocale;

    /** The date formatter */
    private DateFormat _valueDateFormat;

    /** The date time formatter */
    private DateFormat _valueTimeFormat;

    /** The number formatter */
    private NumberFormat _valueNumberFormat;
    
    /** Whether comments are supported */
    private boolean _commentsSupported = true;

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
     * Adds a mapping from jdbc type to database-native type. Note that this
     * method accesses the named constant in {@link java.sql.Types} via reflection
     * and is thus safe to use under JDK 1.2/1.3 even with constants defined
     * only in later Java versions - for these, the method simply will not add
     * a mapping.
     * 
     * @param jdbcTypeName The jdbc type name, one of the constants defined in
     *                     {@link java.sql.Types}
     * @param nativeType   The native type
     */
    protected void addNativeTypeMapping(String jdbcTypeName, String nativeType)
    {
        try
        {
            Field constant = Types.class.getField(jdbcTypeName);

            if (constant != null)
            {
                addNativeTypeMapping(constant.getInt(null), nativeType);
            }
        }
        catch (Exception ex)
        {
            // ignore -> won't be defined
            // TODO: add a logging statement
        }
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
     * Returns whether embedded foreign key constraints should have a name.
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
    public void setEmbeddedForeignKeysNamed(boolean embeddedForeignKeysNamed)
    {
        _embeddedForeignKeysNamed = embeddedForeignKeysNamed;
    }

    /**
     * Determines whether an ALTER TABLE statement shall be used for dropping indices
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
     * Returns the maximum length of identifiers that this database allows.
     * 
     * @return The maximum identifier length, -1 if unlimited
     */
    public int getMaxIdentifierLength()
    {
        return _maxIdentifierLength;
    }

    /**
     * Sets the maximum length of identifiers that this database allows.
     * 
     * @param maxIdentifierLength The maximum identifier length, -1 if unlimited
     */
    public void setMaxIdentifierLength(int maxIdentifierLength)
    {
        _maxIdentifierLength = maxIdentifierLength;
    }

    /**
     * Determines whether the database has case sensitive identifiers.
     *
     * @return <code>true</code> if case of the the identifiers is important
     */
    public boolean isCaseSensitive()
    {
        return _caseSensitive;
    }

    /**
     * Specifies whether the database has case sensitive identifiers.
     *
     * @param caseSensitive <code>true</code> if case of the the identifiers is important
     */
    public void setCaseSensitive(boolean caseSensitive)
    {
        _caseSensitive = caseSensitive;
    }

    /**
     * Returns the locale that is used for number and date formatting
     * (when printing default values and in generates insert/update/delete
     * statements).
     * 
     * @return The locale or <code>null</code> if default formatting is used
     */
    public String getValueLocale()
    {
        return _valueLocale;
    }

    /**
     * Sets the locale that is used for number and date formatting
     * (when printing default values and in generates insert/update/delete
     * statements).
     *
     * @param locale The new locale or <code>null</code> if default formatting
     *               should be used; Format is "language[_country[_variant]]"
     */
    public void setValueLocale(String localeStr)
    {
        if (localeStr != null)
        {
            int    sepPos   = localeStr.indexOf('_');
            String language = null;
            String country  = null;
            String variant  = null;

            if (sepPos > 0)
            {
                language = localeStr.substring(0, sepPos);
                country  = localeStr.substring(sepPos + 1);
                sepPos   = country.indexOf('_');
                if (sepPos > 0)
                {
                    variant = country.substring(sepPos + 1);
                    country = country.substring(0, sepPos);
                }
            }
            else
            {
                language = localeStr;
            }
            if (language != null)
            {
                Locale locale = null;
    
                if (variant != null)
                {
                    locale = new Locale(language, country, variant);
                }
                else if (country != null)
                {
                    locale = new Locale(language, country);
                }
                else
                {
                    locale = new Locale(language);
                }

                _valueLocale       = localeStr;
                _valueDateFormat   = DateFormat.getDateInstance(DateFormat.SHORT, locale);
                _valueTimeFormat   = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
                _valueNumberFormat = NumberFormat.getNumberInstance(locale);
                return;
            }
        }
        _valueLocale       = null;
        _valueDateFormat   = null;
        _valueTimeFormat   = null;
        _valueNumberFormat = null;
    }

    /**
     * Returns the text that is used for for quoting values (e.g. text) when
     * printing default values and in generates insert/update/delete statements.
     * 
     * @return The quote text
     */
    public String getValueQuoteChar()
    {
        return _valueQuoteChar;
    }

    /**
     * Sets the text that is used for for quoting values (e.g. text) when
     * printing default values and in generates insert/update/delete statements.
     *
     * @param valueQuoteChar The new quote text
     */
    public void setValueQuoteChar(String valueQuoteChar)
    {
        _valueQuoteChar = valueQuoteChar;
    }

    /**
     * Determines whether the database supports comments.
     *
     * @return <code>true</code> if comments are supported
     */
    public boolean getCommentsSupported()
    {
        return _commentsSupported;
    }

    /**
     * Specifies whether comments are supported by the database.
     * 
     * @param commentsSupported <code>true</code> if comments are supported
     */
    public void setCommentsSupported(boolean commentsSupported)
    {
        _commentsSupported = commentsSupported;
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
     * Returns the name of the database that this builder is for.
     * 
     * @return The database name
     */
    public abstract String getDatabaseName();
    
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

        createTables(database);

        // we're writing the external foreignkeys last to ensure that all referenced tables are already defined
        createExternalForeignKeys(database);
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
                if (_log.isInfoEnabled())
                {
                    _log.info("Creating table " + desiredTable.getName());
                }
                createTable(desiredDb, desiredTable);
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
                        if (_log.isInfoEnabled())
                        {
                            _log.info("Creating column " + desiredTable.getName() + "." + desiredColumn.getName());
                        }
                        writeColumnAlterStmt(desiredTable, desiredColumn, true);
                    }
                    else if (columnsDiffer(desiredColumn, currentColumn))
                    {
                        if (modifyColumns)
                        {
                            if (_log.isInfoEnabled())
                            {
                                _log.info("Altering column " + desiredTable.getName() + "." + desiredColumn.getName());
                                _log.info("  desired = " + desiredColumn.toStringAll());
                                _log.info("  current = " + currentColumn.toStringAll());
                            }
                            writeColumnAlterStmt(desiredTable, desiredColumn, false);
                        }
                        else
                        {
                            String text = "Column " + currentColumn.getName() + " in table " + currentTable.getName() + " differs from current specification";

                            if (_log.isInfoEnabled())
                            {
                                _log.info(text);
                            }
                            printComment(text);
                        }
                    }
                }

                // add fk constraints
                for (Iterator fkIt = desiredTable.getForeignKeys().iterator(); fkIt.hasNext();) {
                    ForeignKey desiredFk = (ForeignKey) fkIt.next();
                    ForeignKey currentFk = currentTable.findForeignKey(desiredFk);
                    if ( currentFk == null ) {
                        if (_log.isInfoEnabled())
                        {
                            _log.info("Creating foreign key " + desiredTable.getName() + "." + desiredFk);
                        }
                        writeExternalForeignKeyCreateStmt(desiredDb, desiredTable, desiredFk);
                    }
                }

                // TODO: should we check the index fields for differences?
                //create new indexes
                for (Iterator indexIt = desiredTable.getIndexes().iterator(); indexIt.hasNext();)
                {
                    Index desiredIndex = (Index)indexIt.next();
                    Index currentIndex = currentTable.findIndex(desiredIndex.getName());

                    if (null == currentIndex)
                    {
                        if (_log.isInfoEnabled())
                        {
                            _log.info("Creating index " + desiredTable.getName() + "." + desiredIndex.getName());
                        }
                        writeExternalIndexCreateStmt(desiredTable, desiredIndex);
                    }
                }

                // drop fk constraints
                for (Iterator fkIt = currentTable.getForeignKeys().iterator(); fkIt.hasNext();) {
                    ForeignKey currentFk = (ForeignKey) fkIt.next();
                    ForeignKey desiredFk = desiredTable.findForeignKey(currentFk);

                    if ( desiredFk == null ) {
                        if (_log.isInfoEnabled())
                        {
                            _log.info((doDrops ? "" : "Not ") + "Dropping foreign key " + currentTable.getName() + "." + currentFk);
                        }
                        if ( doDrops ) {
                            writeExternalForeignKeyDropStmt(currentTable, currentFk);
                        }
                    }
                }


                //Drop columns
                for (Iterator columnIt = currentTable.getColumns().iterator(); columnIt.hasNext();)
                {
                    Column currentColumn = (Column)columnIt.next();
                    Column desiredColumn = desiredTable.findColumn(currentColumn.getName());

                    if (null == desiredColumn)
                    {
                        if (doDrops)
                        {
                            if (_log.isInfoEnabled())
                            {
                                _log.info("Dropping column " + currentTable.getName() + "." + currentColumn.getName());
                            }
                            writeColumnDropStmt(currentTable, currentColumn);
                        }
                        else
                        {
                            String text = "Column " + currentColumn.getName() + " can be dropped from table " + currentTable.getName();

                            if (_log.isInfoEnabled())
                            {
                                _log.info(text);
                            }
                            printComment(text);
                        }
                    }
                }

                //Drop indexes
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

                            if (column != null && !column.isPrimaryKey())
                            {
                                isPk = false;
                                break;
                            }
                        }
                        if (!isPk)
                        {
                            if (_log.isInfoEnabled())
                            {
                                _log.info((doDrops ? "" : "Not ") + "Dropping non-primary index " + currentTable.getName() + "." + currentIndex.getName());
                            }
                            if ( doDrops )
                            {
                                writeExternalIndexDropStmt(currentTable, currentIndex);
                            }
                        }
                    }
                }

            } 
        }

        // generating deferred foreignkeys
        //TODO should we try to generate new FKs on existing tables?
        for (Iterator fkIt = newTables.iterator(); fkIt.hasNext();)
        {
            createExternalForeignKeys(desiredDb, (Table)fkIt.next());
        }

        // check for table drops
        for (Iterator tableIt = currentDb.getTables().iterator(); tableIt.hasNext();)
        {
            Table currentTable = (Table)tableIt.next();
            Table desiredTable = desiredDb.findTable(currentTable.getName());

            if ((desiredTable == null) && (currentTable.getName() != null) && (currentTable.getName().length() > 0))
            {
                if (doDrops)
                {
                    if (_log.isInfoEnabled())
                    {
                        _log.info("Dropping table " + currentTable.getName());
                    }
                    dropTable(currentTable);
                }
                else
                {
                    String text = "Table " + currentTable.getName() + " can be dropped";

                    if (_log.isInfoEnabled())
                    {
                        _log.info(text);
                    }
                    printComment(text);
                }
            }
        }
    }

    /** 
     * Outputs the DDL to create all tables of the given database model.
     * 
     * @param database The database
     */
    public void createTables(Database database) throws IOException
    {
        for (Iterator it = database.getTables().iterator(); it.hasNext(); )
        {
            Table table = (Table)it.next();

            writeTableComment(table);
            createTable(database, table);
        }
    }

    /** 
     * Outputs the DDL to create the table along with any non-external constraints as well
     * as with external primary keys and indices (but not foreign keys).
     * 
     * @param database The database model
     * @param table    The table
     */
    public void createTable(Database database, Table table) throws IOException 
    {
        print("CREATE TABLE ");
        println(getTableName(table));
        println("(");

        writeColumns(table);

        if (isPrimaryKeyEmbedded())
        {
            writeEmbeddedPrimaryKeysStmt(table);
        }
        if (isForeignKeysEmbedded())
        {
            writeEmbeddedForeignKeysStmt(database, table);
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
     * Creates the external foreignkey creation statements for all tables in the database.
     * 
     * @param database The database
     */
    public void createExternalForeignKeys(Database database) throws IOException
    {
        for (Iterator it = database.getTables().iterator(); it.hasNext(); )
        {
            createExternalForeignKeys(database, (Table)it.next());
        }
    }

    /**
     * Creates external foreignkey creation statements if necessary.
     * 
     * @param database The database model
     * @param table    The table
     */
    public void createExternalForeignKeys(Database database, Table table) throws IOException
    {
        if (!isForeignKeysEmbedded())
        {
            int numKey = 1;

            for (Iterator it = table.getForeignKeys().iterator(); it.hasNext(); numKey++)
            {
                writeExternalForeignKeyCreateStmt(database, table, (ForeignKey)it.next());
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
            Table table = (Table)tables.get(idx);

            if ((table.getName() != null) &&
                (table.getName().length() > 0))
            {
                dropExternalForeignKeys(table);
            }
        }

        // Next we drop the tables in reverse order to avoid referencial problems
        for (int idx = tables.size() - 1; idx >= 0; idx--)
        {
            Table table = (Table)tables.get(idx);

            if ((table.getName() != null) &&
                (table.getName().length() > 0))
            {
                writeTableComment(table);
                dropTable(table);
            }
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
        print(getTableName(table));
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
                writeExternalForeignKeyDropStmt(table, (ForeignKey)it.next());
            }
        }
    }

    /**
     * Creates the SQL for inserting an object into the specified table.
     * If values are given then a concrete insert statement is created, otherwise an
     * insert statement usable in a prepared statement is build.
     *  
     * @param table           The table
     * @param columnValues    The columns values indexed by the column names
     * @param genPlaceholders Whether to generate value placeholders for a
     *                        prepared statement
     * @return The insertion sql
     */
    public String getInsertSql(Table table, HashMap columnValues, boolean genPlaceholders)
    {
        StringBuffer buffer   = new StringBuffer("INSERT INTO ");
        boolean      addComma = false;

        buffer.append(getTableName(table));
        buffer.append(" (");

        for (Iterator it = table.getColumns().iterator(); it.hasNext();)
        {
            Column column = (Column)it.next();

            if (columnValues.containsKey(column.getName()))
            {
                if (addComma)
                {
                    buffer.append(", ");
                }
                buffer.append(column.getName());
                addComma = true;
            }
        }
        buffer.append(") VALUES (");
        if (genPlaceholders)
        {
            addComma = false;
            for (int idx = 0; idx < columnValues.size(); idx++)
            {
                if (addComma)
                {
                    buffer.append(", ");
                }
                buffer.append("?");
                addComma = true;
            }
        }
        else
        {
            addComma = false;
            for (Iterator it = table.getColumns().iterator(); it.hasNext();)
            {
                Column column = (Column)it.next();

                if (columnValues.containsKey(column.getName()))
                {
                    if (addComma)
                    {
                        buffer.append(", ");
                    }
                    buffer.append(getValueAsString(column, columnValues.get(column.getName())));
                    addComma = true;
                }
            }
        }
        buffer.append(")");
        return buffer.toString();
    }

    /**
     * Creates the SQL for updating an object in the specified table.
     * If values are given then a concrete update statement is created, otherwise an
     * update statement usable in a prepared statement is build.
     * 
     * @param table           The table
     * @param columnValues    Contains the primary key values to identify the object to update
     *                        and the values for the columns to update
     * @param genPlaceholders Whether to generate value placeholders for a
     *                        prepared statement (both for the pk values and the object values)
     * @return The update sql
     */
    public String getUpdateSql(Table table, HashMap columnValues, boolean genPlaceholders)
    {
        StringBuffer buffer = new StringBuffer("UPDATE ");
        boolean      addSep = false;

        buffer.append(getTableName(table));
        buffer.append(" SET ");

        for (Iterator it = table.getColumns().iterator(); it.hasNext();)
        {
            Column column = (Column)it.next();

            if (!column.isPrimaryKey() && columnValues.containsKey(column.getName()))
            {
                if (addSep)
                {
                    buffer.append(", ");
                }
                buffer.append(column.getName());
                buffer.append(" = ");
                if (genPlaceholders)
                {
                    buffer.append("?");
                }
                else
                {
                    buffer.append(getValueAsString(column, columnValues.get(column.getName())));
                }
                addSep = true;
            }
        }
        buffer.append(" WHERE ");
        addSep = false;
        for (Iterator it = table.getColumns().iterator(); it.hasNext();)
        {
            Column column = (Column)it.next();

            if (column.isPrimaryKey() && columnValues.containsKey(column.getName()))
            {
                if (addSep)
                {
                    buffer.append(" AND ");
                }
                buffer.append(column.getName());
                buffer.append(" = ");
                if (genPlaceholders)
                {
                    buffer.append("?");
                }
                else
                {
                    buffer.append(getValueAsString(column, columnValues.get(column.getName())));
                }
                addSep = true;
            }
        }
        return buffer.toString();
    }

    /**
     * Creates the SQL for deleting an object from the specified table.
     * If values are given then a concrete delete statement is created, otherwise an
     * delete statement usable in a prepared statement is build.
     * 
     * @param table           The table
     * @param pkValues        The primary key values indexed by the column names, can be empty
     * @param genPlaceholders Whether to generate value placeholders for a
     *                        prepared statement
     * @return The delete sql
     */
    public String getDeleteSql(Table table, HashMap pkValues, boolean genPlaceholders)
    {
        StringBuffer buffer = new StringBuffer("DELETE FROM ");
        boolean      addSep = false;

        buffer.append(getTableName(table));
        if ((pkValues != null) && !pkValues.isEmpty())
        {
            buffer.append(" WHERE ");
            for (Iterator it = pkValues.entrySet().iterator(); it.hasNext();)
            {
                Map.Entry entry  = (Map.Entry)it.next();
                Column    column = table.findColumn((String)entry.getKey());
    
                if (addSep)
                {
                    buffer.append(" AND ");
                }
                buffer.append(entry.getKey());
                buffer.append(" = ");
                if (genPlaceholders)
                {
                    buffer.append("?");
                }
                else
                {
                    buffer.append(column == null ? entry.getValue() : getValueAsString(column, entry.getValue()));
                }
                addSep = true;
            }
        }
        return buffer.toString();
    }

    /**
     * Generates the SQL for querying the id that was created in the last insertion
     * operation. This is obviously only useful for pk fields that are auto-incrementing.
     * A database that does not support this, will return <code>null</code>.
     * 
     * @param table The table
     * @return The sql, or <code>null</code> if the database does not support this
     */
    public String getSelectLastInsertId(Table table)
    {
        // No default possible as the databases are quite different in this respect
        return null;
    }

    //
    // implementation methods that may be overridden by specific database builders
    //

    /**
     * Generates a version of the name that has at most the specified
     * length.
     * 
     * @param name          The original name
     * @param desiredLength The desired maximum length
     * @return The shortened version
     */
    protected String shortenName(String name, int desiredLength)
    {
        // TODO: Find an algorithm that generates unique names
        int originalLength = name.length();

        if ((desiredLength <= 0) || (originalLength <= desiredLength))
        {
            return name;
        }

        int delta    = originalLength - desiredLength;
        int startCut = desiredLength / 2;

        StringBuffer result = new StringBuffer();

        result.append(name.substring(0, startCut));
        if (((startCut == 0) || (name.charAt(startCut - 1) != '_')) &&
            ((startCut + delta + 1 == originalLength) || (name.charAt(startCut + delta + 1) != '_')))
        {
            // just to make sure that there isn't already a '_' right before or right
            // after the cutting place (which would look odd with an aditional one)
            result.append("_");
        }
        result.append(name.substring(startCut + delta + 1, originalLength));
        return result.toString();
    }
    
    /**
     * Returns the table name. This method takes care of length limitations imposed by some databases.
     * 
     * @param table The table
     * @return The table name
     */
    protected String getTableName(Table table)
    {
        return shortenName(table.getName(), _maxIdentifierLength);
    }
    
    /** 
     * Outputs a comment for the table.
     * 
     * @param table The table
     */
    protected void writeTableComment(Table table) throws IOException
    {
        printComment("-----------------------------------------------------------------------");
        printComment(getTableName(table));
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
        println(getTableName(table));
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
     * Returns the column name. This method takes care of length limitations imposed by some databases.
     * 
     * @param column The column
     * @return The column name
     */
    protected String getColumnName(Column column) throws IOException
    {
        return shortenName(column.getName(), _maxIdentifierLength);
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
        print(getColumnName(column));
        print(" ");
        print(getSqlType(column));

        if (column.getDefaultValue() != null)
        {
            print(" DEFAULT ");
            print(_valueQuoteChar);
            print(column.getDefaultValue());
            print(_valueQuoteChar);
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
        print(getColumnName(column));
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
            sqlType.append("(");
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
     * Generates the string representation of the given value.
     * 
     * @param column The column
     * @param value  The value
     * @return The string representation
     */
    protected String getValueAsString(Column column, Object value)
    {
        if (value == null)
        {
            return "NULL";
        }

        StringBuffer result = new StringBuffer();

        // TODO: Handle binary types (BINARY, VARBINARY, LONGVARBINARY, BLOB)
        switch (column.getTypeCode())
        {
            // Note: TIMESTAMP (java.sql.Timestamp) is properly handled by its toString method
            case Types.DATE:
                result.append(_valueQuoteChar);
                if (!(value instanceof String) && (_valueDateFormat != null))
                {
                    // TODO: Can the format method handle java.sql.Date properly ?
                    result.append(_valueDateFormat.format(value));
                }
                else
                {
                    result.append(value.toString());
                }
                result.append(_valueQuoteChar);
                break;
            case Types.TIME:
                result.append(_valueQuoteChar);
                if (!(value instanceof String) && (_valueTimeFormat != null))
                {
                    // TODO: Can the format method handle java.sql.Date properly ?
                    result.append(_valueTimeFormat.format(value));
                }
                else
                {
                    result.append(value.toString());
                }
                result.append(_valueQuoteChar);
                break;
            case Types.REAL:
            case Types.NUMERIC:
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.DECIMAL:
                result.append(_valueQuoteChar);
                if (!(value instanceof String) && (_valueNumberFormat != null))
                {
                    result.append(_valueNumberFormat.format(value));
                }
                else
                {
                    result.append(value.toString());
                }
                result.append(_valueQuoteChar);
                break;
            default:
                result.append(_valueQuoteChar);
                result.append(value.toString());
                result.append(_valueQuoteChar);
                break;
        }
        return result.toString();
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
            (sizeMatters && (!columnA.getSize().equals(columnB.getSize()))) ||
            !defaultsEqual /*|| //determined these two to be hardly useful
            (columnA.getScale() != columnB.getScale()) ||
            (columnA.getPrecisionRadix() != columnB.getPrecisionRadix())*/ )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    protected String getForeignKeyName(ForeignKey fk) {
        //table and local column should be sufficient - is it possible for one
        //column to reference multiple tables
        return fk.firstReference().getLocal() + "_" + fk.getForeignTable();
    }

    /**
     * Returns the constraint name. This method takes care of length limitations imposed by some databases.
     * 
     * @param prefix     The constraint prefix, can be <code>null</code>
     * @param table      The table that the constraint belongs to
     * @param secondPart The second name part, e.g. the name of the constraint column
     * @param suffix     The constraint suffix, e.g. a counter (can be <code>null</code>)
     * @return The constraint name
     */
    protected String getConstraintName(String prefix, Table table, String secondPart, String suffix)
    {
        StringBuffer result = new StringBuffer();
        
        if (prefix != null)
        {
            result.append(prefix);
            result.append("_");
        }
        result.append(table.getName());
        result.append("_");
        result.append(secondPart);
        if (suffix != null)
        {
            result.append("_");
            result.append(suffix);
        }
        return shortenName(result.toString(), _maxIdentifierLength);
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
            writePrimaryKeyStmt(table, primaryKeyColumns);
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
            println(getTableName(table));
            printIndent();
            print("ADD CONSTRAINT ");
            print(getConstraintName(null, table, "PK", null));
            print(" ");
            writePrimaryKeyStmt(table, primaryKeyColumns);
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
     * @param table             The table
     * @param primaryKeyColumns The primary columns
     */
    protected void writePrimaryKeyStmt(Table table, List primaryKeyColumns) throws IOException
    {
        print("PRIMARY KEY (");
        for (Iterator it = primaryKeyColumns.iterator(); it.hasNext();)
        {
            print(getColumnName((Column)it.next()));
            if (it.hasNext())
            {
                print(", ");
            }
        }
        print(")");
    }

    /**
     * Returns the index name. This method takes care of length limitations imposed by some databases.
     * 
     * @param index The index
     * @return The index name
     */
    protected String getIndexName(Index index) throws IOException
    {
        return index.getName();
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
            print(getIndexName(index));
            print(" ON ");
            print(getTableName(table));
            print(" (");

            for (Iterator it = index.getIndexColumns().iterator(); it.hasNext();)
            {
                IndexColumn idxColumn = (IndexColumn)it.next();

                Column col = table.findColumn(idxColumn.getName());
                if ( col == null ) {
                    //would get null pointer on next line anyway, so throw exception
                    throw new RuntimeException("Invalid column '" + idxColumn.getName() + "' on index " + index.getName() + " for table " + table.getName());
                }
                print(getColumnName(col));
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
        print(getIndexName(index));
        if (!isUseAlterTableForDrop())
        {
            print(" ON ");
            print(getTableName(table));
        }
        printEndOfStatement();
    }


    /**
     * Writes the foreign key constraints inside a create table () clause.
     * 
     * @param database The database model
     * @param table    The table
     */
    protected void writeEmbeddedForeignKeysStmt(Database database, Table table) throws IOException
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
                    print(getConstraintName(null, table, "FK", Integer.toString(numKey)));
                    print(" ");
                }
                print("FOREIGN KEY (");
                writeLocalReferences(key);
                print(") REFERENCES ");
                print(getTableName(database.findTable(key.getForeignTable())));
                print(" (");
                writeForeignReferences(key);
                print(")");
            }
        }
    }

    /**
     * Writes a single foreign key constraint using a alter table statement.
     * 
     * @param database The database model
     * @param table    The table 
     * @param key      The foreign key
     * @param numKey   The number of the key, starting with 1
     */
    protected void writeExternalForeignKeyCreateStmt(Database database, Table table, ForeignKey key) throws IOException
    {
        if (key.getForeignTable() == null)
        {
            _log.warn("Foreign key table is null for key " + key);
        }
        else
        {
            writeTableAlterStmt(table);

            print("ADD CONSTRAINT ");
            print(getConstraintName(null, table, "FK", getForeignKeyName(key)));
            print(" FOREIGN KEY (");
            writeLocalReferences(key);
            print(") REFERENCES ");
            print(getTableName(database.findTable(key.getForeignTable())));
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
    protected void writeExternalForeignKeyDropStmt(Table table, ForeignKey foreignKey) throws IOException
    {
        writeTableAlterStmt(table);
        print("DROP CONSTRAINT ");
        print(getConstraintName(null, table, "FK", getForeignKeyName(foreignKey)));
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
        if (getCommentsSupported())
        {
            print(getCommentPrefix());
            // Some databases insist on a space after the prefix
            print(" ");
            print(text);
            print(" ");
            print(getCommentSuffix());
            println();
        }
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
