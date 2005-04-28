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

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.type.Types;


/**
 * Interface to map a database schema to that supported by a particular
 * database provider
 * 
 * @version     1.1 2003/02/05 08:08:36
 * @author      <a href="mailto:tima@intalio.com">Tim Anderson</a>
 */
public interface DatabaseMapper {

    /**
     * Set the database to be mapped
     */
    public void setDatabase(Database database);

    /**
     * Returns the database to be mapped
     */
    public Database getDatabase();

    /**
     * Set the type set supported by the database provider
     */
    public void setTypes(Types types);

    /**
     * Returns the type set supported by the database provider
     */
    public Types getTypes();

    /**
     * Map the database to that supported by the database provider
     *
     * @throws SQLException if the database cannot be mapped
     */
    public Database map() throws SQLException;

}
