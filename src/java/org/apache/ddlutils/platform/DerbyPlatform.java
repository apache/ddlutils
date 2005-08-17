package org.apache.ddlutils.platform;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.ddlutils.DynaSqlException;

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
 * The platform implementation for Derby.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 231306 $
 */
public class DerbyPlatform extends CloudscapePlatform
{
    /** Database name of this platform */
    public static final String DATABASENAME         = "Derby";
    /** The derby jdbc driver for use as a client for a normal server */
    public static final String JDBC_DRIVER          = "org.apache.derby.jdbc.ClientDriver";
    /** The derby jdbc driver for use as an embedded database */
    public static final String JDBC_DRIVER_EMBEDDED = "org.apache.derby.jdbc.EmbeddedDriver";
    /** The subprotocol used by the derby drivers */
    public static final String JDBC_SUBPROTOCOL     = "derby";

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.Platform#getDatabaseName()
     */
    public String getDatabaseName()
    {
        return DATABASENAME;
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.platform.PlatformImplBase#createDatabase(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void createDatabase(String jdbcDriverClassName, String connectionUrl, String username, String password) throws DynaSqlException, UnsupportedOperationException
    {
        // For Derby, you create databases by simply appending ";create=true" to the connection url
        if (JDBC_DRIVER.equals(jdbcDriverClassName) ||
            JDBC_DRIVER_EMBEDDED.equals(jdbcDriverClassName))
        {
            Connection connection = null;

            try
            {
                Class.forName(jdbcDriverClassName);

                connection = DriverManager.getConnection(connectionUrl + ";create=true", username, password);
                logWarnings(connection);
            }
            catch (Exception ex)
            {
                throw new DynaSqlException("Error while trying to create a database", ex);
            }
            finally
            {
                if (connection != null)
                {
                    try
                    {
                        connection.close();
                    }
                    catch (SQLException ex)
                    {}
                }
            }
        }
        else
        {
            throw new UnsupportedOperationException("Unable to create a Derby database via the driver "+jdbcDriverClassName);
        }
    }
}
