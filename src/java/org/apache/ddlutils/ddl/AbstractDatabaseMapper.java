package org.apache.ddlutils.ddl;

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

import java.sql.SQLException;
import java.util.Iterator;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.type.Types;


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
