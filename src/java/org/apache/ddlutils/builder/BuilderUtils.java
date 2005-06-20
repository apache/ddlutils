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
   
    /** The standard DB2 jdbc driver */
    public static final String DRIVER_DB2                     = "COM.ibm.db2.jdbc.app.DB2Driver";
    /** The i-net DB2 jdbc driver */
    public static final String DRIVER_DB2_INET                = "com.inet.drda.DRDADriver";
    /** The standard Firebird jdbc driver */
    public static final String DRIVER_FIREBIRD                = "org.firebirdsql.jdbc.FBDriver";
    /** The standard Hsqldb jdbc driver */
    public static final String DRIVER_HSQLDB                  = "org.hsqldb.jdbcDriver";
    /** The i-net pooled jdbc driver for SQLServer and Sybase */
    public static final String DRIVER_INET_POOLED             = "com.inet.pool.PoolDriver";
    /** The standard Informix jdbc driver */
    public static final String DRIVER_INFORMIX                = "com.informix.jdbc.IfxDriver";
    /** The jTDS jdbc driver for SQLServer and Sybase */
    public static final String DRIVER_JTDS                    = "net.sourceforge.jtds.jdbc.Driver";
    /** The standard MaxDB jdbc driver */
    public static final String DRIVER_MAXDB                   = "com.sap.dbtech.jdbc.DriverSapDB";
    /** The standard McKoi jdbc driver */
    public static final String DRIVER_MCKOI                   = "com.mckoi.JDBCDriver";
    /** The standard SQLServer jdbc driver */
    public static final String DRIVER_MSSQLSERVER             = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
    /** The i-net SQLServer jdbc driver */
    public static final String DRIVER_MSSQLSERVER_INET        = "com.inet.tds.TdsDriver";
    /** The JNetDirect SQLServer jdbc driver */
    public static final String DRIVER_MSSQLSERVER_JSQLCONNECT = "com.jnetdirect.jsql.JSQLDriver";
    /** The standard MySQL jdbc driver */
    public static final String DRIVER_MYSQL                   = "com.mysql.jdbc.Driver";
    /** The old MySQL jdbc driver */
    public static final String DRIVER_MYSQL_OLD               = "org.gjt.mm.mysql.Driver";
    /** The standard Oracle jdbc driver */
    public static final String DRIVER_ORACLE                  = "oracle.jdbc.driver.OracleDriver";
    /** The i-net Oracle jdbc driver */
    public static final String DRIVER_ORACLE_INET             = "com.inet.ora.OraDriver";
    /** The standard PostgreSQL jdbc driver */
    public static final String DRIVER_POSTGRESQL              = "org.postgresql.Driver";
    /** The standard Sapdb jdbc driver */
    public static final String DRIVER_SAPDB                   = DRIVER_MAXDB;
    /** The standard Sybase jdbc driver */
    public static final String DRIVER_SYBASE                  = "com.sybase.jdbc2.jdbc.SybDriver";
    /** The old Sybase jdbc driver */
    public static final String DRIVER_SYBASE_OLD              = "com.sybase.jdbc.SybDriver";
    /** The i-net Sybase jdbc driver */
    public static final String DRIVER_SYBASE_INET             = "com.inet.syb.SybDriver";
    
    /** The subprotocol used by the standard DB2 driver */
    public static final String SUBPROTOCOL_DB2                       = "db2";
    /** The subprotocol used by the i-net DB2 driver */
    public static final String SUBPROTOCOL_DB2_INET                  = "inetdb2";
    /** The subprotocol used by the standard Firebird driver */
    public static final String SUBPROTOCOL_FIREBIRD                  = "firebirdsql";
    /** The subprotocol used by the standard Hsqldb driver */
    public static final String SUBPROTOCOL_HSQLDB                    = "hsqldb";
    /** The subprotocol used by the standard Informix driver */
    public static final String SUBPROTOCOL_INFORMIX                  = "informix-sqli";
    /** The subprotocol used by the standard MaxDB driver */
    public static final String SUBPROTOCOL_MAXDB                     = "sapdb";
    /** The subprotocol used by the standard McKoi driver */
    public static final String SUBPROTOCOL_MCKOI                     = "mckoi";
    /** The subprotocol used by the standard SQLServer driver */
    public static final String SUBPROTOCOL_MSSQLSERVER               = "microsoft:sqlserver";
    /** A subprotocol used by the i-net SQLServer driver */
    public static final String SUBPROTOCOL_MSSQLSERVER_INET          = "inetdae";
    /** A subprotocol used by the i-net SQLServer driver */
    public static final String SUBPROTOCOL_MSSQLSERVER6_INET         = "inetdae6";
    /** A subprotocol used by the i-net SQLServer driver */
    public static final String SUBPROTOCOL_MSSQLSERVER7_INET         = "inetdae7";
    /** A subprotocol used by the i-net SQLServer driver */
    public static final String SUBPROTOCOL_MSSQLSERVER7A_INET        = "inetdae7a";
    /** A subprotocol used by the pooled i-net SQLServer driver */
    public static final String SUBPROTOCOL_MSSQLSERVER_INET_POOLED   = "inetpool:inetdae";
    /** A subprotocol used by the pooled i-net SQLServer driver */
    public static final String SUBPROTOCOL_MSSQLSERVER6_INET_POOLED  = "inetpool:inetdae6";
    /** A subprotocol used by the pooled i-net SQLServer driver */
    public static final String SUBPROTOCOL_MSSQLSERVER7_INET_POOLED  = "inetpool:inetdae7";
    /** A subprotocol used by the pooled i-net SQLServer driver */
    public static final String SUBPROTOCOL_MSSQLSERVER7A_INET_POOLED = "inetpool:inetdae7a";
    /** The subprotocol used by the JNetDirect SQLServer driver */
    public static final String SUBPROTOCOL_MSSQLSERVER_JSQLCONNECT   = "JSQLConnect";
    /** The subprotocol used by the jTDS SQLServer driver */
    public static final String SUBPROTOCOL_MSSQLSERVER_JTDS          = "jtds:sqlserver";
    /** The subprotocol used by the standard MySQL driver */
    public static final String SUBPROTOCOL_MYSQL                     = "mysql";
    /** The subprotocol used by the standard Oracle driver */
    public static final String SUBPROTOCOL_ORACLE                    = "oracle";
    /** The subprotocol used by the i-net Oracle driver */
    public static final String SUBPROTOCOL_ORACLE_INET               = "inetora";
    /** The subprotocol used by the standard PostgreSQL driver */
    public static final String SUBPROTOCOL_POSTGRESQL                = "postgresql";
    /** The subprotocol used by the standard Sapdb driver */
    public static final String SUBPROTOCOL_SAPDB                     = SUBPROTOCOL_MAXDB;
    /** The subprotocol used by the standard Sybase driver */
    public static final String SUBPROTOCOL_SYBASE                    = "sybase:Tds";
    /** The subprotocol used by the i-net Sybase driver */
    public static final String SUBPROTOCOL_SYBASE_INET               = "inetsyb";
    /** The subprotocol used by the pooled i-net Sybase driver */
    public static final String SUBPROTOCOL_SYBASE_INET_POOLED        = "inetpool:inetsyb";
    /** The subprotocol used by the jTDS Sybase driver */
    public static final String SUBPROTOCOL_SYBASE_JTDS               = "jtds:sybase";
    
    
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
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_DB2,                       Db2Builder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_DB2_INET,                  Db2Builder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_FIREBIRD,                  FirebirdBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_HSQLDB,                    HsqlDbBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_INFORMIX,                  FirebirdBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_MAXDB,                     MaxDbBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_MCKOI,                     MckoiSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_MSSQLSERVER,               MSSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_MSSQLSERVER_INET,          MSSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_MSSQLSERVER6_INET,         MSSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_MSSQLSERVER7_INET,         MSSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_MSSQLSERVER7A_INET,        MSSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_MSSQLSERVER_INET_POOLED,   MSSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_MSSQLSERVER6_INET_POOLED,  MSSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_MSSQLSERVER7_INET_POOLED,  MSSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_MSSQLSERVER7A_INET_POOLED, MSSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_MSSQLSERVER_JTDS,          MSSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_MYSQL,                     MySqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_ORACLE,                    OracleBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_ORACLE_INET,               OracleBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_POSTGRESQL,                PostgreSqlBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_SYBASE,                    SybaseBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_SYBASE_INET,               SybaseBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_SYBASE_INET_POOLED,        SybaseBuilder.DATABASENAME);
        jdbcSubProtocolToPlatform.put(SUBPROTOCOL_SYBASE_JTDS,               SybaseBuilder.DATABASENAME);

        jdbcDriverToPlatform.put(DRIVER_DB2,                     Db2Builder.DATABASENAME);
        jdbcDriverToPlatform.put(DRIVER_DB2_INET,                Db2Builder.DATABASENAME);
        jdbcDriverToPlatform.put(DRIVER_FIREBIRD,                FirebirdBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(DRIVER_HSQLDB,                  HsqlDbBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(DRIVER_INFORMIX,                FirebirdBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(DRIVER_MAXDB,                   MaxDbBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(DRIVER_MCKOI,                   MckoiSqlBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(DRIVER_MSSQLSERVER,             MSSqlBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(DRIVER_MSSQLSERVER_INET,        MSSqlBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(DRIVER_MSSQLSERVER_JSQLCONNECT, MSSqlBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(DRIVER_MYSQL,                   MySqlBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(DRIVER_MYSQL_OLD,               MySqlBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(DRIVER_ORACLE,                  OracleBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(DRIVER_ORACLE_INET,             OracleBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(DRIVER_POSTGRESQL,              PostgreSqlBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(DRIVER_SYBASE,                  SybaseBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(DRIVER_SYBASE_OLD,              SybaseBuilder.DATABASENAME);
        jdbcDriverToPlatform.put(DRIVER_SYBASE_INET,             SybaseBuilder.DATABASENAME);
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
