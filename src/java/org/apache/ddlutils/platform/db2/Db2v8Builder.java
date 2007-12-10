package org.apache.ddlutils.platform.db2;

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

import java.io.IOException;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;

/**
 * The SQL Builder for DB2 v8 and above.
 * 
 * @version $Revision: $
 */
public class Db2v8Builder extends Db2Builder
{
    /**
     * Creates a new builder instance.
     * 
     * @param platform The plaftform this builder belongs to
     */
    public Db2v8Builder(Platform platform)
    {
        super(platform);
    }

    /**
     * Generates the SQL to drop a column from a table.
     * 
     * @param table  The table where to drop the column from
     * @param column The column to drop
     */
    public void dropColumn(Table table, Column column) throws IOException
    {
        super.dropColumn(table, column);
        print("CALL ADMIN_CMD('REORG TABLE ");
        printIdentifier(getTableName(table));
        print("')");
        printEndOfStatement();
    }
}
