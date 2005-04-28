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
import java.util.List;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.type.Mapping;
import org.apache.ddlutils.type.TypeMapping;
import org.apache.ddlutils.type.Types;


/**
 * Default implementation of the {@link DatabaseMapper} interface.
 * This version does not transform the database, but verifies 
 * that the columns are supported by the target database provider.
 * 
 * @version     1.1 2003/02/05 08:08:36
 * @author      <a href="mailto:tima@intalio.com">Tim Anderson</a>
 */
public class DefaultDatabaseMapper extends AbstractDatabaseMapper {

    /**
     * Map the database to that supported by the database provider.
     * 
     * @throws SQLException if the database cannot be mapped
     */
    public Database map() throws SQLException {
        Database database = getDatabase();

        // map all auto-increment columns first        
        Iterator tables = database.getTables().iterator();
        while (tables.hasNext()) {
            Table table = (Table) tables.next();
            if (table.getAutoIncrementColumn() != null) {
                mapAutoIncrementColumn(table);
            }
        }
        return super.map();
    }

    protected Column map(Table table, Column column) throws SQLException {
        if (!column.isAutoIncrement()) {
            mapColumn(table, column);
        }

        return super.map(table, column);
    }

    protected void mapColumn(Table table, Column column) throws SQLException {
        Types types = getTypes();
        String name = column.getType();
        int size = column.getSizeAsInt();
        
        TypeMapping mapping = types.getTypeMapping(name, size);
        if (mapping == null) {
            mapping = types.promote(name, size);
        }
        if (mapping == null) {
            throw new SQLException("Column not supported: " + column);
        }
        if (mapping.getSize() != 0 && column.getSizeAsInt() > mapping.getSize()) {
            // if the target type specifies a size of zero, and the
            // requested column specifies > 0, just ignore - assume that
            // the type has unlimited size
            throw new SQLException("Column exceeds size of type: " + column);
        }
        promoteColumn(table, column, mapping);
    }


    protected void mapAutoIncrementColumn(Table table) throws SQLException {
        Types types = getTypes();

        Column column = table.getAutoIncrementColumn();
        String name = column.getType();
        int size = column.getSizeAsInt();

        if (!types.getAutoIncrementMappings().isEmpty()) {
            // database provider supports auto-increment columns - 
            // make sure the column is supported directly, or can be mapped
            // to one that does
            TypeMapping mapping = types.getAutoIncrementMapping(name, size);
            if (mapping == null) {
                TypeMapping promoted = null;
                Iterator mappings = 
                    types.getAutoIncrementMappings().iterator();
                while (mappings.hasNext()) {
                    Mapping autoIncMapping = (Mapping) mappings.next();
                    promoted = types.promote(name, autoIncMapping.getName(), 
                                             size);
                    if (promoted != null) {
                        break;
                    }
                }
                if (promoted == null) {
                    throw new SQLException("Auto-increment column " + column + 
                                           " not supported by database");
                }
                promoteColumn(table, column, promoted);
            }
        } else {
            // database provider doesn't support auto-increment columns 
            // directly. Verify that the column type is supported, and leave
            // it to the DDL generation to handle the auto-increment
            mapColumn(table, column);
        }
    }

    /**
     * Promote a column to the specified type mapping, and update
     * all foreign keys which reference the column
     */
    protected void promoteColumn(Table table, Column column, 
                                 TypeMapping mapping) {
        column.setType(mapping.getName());
        if (mapping.getSize() < column.getSizeAsInt()) {
            column.setSize(String.valueOf(mapping.getSize())); 
            // @todo - oracle returns column sizes of type long
        }
        
        Database database = getDatabase();
        Iterator tables = database.getTables().iterator();
        while (tables.hasNext()) {
            Table referencingTable = (Table) tables.next();
            List references = ModelHelper.getReferences(
                referencingTable, table, column.getName());
            
            Iterator iterator = references.iterator();
            while (iterator.hasNext()) {
                Reference reference = (Reference) iterator.next();
                Column referencingColumn = ModelHelper.getColumn(
                    referencingTable, reference.getLocal());
                referencingColumn.setType(column.getType());
                referencingColumn.setSize(column.getSize());
            }
        }
    }
        
}
