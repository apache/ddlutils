package org.apache.commons.sql.ddl;

import java.sql.SQLException;
import java.util.Iterator;

import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.Database;
import org.apache.commons.sql.model.ForeignKey;
import org.apache.commons.sql.model.Reference;
import org.apache.commons.sql.model.Table;

import org.apache.commons.sql.type.Types;


/**
 * Helper to adapt a database schema to that supported by a particular
 * database provider
 * 
 * @version     1.1 2003/02/05 08:08:36
 * @author      <a href="mailto:tima@intalio.com">Tim Anderson</a>
 */
public abstract class AbstractDatabaseMapper implements DatabaseMapper {

    /**
     * The database to map
     */
    private Database database;

    /**
     * The types supported by the database provider
     */
    private Types types;


    /**
     * Set the database to be mapped
     */
    public void setDatabase(Database database) {
        this.database = database;
    }

    /**
     * Returns the database to be mapped
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Set the types supported by the database provider
     */
    public void setTypes(Types types) {
        this.types = types;
    }

    /**
     * Returns the types supported by the database provider
     */
    public Types getTypes() {
        return types;
    }

    /**
     * Map the database to that supported by the database provider.
     * 
     * @throws SQLException if the database cannot be mapped
     */
    public Database map() throws SQLException {
        Iterator tables = database.getTables().iterator();
        while (tables.hasNext()) {
            map((Table) tables.next());
        }
        return database;
    }

    protected Table map(Table table) throws SQLException {
        Iterator columns = table.getColumns().iterator();
        while (columns.hasNext()) {
            map(table, (Column) columns.next());
        }

        Iterator foreignKeys = table.getForeignKeys().iterator();
        while (foreignKeys.hasNext()) {
            map(table, (ForeignKey) foreignKeys.next());
        }
        return table;
    }

    protected Column map(Table table, Column column) throws SQLException {
        return column;
    }

    protected ForeignKey map(Table table, ForeignKey key) throws SQLException {
        Iterator references = key.getReferences().iterator();
        while (references.hasNext()) {
            map(table, (Reference) references.next());
        }
        return key;
    }

    protected Reference map(Table table, Reference reference) 
        throws SQLException {
        return reference;
    }
}
