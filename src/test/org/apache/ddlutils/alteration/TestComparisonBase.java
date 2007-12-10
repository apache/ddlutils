package org.apache.ddlutils.alteration;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.sql.Types;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.TestBase;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.TestPlatform;

/**
 * Base class for model comparison tests.
 * 
 * @version $Revision: $
 */
public abstract class TestComparisonBase extends TestBase
{
    /**
     * Creates a new platform object.
     *
     * @param delimitedIdentifierModeOn Whether delimited identifiers shall be used
     * @return The platform object
     */
    protected Platform getPlatform(boolean delimitedIdentifierModeOn)
    {
        TestPlatform platform = new TestPlatform() {
            protected TableDefinitionChangesPredicate getTableDefinitionChangesPredicate()
            {
                return null;
            }
        };

        PlatformInfo platformInfo = platform.getPlatformInfo();

        platform.setDelimitedIdentifierModeOn(delimitedIdentifierModeOn);
        platformInfo.setHasSize(Types.DECIMAL, true);
        platformInfo.setHasSize(Types.NUMERIC, true);
        platformInfo.setHasSize(Types.CHAR, true);
        platformInfo.setHasSize(Types.VARCHAR, true);

        return platform;
    }

    /**
     * Asserts the given table.
     * 
     * @param name        The expected name
     * @param description The expected description
     * @param columnCount The expected number of columns
     * @param fkCount     The expected number of foreign keys
     * @param indexCount  The expected number of indexes
     * @param table       The table to assert
     */
    protected void assertTable(String name, String description, int columnCount, int fkCount, int indexCount, Table table)
    {
        assertEquals(name,
                     table.getName());
        assertEquals(description,
                     table.getDescription());
        assertEquals(columnCount,
                     table.getColumnCount());
        assertEquals(fkCount,
                     table.getForeignKeyCount());
        assertEquals(indexCount,
                     table.getIndexCount());
    }

    /**
     * Asserts the given column.
     * 
     * @param name            The expected name
     * @param typeCode        The expected type code
     * @param sizeSpec        The expected size
     * @param defaultValue    The expected default value
     * @param isPrimaryKey    The expected primary key status
     * @param isRequired      The expected required status
     * @param isAutoIncrement The expected auto increment status
     * @param column          The column to assert
     */
    protected void assertColumn(String  name,
                                int     typeCode,
                                String  sizeSpec,
                                String  defaultValue,
                                boolean isPrimaryKey,
                                boolean isRequired,
                                boolean isAutoIncrement,
                                Column  column)
    {
        assertEquals(name,
                     column.getName());
        assertEquals(typeCode,
                     column.getTypeCode());
        assertEquals(sizeSpec,
                     column.getSize());
        assertEquals(defaultValue,
                     column.getDefaultValue());
        assertEquals(isPrimaryKey,
                     column.isPrimaryKey());
        assertEquals(isRequired,
                     column.isRequired());
        assertEquals(isAutoIncrement,
                     column.isAutoIncrement());
    }

    /**
     * Asserts the given index.
     * 
     * @param name         The expected name
     * @param isUnique     Whether the index is expected to be a unique index
     * @param indexColumns The names of the columns expected to be in the index
     * @param index        The index to assert
     */
    protected void assertIndex(String name, boolean isUnique, String[] indexColumns, Index index)
    {
        assertEquals(name,
                     index.getName());
        assertEquals(isUnique,
                     index.isUnique());
        assertEquals(indexColumns.length,
                     index.getColumnCount());
        for (int idx = 0; idx < indexColumns.length; idx++)
        {
            assertEquals(indexColumns[idx],
                         index.getColumn(idx).getName());
            assertEquals(indexColumns[idx],
                         index.getColumn(idx).getColumn().getName());
        }
    }

    /**
     * Asserts the given foreign key.
     * 
     * @param name               The expected name
     * @param targetTableName    The name of the expected target table
     * @param localColumnNames   The names of the expected local columns
     * @param foreignColumnNames The names of the expected foreign columns
     * @param fk                 The foreign key to assert
     */
    protected void assertForeignKey(String name, String targetTableName, String[] localColumnNames, String[] foreignColumnNames, ForeignKey fk)
    {
        assertEquals(name,
                     fk.getName());
        assertEquals(targetTableName,
                     fk.getForeignTable().getName());
        assertEquals(localColumnNames.length,
                     fk.getReferenceCount());
        for (int idx = 0; idx < localColumnNames.length; idx++)
        {
            assertEquals(localColumnNames[idx],
                         fk.getReference(idx).getLocalColumnName());
            assertEquals(foreignColumnNames[idx],
                         fk.getReference(idx).getForeignColumnName());
        }
    }
}
