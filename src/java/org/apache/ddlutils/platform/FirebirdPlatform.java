package org.apache.ddlutils.platform;

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

/**
 * The platform implementation for the Firebird database.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 231306 $
 */
public class FirebirdPlatform extends InterbasePlatform
{
    /** Database name of this platform */
    public static final String DATABASENAME     = "Firebird";
    /** The standard Firebird jdbc driver */
    public static final String JDBC_DRIVER      = "org.firebirdsql.jdbc.FBDriver";
    /** The subprotocol used by the standard Firebird driver */
    public static final String JDBC_SUBPROTOCOL = "firebirdsql";

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.Platform#getDatabaseName()
     */
    public String getDatabaseName()
    {
        return DATABASENAME;
    }
}
