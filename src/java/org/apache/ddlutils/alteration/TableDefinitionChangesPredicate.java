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

import java.util.List;

import org.apache.ddlutils.model.Table;

/**
 * Defines a predicate that allows platforms to state whether all of the given table definition
 * changes (i.e. column changes and primary key changes) are supported by the platform or not.
 * 
 * @version $Revision: $
 */
public interface TableDefinitionChangesPredicate
{
    /**
     * Evaluates the given list of table changes and determines whether they are supported.
     * 
     * @param intermediateTable The current table object which has certain non-table-definition
     *                          changes already applied (those that would come before the give
     *                          list of changes in the result of
     *                          {@link ModelComparator#compare(org.apache.ddlutils.model.Database, org.apache.ddlutils.model.Database)}
     * @param changes The non-empty list of changes
     * @return <code>true</code> if the current plaform supports them
     */
    public boolean areSupported(Table intermediateTable, List changes);
}
