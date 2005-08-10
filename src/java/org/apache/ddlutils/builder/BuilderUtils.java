package org.apache.ddlutils.builder;

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

/**
 * Utility functions for dealing with builders.
 */
public class BuilderUtils
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
    public BuilderUtils()
    {
        // Note that currently Sapdb and MaxDB have equal subprotocols and
        // drivers so we have no means to distinguish them
        jdbcSubProtocolToPlatform.put(AxionBuilder.JDBC_SUBPROTOCOL,                         AxionBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(Db2Builder.JDBC_SUBPROTOCOL,                           Db2Builder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(BuilderUtils.JDBC_SUBPROTOCOL_INET_DB2,                Db2Builder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(DerbyBuilder.JDBC_SUBPROTOCOL,                         DerbyBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(FirebirdBuilder.JDBC_SUBPROTOCOL,                      FirebirdBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(HsqlDbBuilder.JDBC_SUBPROTOCOL,                        HsqlDbBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(InterbaseBuilder.JDBC_SUBPROTOCOL,                     InterbaseBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SapDbBuilder.JDBC_SUBPROTOCOL,                         MaxDbBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(MckoiSqlBuilder.JDBC_SUBPROTOCOL,                      MckoiSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(MSSqlBuilder.JDBC_SUBPROTOCOL,                         MSSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(BuilderUtils.JDBC_SUBPROTOCOL_INET_SQLSERVER,          MSSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(BuilderUtils.JDBC_SUBPROTOCOL_INET_SQLSERVER6,         MSSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(BuilderUtils.JDBC_SUBPROTOCOL_INET_SQLSERVER7,         MSSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(BuilderUtils.JDBC_SUBPROTOCOL_INET_SQLSERVER7A,        MSSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(BuilderUtils.JDBC_SUBPROTOCOL_INET_SQLSERVER_POOLED,   MSSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(BuilderUtils.JDBC_SUBPROTOCOL_INET_SQLSERVER6_POOLED,  MSSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(BuilderUtils.JDBC_SUBPROTOCOL_INET_SQLSERVER7_POOLED,  MSSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(BuilderUtils.JDBC_SUBPROTOCOL_INET_SQLSERVER7A_POOLED, MSSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(BuilderUtils.JDBC_SUBPROTOCOL_JTDS_SQLSERVER,          MSSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(MySqlBuilder.JDBC_SUBPROTOCOL,                         MySqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(OracleBuilder.JDBC_SUBPROTOCOL,                        OracleBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(BuilderUtils.JDBC_SUBPROTOCOL_INET_ORACLE,             OracleBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(PostgreSqlBuilder.JDBC_SUBPROTOCOL,                    PostgreSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SybaseBuilder.JDBC_SUBPROTOCOL,                        SybaseBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(BuilderUtils.JDBC_SUBPROTOCOL_INET_SYBASE,             SybaseBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(BuilderUtils.JDBC_SUBPROTOCOL_INET_SYBASE_POOLED,      SybaseBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(BuilderUtils.JDBC_SUBPROTOCOL_JTDS_SYBASE,             SybaseBuilder.DATABASENAME);

        jdbcDriverToPlatform.put(AxionBuilder.JDBC_DRIVER,                       AxionBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(Db2Builder.JDBC_DRIVER,                         Db2Builder.DATABASENAME);
        jdbcDriverToPlatform.put(BuilderUtils.JDBC_DRIVER_INET_DB2,              Db2Builder.DATABASENAME);
        jdbcDriverToPlatform.put(DerbyBuilder.JDBC_DRIVER_EMBEDDED,              DerbyBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(DerbyBuilder.JDBC_DRIVER,                       DerbyBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(FirebirdBuilder.JDBC_DRIVER,                    FirebirdBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(HsqlDbBuilder.JDBC_DRIVER,                      HsqlDbBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(InterbaseBuilder.JDBC_DRIVER,                   InterbaseBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(SapDbBuilder.JDBC_DRIVER,                       MaxDbBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(MckoiSqlBuilder.JDBC_DRIVER,                    MckoiSqlBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(MSSqlBuilder.JDBC_DRIVER,                       MSSqlBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(BuilderUtils.JDBC_DRIVER_INET_SQLSERVER,        MSSqlBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(BuilderUtils.JDBC_DRIVER_JSQLCONNECT_SQLSERVER, MSSqlBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(MySqlBuilder.JDBC_DRIVER,                       MySqlBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(MySqlBuilder.JDBC_DRIVER_OLD,                   MySqlBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(OracleBuilder.JDBC_DRIVER,                      OracleBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(BuilderUtils.JDBC_DRIVER_INET_ORACLE,           OracleBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(PostgreSqlBuilder.JDBC_DRIVER,                  PostgreSqlBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(SybaseBuilder.JDBC_DRIVER,                      SybaseBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(SybaseBuilder.JDBC_DRIVER_OLD,                  SybaseBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(BuilderUtils.JDBC_DRIVER_INET_SYBASE,           SybaseBuilder.DATABASENAME);
    }

    /**
     * Tries to determine the database type for the given data source. Note that this will establish
     * a connection to the database.
     * 
     * @param dataSource The data source
     * @return The database type or <code>null</code> if the database type couldn't be determined
     */
    public String determineDatabaseType(DataSource dataSource) throws SQLException
    {
        Connection connection = null;

        try
        {
            connection = dataSource.getConnection();
            
            DatabaseMetaData metaData = connection.getMetaData();

            return determineDatabaseType(metaData.getDriverName(), metaData.getURL());
        }
        finally
        {
            if (connection != null)
            {
                connection.close();
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
