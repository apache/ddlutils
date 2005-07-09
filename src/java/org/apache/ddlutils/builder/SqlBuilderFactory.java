package org.apache.ddlutils.builder;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A factory of SqlBuilder instances based on a case insensitive database name.
 * 
 * Ultimately this class could use a discovery mechanism (such as commons-discovery) to find
 * new databases on the classpath.
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision$
 */
public class SqlBuilderFactory
{
    private static Map databases = new HashMap();

    static
    {
        registerDatabases();
    }

    /**
     * Creates a new SqlBuilder for the given (case insensitive) database name
     * or returns null if the database is not recognized.
     * 
     * @param databaseName The name of the database (case is not important)
     * @return The builder or <code>null</code> if the database is not supported
     */
    public static synchronized SqlBuilder newSqlBuilder(String databaseName) throws IllegalAccessException, InstantiationException
    {
        Class builderClass = (Class) databases.get(databaseName.toLowerCase());

        if (builderClass != null)
        {
            return (SqlBuilder)builderClass.newInstance();
        }
        return null;
    }

    /**
     * Returns a list of all supported databases.
     * 
     * @return The currently registered database types
     */
    public static synchronized List getDatabaseTypes()
    {
        // return a copy to prevent modification
        List answer = new ArrayList();

        answer.addAll(databases.keySet());
        return answer;
    }


    /**
     * Registers a builder.
     * 
     * @param databaseName    The database name
     * @param sqlBuilderClass The builder class
     */
    public static synchronized void registerDatabase(String databaseName, Class sqlBuilderClass)
    {
        databases.put(databaseName.toLowerCase(), sqlBuilderClass);        
    }

    /**
     * Registers the predefined builders.
     */
    protected static void registerDatabases()
    {
        registerDatabase(AxionBuilder.DATABASENAME,      AxionBuilder.class);
        registerDatabase(CloudscapeBuilder.DATABASENAME, CloudscapeBuilder.class);
        registerDatabase(Db2Builder.DATABASENAME,        Db2Builder.class);
        registerDatabase(DerbyBuilder.DATABASENAME,      DerbyBuilder.class);
        registerDatabase(FirebirdBuilder.DATABASENAME,   FirebirdBuilder.class);
        registerDatabase(HsqlDbBuilder.DATABASENAME,     HsqlDbBuilder.class);
        registerDatabase(InterbaseBuilder.DATABASENAME,  InterbaseBuilder.class);
        registerDatabase(MaxDbBuilder.DATABASENAME,      MaxDbBuilder.class);
        registerDatabase(MckoiSqlBuilder.DATABASENAME,   MckoiSqlBuilder.class);
        registerDatabase(MSSqlBuilder.DATABASENAME,      MSSqlBuilder.class);
        registerDatabase(MySqlBuilder.DATABASENAME,      MySqlBuilder.class);
        registerDatabase(OracleBuilder.DATABASENAME,     OracleBuilder.class);
        registerDatabase(PostgreSqlBuilder.DATABASENAME, PostgreSqlBuilder.class);
        registerDatabase(SapDbBuilder.DATABASENAME,      SapDbBuilder.class);
        registerDatabase(SybaseBuilder.DATABASENAME,     SybaseBuilder.class);
    }
}
