package org.apache.ddlutils.platform;

/*
 * Copyright 1999-2006 The Apache Software Foundation.
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
import java.sql.Types;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.DynaSqlException;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.IndexColumn;
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
 * @author James Strachan
 * @author John Marshall/Connectria
 * @author Thomas Dudziak
 * @version $Revision$
 */
public abstract class SqlBuilder
{
    /** The line separator for in between sql commands. */
    private static final String LINE_SEPERATOR = System.getProperty("line.separator", "\n");
    /** The placeholder for the size value in the native type spec. */
    protected static final String SIZE_PLACEHOLDER = "{0}";

    /** The Log to which logging calls will be made. */
    protected final Log _log = LogFactory.getLog(SqlBuilder.class);
    
    /** The current Writer used to output the SQL to. */
    private Writer _writer;
    
    /** The indentation used to indent commands. */
    private String _indent = "    ";

    /** The platform info. */
    private PlatformInfo _info;

    /** An optional locale specification for number and date formatting. */
    private String _valueLocale;

    /** The date formatter. */
    private DateFormat _valueDateFormat;

    /** The date time formatter. */
    private DateFormat _valueTimeFormat;

    /** The number formatter. */
    private NumberFormat _valueNumberFormat;

    /** Helper object for dealing with default values. */
    private DefaultValueHelper _defaultValueHelper = new DefaultValueHelper();

    /** The character sequences that need escaping. */
    private Map _charSequencesToEscape = new ListOrderedMap();

    //
    // Configuration
    //                

    /**
     * Creates a new sql builder.
     * 
     * @param info The plaftform information
     */
    public SqlBuilder(PlatformInfo info)
    {
        _info = info;
    }

    /**
     * Returns the platform info object.
     * 
     * @return The info object
     */
    public PlatformInfo getPlatformInfo()
    {
        return _info;
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
     * Returns the default value helper.
     *
     * @return The default value helper
     */
    public DefaultValueHelper getDefaultValueHelper()
    {
        return _defaultValueHelper;
    }

    /** 
     * Returns the string used to indent the SQL.
     * 
     * @return The indentation string
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
     * @param localeStr The new locale or <code>null</code> if default formatting
     *                  should be used; Format is "language[_country[_variant]]"
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
     * Adds a char sequence that needs escaping, and its escaped version.
     * 
     * @param charSequence   The char sequence
     * @param escapedVersion The escaped version
     */
    protected void addEscapedCharSequence(String charSequence, String escapedVersion)
    {
        _charSequencesToEscape.put(charSequence, escapedVersion);
    }
    
    //
    // public interface
    //

    /**
     * Outputs the DDL required to drop and (re)create all tables in the database model.
     * 
     * @param database The database model 
     */
    public void createTables(Database database) throws IOException
    {
        createTables(database, null, true);
    }

    /**
     * Outputs the DDL required to drop (if requested) and (re)create all tables in the database model.
     * 
     * @param database   The database
     * @param dropTables Whether to drop tables before creating them
     */
    public void createTables(Database database, boolean dropTables) throws IOException
    {
        createTables(database, null, dropTables);
    }

    /**
     * Outputs the DDL required to drop (if requested) and (re)create all tables in the database model.
     * 
     * @param database   The database
     * @param params     The parameters used in the creation
     * @param dropTables Whether to drop tables before creating them
     */
    public void createTables(Database database, CreationParameters params, boolean dropTables) throws IOException
    {
        if (dropTables)
        {
            dropTables(database);
        }

        for (int idx = 0; idx < database.getTableCount(); idx++)
        {
            Table table = database.getTable(idx);

            writeTableComment(table);
            createTable(database,
                        table,
                        params == null ? null : params.getParametersFor(table));
        }

        // we're writing the external foreignkeys last to ensure that all referenced tables are already defined
        createExternalForeignKeys(database);
    }

    /**
     * Generates the DDL to modify an existing database so the schema matches
     * the current specified database schema. Drops and modifications will
     * not be made.
     *
     * @param currentModel  The current database schema
     * @param desiredModel  The desired database schema
     */
    public void alterDatabase(Database currentModel, Database desiredModel) throws IOException
    {
        alterDatabase(currentModel, desiredModel, false, false);
    }

    /**
     * Generates the DDL to modify an existing database so the schema matches
     * the current specified database schema.
     *
     * @param currentModel  The current database schema
     * @param desiredModel  The desired database schema
     * @param doDrops       Whether columns and indexes should be dropped if not in the
     *                      new schema
     * @param modifyColumns Whether columns should be altered for datatype, size as required
     */
    public void alterDatabase(Database currentModel, Database desiredModel, boolean doDrops, boolean modifyColumns) throws IOException
    {
        alterDatabase(currentModel, desiredModel, null, doDrops, modifyColumns);
    }        

    /**
     * Generates the DDL to modify an existing database so the schema matches
     * the current specified database schema.
     *
     * @param currentModel  The current database schema
     * @param desiredModel  The desired database schema
     * @param params        The parameters used in the creation of new tables. Note that for existing
     *                      tables, parameters won't be applied
     * @param doDrops       Whether columns and indexes should be dropped if not in the
     *                      new schema
     * @param modifyColumns Whether columns should be altered for datatype, size as required
     */
    public void alterDatabase(Database currentModel, Database desiredModel, CreationParameters params, boolean doDrops, boolean modifyColumns) throws IOException
    {
        ArrayList newTables = new ArrayList();

        for (int tableIdx = 0; tableIdx < desiredModel.getTableCount(); tableIdx++)
        {
            Table desiredTable = desiredModel.getTable(tableIdx);
            Table currentTable = currentModel.findTable(desiredTable.getName());

            if (currentTable == null)
            {
                if (_log.isInfoEnabled())
                {
                    _log.info("Creating table " + desiredTable.getName());
                }
                createTable(desiredModel,
                            desiredTable,
                            params == null ? null : params.getParametersFor(desiredTable));
                // we're deferring foreignkey generation
                newTables.add(desiredTable);
            }
            else
            {
                alterTable(currentModel, currentTable,
                           desiredModel, desiredTable,
                           doDrops, modifyColumns);
            }
        }

        // generating deferred foreignkeys
        //TODO should we try to generate new FKs on existing tables?
        for (Iterator fkIt = newTables.iterator(); fkIt.hasNext();)
        {
            createExternalForeignKeys(desiredModel, (Table)fkIt.next());
        }

        // check for table drops
        for (int idx = 0; idx < currentModel.getTableCount(); idx++)
        {
            Table currentTable = currentModel.getTable(idx);
            Table desiredTable = desiredModel.findTable(currentTable.getName());

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
     * Alters the given currently existing table object to match the given desired table object.
     * 
     * @param currentModel The current model
     * @param currentTable The current table definition
     * @param desiredModel The desired model
     * @param desiredTable The desired table definition
     * @param doDrops       Whether columns and indexes should be dropped if not in the
     *                      new schema
     * @param modifyColumns Whether columns should be altered for datatype, size as required
     */
    protected void alterTable(Database currentModel, Table currentTable, Database desiredModel, Table desiredTable, boolean doDrops, boolean modifyColumns) throws IOException
    {
        for (int columnIdx = 0; columnIdx < desiredTable.getColumnCount(); columnIdx++)
        {
            Column desiredColumn = desiredTable.getColumn(columnIdx);
            Column currentColumn = currentTable.findColumn(desiredColumn.getName());

            if (null == currentColumn)
            {
                if (_log.isInfoEnabled())
                {
                    _log.info("Creating column " + desiredTable.getName() + "." + desiredColumn.getName());
                }
                writeColumnAlterStmt(desiredTable, desiredColumn, true);
            }
            else if (columnsDiffer(currentColumn, desiredColumn))
            {
                if (modifyColumns)
                {
                    if (_log.isInfoEnabled())
                    {
                        _log.info("Altering column " + desiredTable.getName() + "." + desiredColumn.getName());
                        _log.info("  desired = " + desiredColumn.toVerboseString());
                        _log.info("  current = " + currentColumn.toVerboseString());
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
        for (int fkIdx = 0; fkIdx < desiredTable.getForeignKeyCount(); fkIdx++)
        {
            ForeignKey desiredFk = desiredTable.getForeignKey(fkIdx);
            ForeignKey currentFk = currentTable.findForeignKey(desiredFk);

            if (currentFk == null)
            {
                if (_log.isInfoEnabled())
                {
                    _log.info("Creating foreign key " + desiredTable.getName() + "." + desiredFk);
                }
                writeExternalForeignKeyCreateStmt(desiredModel, desiredTable, desiredFk);
            }
        }

        // TODO: should we check the index fields for differences?
        //create new indexes
        for (int indexIdx = 0; indexIdx < desiredTable.getIndexCount(); indexIdx++)
        {
            Index desiredIndex = desiredTable.getIndex(indexIdx);
            Index currentIndex = currentTable.findIndex(desiredIndex.getName());

            if (currentIndex == null)
            {
                if (_log.isInfoEnabled())
                {
                    _log.info("Creating index " + desiredTable.getName() + "." + desiredIndex.getName());
                }
                writeExternalIndexCreateStmt(desiredTable, desiredIndex);
            }
        }

        // drop fk constraints
        for (int fkIdx = 0; fkIdx < currentTable.getForeignKeyCount(); fkIdx++)
        {
            ForeignKey currentFk = currentTable.getForeignKey(fkIdx);
            ForeignKey desiredFk = desiredTable.findForeignKey(currentFk);

            if (desiredFk == null)
            {
                if (_log.isInfoEnabled())
                {
                    _log.info((doDrops ? "" : "Not ") + "Dropping foreign key " + currentTable.getName() + "." + currentFk);
                }
                if (doDrops)
                {
                    writeExternalForeignKeyDropStmt(currentTable, currentFk);
                }
            }
        }


        //Drop columns
        for (int columnIdx = 0; columnIdx < currentTable.getColumnCount(); columnIdx++)
        {
            Column currentColumn = currentTable.getColumn(columnIdx);
            Column desiredColumn = desiredTable.findColumn(currentColumn.getName());

            if (desiredColumn == null)
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
        for (int indexIdx = 0; indexIdx < currentTable.getIndexCount(); indexIdx++)
        {
            Index currentIndex = currentTable.getIndex(indexIdx);
            Index desiredIndex = desiredTable.findIndex(currentIndex.getName());

            if (desiredIndex == null)
            {
                // make sure this isn't the primary key index
                boolean  isPk = true;

                for (int columnIdx = 0; columnIdx < currentIndex.getColumnCount(); columnIdx++)
                {
                    IndexColumn indexColumn = currentIndex.getColumn(columnIdx);
                    Column      column      = currentTable.findColumn(indexColumn.getName());

                    if ((column != null) && !column.isPrimaryKey())
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
                    if (doDrops)
                    {
                        writeExternalIndexDropStmt(currentTable, currentIndex);
                    }
                }
            }
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
        createTable(database, table, null); 
    }

    /** 
     * Outputs the DDL to create the table along with any non-external constraints as well
     * as with external primary keys and indices (but not foreign keys).
     * 
     * @param database   The database model
     * @param table      The table
     * @param parameters Additional platform-specific parameters for the table creation
     */
    public void createTable(Database database, Table table, Map parameters) throws IOException 
    {
        print("CREATE TABLE ");
        printlnIdentifier(getTableName(table));
        println("(");

        writeColumns(table);
        
        if (getPlatformInfo().isPrimaryKeyEmbedded())
        {
            writeEmbeddedPrimaryKeysStmt(table);
        }
        if (getPlatformInfo().isForeignKeysEmbedded())
        {
            writeEmbeddedForeignKeysStmt(database, table);
        }
        if (getPlatformInfo().isIndicesEmbedded())
        {
            writeEmbeddedIndicesStmt(table);
        }
        println();
        print(")");
        writeTableCreationStmtEnding(table, parameters);

        if (!getPlatformInfo().isPrimaryKeyEmbedded())
        {
            writeExternalPrimaryKeysCreateStmt(table);
        }
        if (!getPlatformInfo().isIndicesEmbedded())
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
        for (int idx = 0; idx < database.getTableCount(); idx++)
        {
            createExternalForeignKeys(database, database.getTable(idx));
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
        if (!getPlatformInfo().isForeignKeysEmbedded())
        {
            for (int idx = 0; idx < table.getForeignKeyCount(); idx++)
            {
                writeExternalForeignKeyCreateStmt(database, table, table.getForeignKey(idx));
            }
        }
    }

    /**
     * Outputs the DDL required to drop the database.
     * 
     * @param database The database 
     */
    public void dropTables(Database database) throws IOException
    {
        // we're dropping the external foreignkeys first
        for (int idx = database.getTableCount() - 1; idx >= 0; idx--)
        {
            Table table = database.getTable(idx);

            if ((table.getName() != null) &&
                (table.getName().length() > 0))
            {
                dropExternalForeignKeys(table);
            }
        }

        // Next we drop the tables in reverse order to avoid referencial problems
        // TODO: It might be more useful to either (or both)
        //       * determine an order in which the tables can be dropped safely (via the foreignkeys)
        //       * alter the tables first to drop the internal foreignkeys
        for (int idx = database.getTableCount() - 1; idx >= 0; idx--)
        {
            Table table = database.getTable(idx);

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
        printIdentifier(getTableName(table));
        printEndOfStatement();
    }

    /**
     * Creates external foreignkey drop statements.
     * 
     * @param table The table
     */
    public void dropExternalForeignKeys(Table table) throws IOException
    {
        if (!getPlatformInfo().isForeignKeysEmbedded())
        {
            for (int idx = 0; idx < table.getForeignKeyCount(); idx++)
            {
                writeExternalForeignKeyDropStmt(table, table.getForeignKey(idx));
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
    public String getInsertSql(Table table, Map columnValues, boolean genPlaceholders)
    {
        StringBuffer buffer   = new StringBuffer("INSERT INTO ");
        boolean      addComma = false;

        buffer.append(getDelimitedIdentifier(getTableName(table)));
        buffer.append(" (");

        for (int idx = 0; idx < table.getColumnCount(); idx++)
        {
            Column column = table.getColumn(idx);

            if (columnValues.containsKey(column.getName()))
            {
                if (addComma)
                {
                    buffer.append(", ");
                }
                buffer.append(getDelimitedIdentifier(column.getName()));
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
            for (int idx = 0; idx < table.getColumnCount(); idx++)
            {
                Column column = table.getColumn(idx);

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
     * @param columnValues    Contains the values for the columns to update, and should also
     *                        contain the primary key values to identify the object to update
     *                        in case <code>genPlaceholders</code> is <code>false</code> 
     * @param genPlaceholders Whether to generate value placeholders for a
     *                        prepared statement (both for the pk values and the object values)
     * @return The update sql
     */
    public String getUpdateSql(Table table, Map columnValues, boolean genPlaceholders)
    {
        StringBuffer buffer = new StringBuffer("UPDATE ");
        boolean      addSep = false;

        buffer.append(getDelimitedIdentifier(getTableName(table)));
        buffer.append(" SET ");

        for (int idx = 0; idx < table.getColumnCount(); idx++)
        {
            Column column = table.getColumn(idx);

            if (!column.isPrimaryKey() && columnValues.containsKey(column.getName()))
            {
                if (addSep)
                {
                    buffer.append(", ");
                }
                buffer.append(getDelimitedIdentifier(column.getName()));
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
        for (int idx = 0; idx < table.getColumnCount(); idx++)
        {
            Column column = table.getColumn(idx);

            if (column.isPrimaryKey() && columnValues.containsKey(column.getName()))
            {
                if (addSep)
                {
                    buffer.append(" AND ");
                }
                buffer.append(getDelimitedIdentifier(column.getName()));
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
    public String getDeleteSql(Table table, Map pkValues, boolean genPlaceholders)
    {
        StringBuffer buffer = new StringBuffer("DELETE FROM ");
        boolean      addSep = false;

        buffer.append(getDelimitedIdentifier(getTableName(table)));
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
                buffer.append(getDelimitedIdentifier(entry.getKey().toString()));
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
                result.append(getPlatformInfo().getValueQuoteToken());
                if (!(value instanceof String) && (_valueDateFormat != null))
                {
                    // TODO: Can the format method handle java.sql.Date properly ?
                    result.append(_valueDateFormat.format(value));
                }
                else
                {
                    result.append(value.toString());
                }
                result.append(getPlatformInfo().getValueQuoteToken());
                break;
            case Types.TIME:
                result.append(getPlatformInfo().getValueQuoteToken());
                if (!(value instanceof String) && (_valueTimeFormat != null))
                {
                    // TODO: Can the format method handle java.sql.Date properly ?
                    result.append(_valueTimeFormat.format(value));
                }
                else
                {
                    result.append(value.toString());
                }
                result.append(getPlatformInfo().getValueQuoteToken());
                break;
            case Types.REAL:
            case Types.NUMERIC:
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.DECIMAL:
                result.append(getPlatformInfo().getValueQuoteToken());
                if (!(value instanceof String) && (_valueNumberFormat != null))
                {
                    result.append(_valueNumberFormat.format(value));
                }
                else
                {
                    result.append(value.toString());
                }
                result.append(getPlatformInfo().getValueQuoteToken());
                break;
            default:
                result.append(getPlatformInfo().getValueQuoteToken());
                result.append(escapeStringValue(value.toString()));
                result.append(getPlatformInfo().getValueQuoteToken());
                break;
        }
        return result.toString();
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
        return shortenName(table.getName(), getPlatformInfo().getMaxIdentifierLength());
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
        printlnIdentifier(getTableName(table));
        printIndent();
    }

    /** 
     * Writes the end of the table creation statement. Per default,
     * only the end of the statement is written, but this can be changed
     * in subclasses.
     * 
     * @param table      The table
     * @param parameters Additional platform-specific parameters for the table creation
     */
    protected void writeTableCreationStmtEnding(Table table, Map parameters) throws IOException
    {
        printEndOfStatement();
    }

    /**
     * Writes the columns of the given table.
     * 
     * @param table The table 
     */
    protected void writeColumns(Table table) throws IOException
    {
        for (int idx = 0; idx < table.getColumnCount(); idx++)
        {
            printIndent();
            writeColumn(table, table.getColumn(idx));
            if (idx < table.getColumnCount() - 1)
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
        return shortenName(column.getName(), getPlatformInfo().getMaxIdentifierLength());
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
        printIdentifier(getColumnName(column));
        print(" ");
        print(getSqlType(column));

        if ((column.getDefaultValue() != null) ||
            (getPlatformInfo().isIdentitySpecUsesDefaultValue() && column.isAutoIncrement()))
        {
            if (!getPlatformInfo().isSupportingDefaultValuesForLongTypes() && 
                ((column.getTypeCode() == Types.LONGVARBINARY) || (column.getTypeCode() == Types.LONGVARCHAR)))
            {
                throw new DynaSqlException("The platform does not support default values for LONGVARCHAR or LONGVARBINARY columns");
            }
            print(" DEFAULT ");
            writeColumnDefaultValue(table, column);
        }
        if (column.isRequired())
        {
            print(" ");
            writeColumnNotNullableStmt();
        }
        else if (getPlatformInfo().isRequiringNullAsDefaultValue() &&
                 getPlatformInfo().hasNullDefault(column.getTypeCode()))
        {
            print(" ");
            writeColumnNullableStmt();
        }
        if (column.isAutoIncrement() && !getPlatformInfo().isIdentitySpecUsesDefaultValue())
        {
            if (!getPlatformInfo().isSupportingNonPKIdentityColumns() && !column.isPrimaryKey())
            {
                throw new DynaSqlException("Column "+column.getName()+" in table "+table.getName()+" is auto-incrementing but not a primary key column, which is not supported by the platform");
            }
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
        printIdentifier(getColumnName(column));
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
        String       nativeType = getNativeType(column);
        int          sizePos    = nativeType.indexOf(SIZE_PLACEHOLDER);
        StringBuffer sqlType    = new StringBuffer();

        sqlType.append(sizePos >= 0 ? nativeType.substring(0, sizePos) : nativeType);

        Object sizeSpec = column.getSize();
        
        if (sizeSpec == null)
        {
            sizeSpec = getPlatformInfo().getDefaultSize(column.getTypeCode());
        }
        if (sizeSpec != null)
        {
            if (getPlatformInfo().hasSize(column.getTypeCode()))
            {
                sqlType.append("(");
                sqlType.append(sizeSpec.toString());
                sqlType.append(")");
            }
            else if (getPlatformInfo().hasPrecisionAndScale(column.getTypeCode()))
            {
                sqlType.append("(");
                sqlType.append(sizeSpec.toString());
                sqlType.append(",");
                sqlType.append(column.getScale());
                sqlType.append(")");
            }
        }
        sqlType.append(sizePos >= 0 ? nativeType.substring(sizePos + SIZE_PLACEHOLDER.length()) : "");

        return sqlType.toString();
    }

    /**
     * Returns the database-native type for the given column.
     * 
     * @param column The column
     * @return The native type
     */
    protected String getNativeType(Column column)
    {
        String nativeType = (String)getPlatformInfo().getNativeType(column.getTypeCode());

        return nativeType == null ? column.getType() : nativeType;
    }

    /**
     * Returns the native default value for the column.
     * 
     * @param column The column
     * @return The native default value
     */
    protected String getNativeDefaultValue(Column column)
    {
        return column.getDefaultValue();
    }

    /**
     * Escapes the necessary characters in given string value.
     * 
     * @param value The value
     * @return The corresponding string with the special characters properly escaped
     */
    protected String escapeStringValue(String value)
    {
        String result = value;

        for (Iterator it = _charSequencesToEscape.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry entry = (Map.Entry)it.next();

            result = StringUtils.replace(result, (String)entry.getKey(), (String)entry.getValue());
        }
        return result;
    }
    
    /**
     * Prints the default value of the column.
     * 
     * @param table  The table
     * @param column The column
     */ 
    protected void writeColumnDefaultValue(Table table, Column column) throws IOException
    {
        boolean shouldUseQuotes = !TypeMap.isNumericType(column.getTypeCode());

        if (shouldUseQuotes)
        {
            // characters are only escaped when within a string literal 
            print(getPlatformInfo().getValueQuoteToken());
            print(escapeStringValue(getNativeDefaultValue(column).toString()));
            print(getPlatformInfo().getValueQuoteToken());
        }
        else
        {
            print(getNativeDefaultValue(column).toString());
        }
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
     * Prints that a column is nullable.
     */
    protected void writeColumnNullableStmt() throws IOException
    {
        print("NULL");
    }
    
    /**
     * Prints that a column is not nullable.
     */
    protected void writeColumnNotNullableStmt() throws IOException 
    {
        print("NOT NULL");
    }

    /**
     * Compares the current column in the database with the desired one.
     * Type, nullability, size, scale, default value, and precision radix are
     * the attributes checked.  Currently default values are compared, and
     * null and empty string are considered equal.
     *
     * @param currentColumn The current column as it is in the database
     * @param desiredColumn The desired column
     * @return <code>true</code> if the column specifications differ
     */
    protected boolean columnsDiffer(Column currentColumn, Column desiredColumn)
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

        String  desiredDefault = desiredColumn.getDefaultValue();
        String  currentDefault = currentColumn.getDefaultValue();
        boolean defaultsEqual  = (desiredDefault == null) || desiredDefault.equals(currentDefault);
        boolean sizeMatters    = (desiredColumn.getSize() != null);

        // We're comparing the jdbc type that corresponds to the native type for the
        // desired type, in order to avoid repeated altering of a perfectly valid column
        if ((getPlatformInfo().getTargetJdbcType(desiredColumn.getTypeCode()) != currentColumn.getTypeCode()) ||
            (desiredColumn.isRequired() != currentColumn.isRequired()) ||
            (sizeMatters && (!desiredColumn.getSize().equals(currentColumn.getSize()))) ||
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

    /**
     * Returns the name to be used for the given foreign key. If the foreign key has no
     * specified name, this method determines a unique name for it. The name will also
     * be shortened to honor the maximum identifier length imposed by the platform.
     * 
     * @param table The table for whith the foreign key is defined
     * @param fk    The foreign key
     * @return The name
     */
    public String getForeignKeyName(Table table, ForeignKey fk)
    {
        String fkName = fk.getName();

        if ((fkName == null) || (fkName.length() == 0))
        {
            StringBuffer name = new StringBuffer();
    
            for (int idx = 0; idx < fk.getReferenceCount(); idx++)
            {
                name.append(fk.getReference(idx).getLocalColumnName());
                name.append("_");
            }
            name.append(fk.getForeignTableName());
            fkName = getConstraintName(null, table, "FK", name.toString());
        }
        return shortenName(fkName, getPlatformInfo().getMaxIdentifierLength());
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
        return shortenName(result.toString(), getPlatformInfo().getMaxIdentifierLength());
    }

    /**
     * Writes the primary key constraints of the table inside its definition.
     * 
     * @param table The table
     */
    protected void writeEmbeddedPrimaryKeysStmt(Table table) throws IOException
    {
        Column[] primaryKeyColumns = table.getPrimaryKeyColumns();

        if ((primaryKeyColumns.length > 0) && shouldGeneratePrimaryKeys(primaryKeyColumns))
        {
            printStartOfEmbeddedStatement();
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
        Column[] primaryKeyColumns = table.getPrimaryKeyColumns();

        if ((primaryKeyColumns.length > 0) && shouldGeneratePrimaryKeys(primaryKeyColumns))
        {
            print("ALTER TABLE ");
            printlnIdentifier(getTableName(table));
            printIndent();
            print("ADD CONSTRAINT ");
            printIdentifier(getConstraintName(null, table, "PK", null));
            print(" ");
            writePrimaryKeyStmt(table, primaryKeyColumns);
            printEndOfStatement();
        }
    }

    /**
     * Determines whether we should generate a primary key constraint for the given
     * primary key columns.
     * 
     * @param primaryKeyColumns The pk columns
     * @return <code>true</code> if a pk statement should be generated for the columns
     */
    protected boolean shouldGeneratePrimaryKeys(Column[] primaryKeyColumns)
    {
        return true;
/*
        for (int idx = 0; idx < primaryKeyColumns.length; idx++)
        {
            if (!primaryKeyColumns[idx].isAutoIncrement())
            {
                return true;
            }
        }
        return false;
*/
    }

    /**
     * Writes a primary key statement for the given columns.
     * 
     * @param table             The table
     * @param primaryKeyColumns The primary columns
     */
    protected void writePrimaryKeyStmt(Table table, Column[] primaryKeyColumns) throws IOException
    {
        print("PRIMARY KEY (");
        for (int idx = 0; idx < primaryKeyColumns.length; idx++)
        {
            printIdentifier(getColumnName(primaryKeyColumns[idx]));
            if (idx < primaryKeyColumns.length - 1)
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
        for (int idx = 0; idx < table.getIndexCount(); idx++)
        {
            Index index = table.getIndex(idx);

            if (!index.isUnique() && !getPlatformInfo().isSupportingNonUniqueIndices())
            {
                throw new DynaSqlException("Platform does not support non-unique indices");
            }
            writeExternalIndexCreateStmt(table, index);
        }
    }

    /**
     * Writes the indexes embedded within the create table statement.
     * 
     * @param table The table
     */
    protected void writeEmbeddedIndicesStmt(Table table) throws IOException 
    {
        for (int idx = 0; idx < table.getIndexCount(); idx++)
        {
            Index index = table.getIndex(idx);

            if (!index.isUnique() && !getPlatformInfo().isSupportingNonUniqueIndices())
            {
                throw new DynaSqlException("Platform does not support non-unique indices");
            }
            printStartOfEmbeddedStatement();
            writeEmbeddedIndexCreateStmt(table, index);
        }
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
            printIdentifier(getIndexName(index));
            print(" ON ");
            printIdentifier(getTableName(table));
            print(" (");

            for (int idx = 0; idx < index.getColumnCount(); idx++)
            {
                IndexColumn idxColumn = index.getColumn(idx);
                Column      col       = table.findColumn(idxColumn.getName());

                if (col == null)
                {
                    //would get null pointer on next line anyway, so throw exception
                    throw new DynaSqlException("Invalid column '" + idxColumn.getName() + "' on index " + index.getName() + " for table " + table.getName());
                }
                if (idx > 0)
                {
                    print(", ");
                }
                printIdentifier(getColumnName(col));
            }

            print(")");
            printEndOfStatement();
        }
    }

    /**
     * Writes the given embedded index of the table.
     * 
     * @param table The table
     * @param index The index
     */
    protected void writeEmbeddedIndexCreateStmt(Table table, Index index) throws IOException
    {
        if ((index.getName() != null) && (index.getName().length() > 0))
        {
            print(" CONSTRAINT ");
            printIdentifier(getIndexName(index));
        }
        if (index.isUnique())
        {
            print(" UNIQUE");
        }
        else
        {
            print(" INDEX ");
        }
        print(" (");

        for (int idx = 0; idx < index.getColumnCount(); idx++)
        {
            IndexColumn idxColumn = index.getColumn(idx);
            Column      col       = table.findColumn(idxColumn.getName());

            if (col == null)
            {
                //would get null pointer on next line anyway, so throw exception
                throw new DynaSqlException("Invalid column '" + idxColumn.getName() + "' on index " + index.getName() + " for table " + table.getName());
            }
            if (idx > 0)
            {
                print(", ");
            }
            printIdentifier(getColumnName(col));
        }

        print(")");
    }

    /**
     * Generates the statement to drop a non-embedded index from the database.
     *
     * @param table The table the index is on
     * @param index The index to drop
     */
    public void writeExternalIndexDropStmt(Table table, Index index) throws IOException
    {
        if (getPlatformInfo().isUseAlterTableForDrop())
        {
            writeTableAlterStmt(table);
        }
        print("DROP INDEX ");
        printIdentifier(getIndexName(index));
        if (!getPlatformInfo().isUseAlterTableForDrop())
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
        for (int idx = 0; idx < table.getForeignKeyCount(); idx++)
        {
            ForeignKey key = table.getForeignKey(idx);

            if (key.getForeignTableName() == null)
            {
                _log.warn("Foreign key table is null for key " + key);
            }
            else
            {
                printStartOfEmbeddedStatement();
                if (getPlatformInfo().isEmbeddedForeignKeysNamed())
                {
                    print("CONSTRAINT ");
                    printIdentifier(getForeignKeyName(table, key));
                    print(" ");
                }
                print("FOREIGN KEY (");
                writeLocalReferences(key);
                print(") REFERENCES ");
                printIdentifier(getTableName(database.findTable(key.getForeignTableName())));
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
     */
    protected void writeExternalForeignKeyCreateStmt(Database database, Table table, ForeignKey key) throws IOException
    {
        if (key.getForeignTableName() == null)
        {
            _log.warn("Foreign key table is null for key " + key);
        }
        else
        {
            writeTableAlterStmt(table);

            print("ADD CONSTRAINT ");
            printIdentifier(getForeignKeyName(table, key));
            print(" FOREIGN KEY (");
            writeLocalReferences(key);
            print(") REFERENCES ");
            printIdentifier(getTableName(database.findTable(key.getForeignTableName())));
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
        for (int idx = 0; idx < key.getReferenceCount(); idx++)
        {
            if (idx > 0)
            {
                print(", ");
            }
            printIdentifier(key.getReference(idx).getLocalColumnName());
        }
    }

    /**
     * Writes a list of foreign references for the given foreign key.
     * 
     * @param key The foreign key
     */
    protected void writeForeignReferences(ForeignKey key) throws IOException
    {
        for (int idx = 0; idx < key.getReferenceCount(); idx++)
        {
            if (idx > 0)
            {
                print(", ");
            }
            printIdentifier(key.getReference(idx).getForeignColumnName());
        }
    }

    /**
     * Generates the statement to drop a foreignkey constraint from the database using an
     * alter table statement.
     *
     * @param table      The table 
     * @param foreignKey The foreign key
     */
    protected void writeExternalForeignKeyDropStmt(Table table, ForeignKey foreignKey) throws IOException
    {
        writeTableAlterStmt(table);
        print("DROP CONSTRAINT ");
        printIdentifier(getForeignKeyName(table, foreignKey));
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
        if (getPlatformInfo().isCommentsSupported())
        {
            print(getPlatformInfo().getCommentPrefix());
            // Some databases insist on a space after the prefix
            print(" ");
            print(text);
            print(" ");
            print(getPlatformInfo().getCommentSuffix());
            println();
        }
    }

    /** 
     * Prints the start of an embedded statement.
     */
    protected void printStartOfEmbeddedStatement() throws IOException
    {
        println(",");
        printIndent();
    }

    /** 
     * Prints the end of statement text, which is typically a semi colon followed by 
     * a carriage return.
     */
    protected void printEndOfStatement() throws IOException
    {
        println(getPlatformInfo().getSqlCommandDelimiter());
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
     * Returns the delimited version of the identifier (if configured).
     * 
     * @param identifier The identifier
     * @return The delimited version of the identifier unless the platform is configured
     *         to use undelimited identifiers; in that case, the identifier is returned unchanged
     */
    protected String getDelimitedIdentifier(String identifier)
    {
        if (getPlatformInfo().isUseDelimitedIdentifiers())
        {
            return getPlatformInfo().getDelimiterToken() + identifier + getPlatformInfo().getDelimiterToken();
        }
        else
        {
            return identifier;
        }
    }

    /**
     * Prints the given identifier. For most databases, this will
     * be a delimited identifier.
     * 
     * @param identifier The identifier
     */
    protected void printIdentifier(String identifier) throws IOException
    {
        print(getDelimitedIdentifier(identifier));
    }

    /**
     * Prints the given identifier followed by a newline. For most databases, this will
     * be a delimited identifier.
     * 
     * @param identifier The identifier
     */
    protected void printlnIdentifier(String identifier) throws IOException
    {
        println(getDelimitedIdentifier(identifier));
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
     * Prints the characters used to indent SQL.
     */
    protected void printIndent() throws IOException
    {
        print(getIndent());
    }
}
