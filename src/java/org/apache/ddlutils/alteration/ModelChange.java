package org.apache.ddlutils.alteration;

import org.apache.ddlutils.model.Database;

/*
 * Copyright 2006 The Apache Software Foundation.
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

/**
 * Marker interface for changes to a database model element.
 * 
 * @version $Revision: $
 */
public interface ModelChange
{
    /**
     * Applies this change to the given database.
     * 
     * @param database The database
     */
    public void apply(Database database);
}
