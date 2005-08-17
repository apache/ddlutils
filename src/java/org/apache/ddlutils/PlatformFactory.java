package org.apache.ddlutils;

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

import org.apache.ddlutils.platform.AxionPlatform;
import org.apache.ddlutils.platform.CloudscapePlatform;
import org.apache.ddlutils.platform.Db2Platform;
import org.apache.ddlutils.platform.DerbyPlatform;
import org.apache.ddlutils.platform.FirebirdPlatform;
import org.apache.ddlutils.platform.HsqlDbPlatform;
import org.apache.ddlutils.platform.InterbasePlatform;
import org.apache.ddlutils.platform.MSSqlPlatform;
import org.apache.ddlutils.platform.MaxDbPlatform;
import org.apache.ddlutils.platform.MckoiPlatform;
import org.apache.ddlutils.platform.MySqlPlatform;
import org.apache.ddlutils.platform.Oracle8Platform;
import org.apache.ddlutils.platform.Oracle9Platform;
import org.apache.ddlutils.platform.PostgreSqlPlatform;
import org.apache.ddlutils.platform.SapDbPlatform;
import org.apache.ddlutils.platform.SybasePlatform;

/**
 * A factory of {@link org.apache.ddlutils.Platform} instances based on a case
 * insensitive database name. Note that this is a convenience class as the platforms
 * can also simply be created via their constructors.
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 209952 $
 */
public class PlatformFactory
{
    /** The database name -> platform map */
    private static Map _platforms = new HashMap();

    static
    {
        registerDatabases();
    }

    /**
     * Creates a new platform for the given (case insensitive) database name
     * or returns null if the database is not recognized.
     * 
     * @param databaseName The name of the database (case is not important)
     * @return The platform or <code>null</code> if the database is not supported
     */
    public static synchronized Platform createNewPlatformInstance(String databaseName) throws IllegalAccessException, InstantiationException
    {
        Class platformClass = (Class)_platforms.get(databaseName.toLowerCase());

        return platformClass != null ? (Platform)platformClass.newInstance() : null;
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

        answer.addAll(_platforms.keySet());
        return answer;
    }


    /**
     * Registers a new platform.
     * 
     * @param databaseName  The database name
     * @param platformClass The platform class which must implement the {@link Platform} interface
     */
    public static synchronized void registerDatabase(String databaseName, Class platformClass)
    {
        if (!Platform.class.isAssignableFrom(platformClass))
        {
            throw new IllegalArgumentException("Cannot register class "+platformClass.getName()+" because it does not implement the "+Platform.class.getName()+" interface");
        }
        _platforms.put(databaseName.toLowerCase(), platformClass);        
    }

    /**
     * Registers the predefined builders.
     */
    protected static void registerDatabases()
    {
        registerDatabase(AxionPlatform.DATABASENAME,      AxionPlatform.class);
        registerDatabase(CloudscapePlatform.DATABASENAME, CloudscapePlatform.class);
        registerDatabase(Db2Platform.DATABASENAME,        Db2Platform.class);
        registerDatabase(DerbyPlatform.DATABASENAME,      DerbyPlatform.class);
        registerDatabase(FirebirdPlatform.DATABASENAME,   FirebirdPlatform.class);
        registerDatabase(HsqlDbPlatform.DATABASENAME,     HsqlDbPlatform.class);
        registerDatabase(InterbasePlatform.DATABASENAME,  InterbasePlatform.class);
        registerDatabase(MaxDbPlatform.DATABASENAME,      MaxDbPlatform.class);
        registerDatabase(MckoiPlatform.DATABASENAME,      MckoiPlatform.class);
        registerDatabase(MSSqlPlatform.DATABASENAME,      MSSqlPlatform.class);
        registerDatabase(MySqlPlatform.DATABASENAME,      MySqlPlatform.class);
        registerDatabase(Oracle8Platform.DATABASENAME,    Oracle8Platform.class);
        registerDatabase(Oracle9Platform.DATABASENAME,    Oracle9Platform.class);
        registerDatabase(PostgreSqlPlatform.DATABASENAME, PostgreSqlPlatform.class);
        registerDatabase(SapDbPlatform.DATABASENAME,      SapDbPlatform.class);
        registerDatabase(SybasePlatform.DATABASENAME,     SybasePlatform.class);
    }
}
