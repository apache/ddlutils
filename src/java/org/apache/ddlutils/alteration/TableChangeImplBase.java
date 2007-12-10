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

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

/**
 * Base class for change implementations.
 * 
 * @version $Revision: $
 */
public abstract class TableChangeImplBase implements TableChange
{
    /** The name of the affected table. */
    private String _tableName;

    /**
     * Creates a new change object.
     * 
     * @param tableName The table's name
     */
    public TableChangeImplBase(String tableName)
    {
        _tableName = tableName;
    }

    /**
     * {@inheritDoc}
     */
    public String getChangedTable()
    {
        return _tableName;
    }

    /**
     * {@inheritDoc}
     */
    public Table findChangedTable(Database model, boolean caseSensitive)
    {
    	return model.findTable(_tableName, caseSensitive);
    }
}
