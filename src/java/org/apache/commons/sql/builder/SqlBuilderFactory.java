/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.commons.sql.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A factory of SqlBuilder instances based on a case insensitive database name.
 * 
 * Ultimately this class could use a discovery mechanism (such as commons-discovery) to find
 * new databases on the classpath.
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.14 $
 */
public class SqlBuilderFactory {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(SqlBuilderFactory.class);

    private static Map databases = new HashMap();
        
    static {
        registerDatabases();
    }

    /**
     * Creates a new SqlBuilder for the given (case insensitive) database name
     * or returns null if the database is not recognized.
     */
    public static synchronized SqlBuilder newSqlBuilder(String databaseName) 
        throws IllegalAccessException, InstantiationException {
            
        Class theClass = (Class) databases.get(databaseName.toLowerCase());
        if (theClass != null) {
            return (SqlBuilder) theClass.newInstance();
        }
        return null;
    }

    /**
     * @return a List of currently registered database types for which there is a
     * specific SqlBuilder.
     */
    public static synchronized List getDatabaseTypes() {
        // return a copy to prevent modification
        List answer = new ArrayList();
        answer.addAll( databases.keySet());
        return answer;
    }


    /**
     * Register the common builders
     */
    public static synchronized void registerDatabase(String databaseName, Class sqlBuilderClass) {
        databases.put(databaseName.toLowerCase(), sqlBuilderClass);        
    }

    /**
     * Register the common builders
     */
    protected static void registerDatabases() {
        registerDatabase("axion", AxionBuilder.class);
        registerDatabase("db2", Db2Builder.class);
        registerDatabase("hsqldb", HsqlDbBuilder.class);
        registerDatabase("mckoi", MckoiSqlBuilder.class);
        registerDatabase("mssql", MSSqlBuilder.class);
        registerDatabase("mysql", MySqlBuilder.class);
        registerDatabase("oracle", OracleBuilder.class);
        registerDatabase("postgresql", PostgreSqlBuilder.class);
        registerDatabase("sybase", SybaseBuilder.class);
    }
}
