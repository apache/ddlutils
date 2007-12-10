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

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

/**
 * Represents the removal of the primary key from a table.
 * 
 * @version $Revision: $
 */
public class RemovePrimaryKeyChange extends TableChangeImplBase
{
    /**
     * Creates a new change object.
     * 
     * @param tableName The name of he table to remove the primary key from
     */
    public RemovePrimaryKeyChange(String tableName)
    {
        super(tableName);
    }

    /**
     * {@inheritDoc}
     */
    public void apply(Database model, boolean caseSensitive)
    {
        Table    table  = findChangedTable(model, caseSensitive);
        Column[] pkCols = table.getPrimaryKeyColumns();

        for (int idx = 0; idx < pkCols.length; idx++)
        {
            pkCols[idx].setPrimaryKey(false);
        }
    }
}
