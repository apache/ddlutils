package org.apache.ddlutils;

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
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;

import javax.sql.DataSource;

import org.apache.ddlutils.platform.AxionPlatform;
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
import org.apache.ddlutils.platform.PostgreSqlPlatform;
import org.apache.ddlutils.platform.SapDbPlatform;
import org.apache.ddlutils.platform.SybasePlatform;

/**
 * Utility functions for dealing with the database platforms.
 */
public class PlatformUtils
{
    // Extended drivers that support more than one database

    /** The i-net DB2 jdbc driver */
    public static final String JDBC_DRIVER_INET_DB2              = "com.inet.drda.DRDADriver";
    /** The i-net Oracle jdbc driver */
    public static final String JDBC_DRIVER_INET_ORACLE           = "com.inet.ora.OraDriver";
    /** The i-net SQLServer jdbc driver */
    public static final String JDBC_DRIVER_INET_SQLSERVER        = "com.inet.tds.TdsDriver";
    /** The i-net Sybase jdbc driver */
    public static final String JDBC_DRIVER_INET_SYBASE           = "com.inet.syb.SybDriver";
    /** The i-net pooled jdbc driver for SQLServer and Sybase */
    public static final String JDBC_DRIVER_INET_POOLED           = "com.inet.pool.PoolDriver";
    /** The JNetDirect SQLServer jdbc driver */
    public static final String JDBC_DRIVER_JSQLCONNECT_SQLSERVER = "com.jnetdirect.jsql.JSQLDriver";
    /** The jTDS jdbc driver for SQLServer and Sybase */
    public static final String JDBC_DRIVER_JTDS                  = "net.sourceforge.jtds.jdbc.Driver";

    /** The subprotocol used by the i-net DB2 driver */
    public static final String JDBC_SUBPROTOCOL_INET_DB2                = "inetdb2";
    /** The subprotocol used by the i-net Oracle driver */
    public static final String JDBC_SUBPROTOCOL_INET_ORACLE             = "inetora";
    /** A subprotocol used by the i-net SQLServer driver */
    public static final String JDBC_SUBPROTOCOL_INET_SQLSERVER          = "inetdae";
    /** A subprotocol used by the i-net SQLServer driver */
    public static final String JDBC_SUBPROTOCOL_INET_SQLSERVER6         = "inetdae6";
    /** A subprotocol used by the i-net SQLServer driver */
    public static final String JDBC_SUBPROTOCOL_INET_SQLSERVER7         = "inetdae7";
    /** A subprotocol used by the i-net SQLServer driver */
    public static final String JDBC_SUBPROTOCOL_INET_SQLSERVER7A        = "inetdae7a";
    /** A subprotocol used by the pooled i-net SQLServer driver */
    public static final String JDBC_SUBPROTOCOL_INET_SQLSERVER_POOLED   = "inetpool:inetdae";
    /** A subprotocol used by the pooled i-net SQLServer driver */
    public static final String JDBC_SUBPROTOCOL_INET_SQLSERVER6_POOLED  = "inetpool:inetdae6";
    /** A subprotocol used by the pooled i-net SQLServer driver */
    public static final String JDBC_SUBPROTOCOL_INET_SQLSERVER7_POOLED  = "inetpool:inetdae7";
    /** A subprotocol used by the pooled i-net SQLServer driver */
    public static final String JDBC_SUBPROTOCOL_INET_SQLSERVER7A_POOLED = "inetpool:inetdae7a";
    /** The subprotocol used by the i-net Sybase driver */
    public static final String JDBC_SUBPROTOCOL_INET_SYBASE             = "inetsyb";
    /** The subprotocol used by the pooled i-net Sybase driver */
    public static final String JDBC_SUBPROTOCOL_INET_SYBASE_POOLED      = "inetpool:inetsyb";
    /** The subprotocol used by the JNetDirect SQLServer driver */
    public static final String JDBC_SUBPROTOCOL_JSQLCONNECT_SQLSERVER   = "JSQLConnect";
    /** The subprotocol used by the jTDS SQLServer driver */
    public static final String JDBC_SUBPROTOCOL_JTDS_SQLSERVER          = "jtds:sqlserver";
    /** The subprotocol used by the jTDS Sybase driver */
    public static final String JDBC_SUBPROTOCOL_JTDS_SYBASE             = "jtds:sybase";

    /** Maps the sub-protocl part of a jdbc connection url to a OJB platform name */
    private HashMap jdbcSubProtocolToPlatform = new HashMap();
    /** Maps the jdbc driver name to a OJB platform name */
    private HashMap jdbcDriverToPlatform      = new HashMap();

    /**
     * Creates a new instance.
     */
    public PlatformUtils()
    {
        // Note that currently Sapdb and MaxDB have equal subprotocols and
        // drivers so we have no means to distinguish them
        jdbcSubProtocolToPlatform.put(AxionPlatform.JDBC_SUBPROTOCOL,                         AxionPlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(Db2Platform.JDBC_SUBPROTOCOL,                           Db2Platform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(PlatformUtils.JDBC_SUBPROTOCOL_INET_DB2,                Db2Platform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(DerbyPlatform.JDBC_SUBPROTOCOL,                         DerbyPlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(FirebirdPlatform.JDBC_SUBPROTOCOL,                      FirebirdPlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(HsqlDbPlatform.JDBC_SUBPROTOCOL,                        HsqlDbPlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(InterbasePlatform.JDBC_SUBPROTOCOL,                     InterbasePlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SapDbPlatform.JDBC_SUBPROTOCOL,                         MaxDbPlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(MckoiPlatform.JDBC_SUBPROTOCOL,                         MckoiPlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(MSSqlPlatform.JDBC_SUBPROTOCOL,                         MSSqlPlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(PlatformUtils.JDBC_SUBPROTOCOL_INET_SQLSERVER,          MSSqlPlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(PlatformUtils.JDBC_SUBPROTOCOL_INET_SQLSERVER6,         MSSqlPlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(PlatformUtils.JDBC_SUBPROTOCOL_INET_SQLSERVER7,         MSSqlPlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(PlatformUtils.JDBC_SUBPROTOCOL_INET_SQLSERVER7A,        MSSqlPlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(PlatformUtils.JDBC_SUBPROTOCOL_INET_SQLSERVER_POOLED,   MSSqlPlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(PlatformUtils.JDBC_SUBPROTOCOL_INET_SQLSERVER6_POOLED,  MSSqlPlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(PlatformUtils.JDBC_SUBPROTOCOL_INET_SQLSERVER7_POOLED,  MSSqlPlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(PlatformUtils.JDBC_SUBPROTOCOL_INET_SQLSERVER7A_POOLED, MSSqlPlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(PlatformUtils.JDBC_SUBPROTOCOL_JTDS_SQLSERVER,          MSSqlPlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(MySqlPlatform.JDBC_SUBPROTOCOL,                         MySqlPlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(Oracle8Platform.JDBC_SUBPROTOCOL,                       Oracle8Platform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(PlatformUtils.JDBC_SUBPROTOCOL_INET_ORACLE,             Oracle8Platform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(PostgreSqlPlatform.JDBC_SUBPROTOCOL,                    PostgreSqlPlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SybasePlatform.JDBC_SUBPROTOCOL,                        SybasePlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(PlatformUtils.JDBC_SUBPROTOCOL_INET_SYBASE,             SybasePlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(PlatformUtils.JDBC_SUBPROTOCOL_INET_SYBASE_POOLED,      SybasePlatform.DATABASENAME);
        jdbcSubProtocolToPlatform.put(PlatformUtils.JDBC_SUBPROTOCOL_JTDS_SYBASE,             SybasePlatform.DATABASENAME);

        jdbcDriverToPlatform.put(AxionPlatform.JDBC_DRIVER,                       AxionPlatform.DATABASENAME);
        jdbcDriverToPlatform.put(Db2Platform.JDBC_DRIVER,                         Db2Platform.DATABASENAME);
        jdbcDriverToPlatform.put(PlatformUtils.JDBC_DRIVER_INET_DB2,              Db2Platform.DATABASENAME);
        jdbcDriverToPlatform.put(DerbyPlatform.JDBC_DRIVER_EMBEDDED,              DerbyPlatform.DATABASENAME);
        jdbcDriverToPlatform.put(DerbyPlatform.JDBC_DRIVER,                       DerbyPlatform.DATABASENAME);
        jdbcDriverToPlatform.put(FirebirdPlatform.JDBC_DRIVER,                    FirebirdPlatform.DATABASENAME);
        jdbcDriverToPlatform.put(HsqlDbPlatform.JDBC_DRIVER,                      HsqlDbPlatform.DATABASENAME);
        jdbcDriverToPlatform.put(InterbasePlatform.JDBC_DRIVER,                   InterbasePlatform.DATABASENAME);
        jdbcDriverToPlatform.put(SapDbPlatform.JDBC_DRIVER,                       MaxDbPlatform.DATABASENAME);
        jdbcDriverToPlatform.put(MckoiPlatform.JDBC_DRIVER,                       MckoiPlatform.DATABASENAME);
        jdbcDriverToPlatform.put(MSSqlPlatform.JDBC_DRIVER,                       MSSqlPlatform.DATABASENAME);
        jdbcDriverToPlatform.put(PlatformUtils.JDBC_DRIVER_INET_SQLSERVER,        MSSqlPlatform.DATABASENAME);
        jdbcDriverToPlatform.put(PlatformUtils.JDBC_DRIVER_JSQLCONNECT_SQLSERVER, MSSqlPlatform.DATABASENAME);
        jdbcDriverToPlatform.put(MySqlPlatform.JDBC_DRIVER,                       MySqlPlatform.DATABASENAME);
        jdbcDriverToPlatform.put(MySqlPlatform.JDBC_DRIVER_OLD,                   MySqlPlatform.DATABASENAME);
        jdbcDriverToPlatform.put(Oracle8Platform.JDBC_DRIVER,                     Oracle8Platform.DATABASENAME);
        jdbcDriverToPlatform.put(PlatformUtils.JDBC_DRIVER_INET_ORACLE,           Oracle8Platform.DATABASENAME);
        jdbcDriverToPlatform.put(PostgreSqlPlatform.JDBC_DRIVER,                  PostgreSqlPlatform.DATABASENAME);
        jdbcDriverToPlatform.put(SybasePlatform.JDBC_DRIVER,                      SybasePlatform.DATABASENAME);
        jdbcDriverToPlatform.put(SybasePlatform.JDBC_DRIVER_OLD,                  SybasePlatform.DATABASENAME);
        jdbcDriverToPlatform.put(PlatformUtils.JDBC_DRIVER_INET_SYBASE,           SybasePlatform.DATABASENAME);
    }

    /**
     * Tries to determine the database type for the given data source. Note that this will establish
     * a connection to the database.
     * 
     * @param dataSource The data source
     * @return The database type or <code>null</code> if the database type couldn't be determined
     */
    public String determineDatabaseType(DataSource dataSource) throws DynaSqlException
    {
        Connection connection = null;

        try
        {
            connection = dataSource.getConnection();
            
            DatabaseMetaData metaData = connection.getMetaData();

            return determineDatabaseType(metaData.getDriverName(), metaData.getURL());
        }
        catch (SQLException ex)
        {
            throw new DynaSqlException("Error while reading the database metadata", ex);
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
                {
                    // we ignore this one
                }
            }
        }
    }

    /**
     * Tries to determine the database type for the given jdbc driver and connection url.
     * 
     * @param driverClassName   The fully qualified name of the JDBC driver 
     * @param jdbcConnectionUrl The connection url
     * @return The database type or <code>null</code> if the database type couldn't be determined
     */
    public String determineDatabaseType(String driverName, String jdbcConnectionUrl)
    {
        if (jdbcDriverToPlatform.containsKey(driverName))
        {
            return (String)jdbcDriverToPlatform.get(driverName);
        }
        if (jdbcConnectionUrl == null)
        {
            return null;
        }

        int pos     = jdbcConnectionUrl.indexOf(':');
        int lastPos = pos;

        // we're skipping over the 'jdbc'
        lastPos = pos;
        pos     = jdbcConnectionUrl.indexOf(':', lastPos + 1);

        String subProtocol = jdbcConnectionUrl.substring(lastPos + 1, pos);

        // there are a few jdbc drivers that have a subprotocol containing one or more ':'
        if ("inetpool".equals(subProtocol))
        {
            // Possible forms are:
            //   inetpool:<subprotocol>
            //   inetpool:jdbc:<subprotocol>   (where we'll remove the 'jdbc' part)
            
            int tmpPos = jdbcConnectionUrl.indexOf(':', pos + 1);

            if ("inetpool:jdbc".equals(jdbcConnectionUrl.substring(lastPos + 1, tmpPos)))
            {
                pos    = tmpPos;
                tmpPos = jdbcConnectionUrl.indexOf(':', pos + 1);
            }
            subProtocol += ":" + jdbcConnectionUrl.substring(pos + 1, tmpPos);
        }
        else if ("jtds".equals(subProtocol) ||
                 "microsoft".equals(subProtocol) ||
                 "sybase".equals(subProtocol))
        {
            pos         = jdbcConnectionUrl.indexOf(':', pos + 1);
            subProtocol = ":" + jdbcConnectionUrl.substring(lastPos + 1, pos);
        }

        return (String)jdbcSubProtocolToPlatform.get(subProtocol);
    }

}
