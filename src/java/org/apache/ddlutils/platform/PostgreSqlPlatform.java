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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.apache.ddlutils.DynaSqlException;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.builder.PostgreSqlBuilder;

/**
 * The platform implementation for PostgresSql.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 231306 $
 */
public class PostgreSqlPlatform extends PlatformImplBase
{
    /** Database name of this platform */
    public static final String DATABASENAME      = "PostgreSql";
    /** The standard PostgreSQL jdbc driver */
    public static final String JDBC_DRIVER       = "org.postgresql.Driver";
    /** The subprotocol used by the standard PostgreSQL driver */
    public static final String JDBC_SUBPROTOCOL  = "postgresql";

    /**
     * Creates a new platform instance.
     */
    public PostgreSqlPlatform()
    {
        PlatformInfo info = new PlatformInfo();

        // this is the default length though it might be changed when building PostgreSQL
        // in file src/include/postgres_ext.h
        info.setMaxIdentifierLength(31);
        info.setRequiringNullAsDefaultValue(false);
        info.setPrimaryKeyEmbedded(true);
        info.setForeignKeysEmbedded(false);
        info.setIndicesEmbedded(false);
        info.addNativeTypeMapping(Types.ARRAY,         "BYTEA");
        info.addNativeTypeMapping(Types.BINARY,        "BYTEA");
        info.addNativeTypeMapping(Types.BIT,           "BOOLEAN");
        info.addNativeTypeMapping(Types.BLOB,          "BYTEA");
        info.addNativeTypeMapping(Types.CLOB,          "TEXT");
        info.addNativeTypeMapping(Types.DECIMAL,       "NUMERIC");
        info.addNativeTypeMapping(Types.DISTINCT,      "BYTEA");
        info.addNativeTypeMapping(Types.DOUBLE,        "DOUBLE PRECISION");
        info.addNativeTypeMapping(Types.FLOAT,         "DOUBLE PRECISION");
        info.addNativeTypeMapping(Types.JAVA_OBJECT,   "BYTEA");
        info.addNativeTypeMapping(Types.LONGVARBINARY, "BYTEA");
        info.addNativeTypeMapping(Types.LONGVARCHAR,   "TEXT");
        info.addNativeTypeMapping(Types.NULL,          "BYTEA");
        info.addNativeTypeMapping(Types.OTHER,         "BYTEA");
        info.addNativeTypeMapping(Types.REF,           "BYTEA");
        info.addNativeTypeMapping(Types.STRUCT,        "BYTEA");
        info.addNativeTypeMapping(Types.TINYINT,       "SMALLINT");
        info.addNativeTypeMapping(Types.VARBINARY,     "BYTEA");
        info.addNativeTypeMapping("DATALINK", "BYTEA");

        // no support for specifying the size for these types
        info.setHasSize(Types.BINARY, false);
        info.setHasSize(Types.VARBINARY, false);

        setSqlBuilder(new PostgreSqlBuilder(info));
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.builder.Platform#getDatabaseName()
     */
    public String getDatabaseName()
    {
        return DATABASENAME;
    }

    /**
     * Creates or drops the database referenced by the given connection url.
     * 
     * @param jdbcDriverClassName The jdbc driver class name
     * @param connectionUrl       The url to connect to the database if it were already created
     * @param username            The username for creating the database
     * @param password            The password for creating the database
     * @param createDb            Whether to create or drop the database
     */
    private void createOrDropDatabase(String jdbcDriverClassName, String connectionUrl, String username, String password, boolean createDb) throws DynaSqlException, UnsupportedOperationException
    {
        if (JDBC_DRIVER.equals(jdbcDriverClassName))
        {
            int slashPos = connectionUrl.lastIndexOf('/');

            if (slashPos < 0)
            {
                throw new DynaSqlException("Cannot parse the given connection url "+connectionUrl);
            }

            int        paramPos   = connectionUrl.lastIndexOf('?');
            String     dbName     = (paramPos > slashPos ? connectionUrl.substring(slashPos + 1, paramPos) : connectionUrl.substring(slashPos + 1));
            Connection connection = null;
            Statement  stmt       = null;

            try
            {
                Class.forName(jdbcDriverClassName);

                connection = DriverManager.getConnection(connectionUrl.substring(0, slashPos + 1) + "template1", username, password);
                stmt       = connection.createStatement();
                if (createDb)
                {
                    stmt.execute("CREATE DATABASE "+dbName);
                }
                else
                {
                    stmt.execute("DROP DATABASE "+dbName);
                }
                logWarnings(connection);
            }
            catch (Exception ex)
            {
                throw new DynaSqlException("Error while trying to " + (createDb ? "create" : "drop") + " a database: "+ex.getLocalizedMessage(), ex);
            }
            finally
            {
                if (stmt != null)
                {
                    try
                    {
                        stmt.close();
                    }
                    catch (SQLException ex)
                    {}
                }
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
            throw new UnsupportedOperationException("Unable to " + (createDb ? "create" : "drop") + " a PostgreSQL database via the driver "+jdbcDriverClassName);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.platform.PlatformImplBase#createDatabase(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void createDatabase(String jdbcDriverClassName, String connectionUrl, String username, String password) throws DynaSqlException, UnsupportedOperationException
    {
        // With PostgreSQL, you create a database by executing "CREATE DATABASE" in an existing database (usually 
        // the template1 database because it usually exists)
        createOrDropDatabase(jdbcDriverClassName, connectionUrl, username, password, true);
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.platform.PlatformImplBase#dropDatabase(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void dropDatabase(String jdbcDriverClassName, String connectionUrl, String username, String password) throws DynaSqlException, UnsupportedOperationException
    {
        // With PostgreSQL, you create a database by executing "DROP DATABASE" in an existing database (usually 
        // the template1 database because it usually exists)
        createOrDropDatabase(jdbcDriverClassName, connectionUrl, username, password, false);
    }

    
}
