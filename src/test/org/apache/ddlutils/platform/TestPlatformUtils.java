package org.apache.ddlutils.platform;

import org.apache.ddlutils.PlatformUtils;

import junit.framework.TestCase;

/**
 * Tests the {@link org.apache.ddlutils.PlatformUtils#determineDatabaseType(String, String)} method.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 279421 $
 */
public class TestPlatformUtils extends TestCase
{
    /** The tested platform utils object */
    private PlatformUtils _platformUtils;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        _platformUtils = new PlatformUtils();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        _platformUtils = null;
    }

    // TODO: test urls for each database

    public void testAxionDriver()
    {
        assertEquals(AxionPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType("org.axiondb.jdbc.AxionDriver", null));
    }

    public void testAxionUrl()
    {
        assertEquals(AxionPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:axiondb:testdb"));
        assertEquals(AxionPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:axiondb:testdb:/tmp/testdbdir"));
    }

    public void testDb2Driver()
    {
        assertEquals(Db2Platform.DATABASENAME,
                     _platformUtils.determineDatabaseType("com.ibm.db2.jcc.DB2Driver", null));
        assertEquals(Db2Platform.DATABASENAME,
                     _platformUtils.determineDatabaseType("COM.ibm.db2os390.sqlj.jdbc.DB2SQLJDriver", null));
        assertEquals(Db2Platform.DATABASENAME,
                     _platformUtils.determineDatabaseType("COM.ibm.db2.jdbc.app.DB2Driver", null));
        // DataDirect Connect
        assertEquals(Db2Platform.DATABASENAME,
                     _platformUtils.determineDatabaseType("com.ddtek.jdbc.db2.DB2Driver", null));
        // i-net
        assertEquals(Db2Platform.DATABASENAME,
                     _platformUtils.determineDatabaseType("com.inet.drda.DRDADriver", null));
    }

    public void testDb2Url()
    {
        assertEquals(Db2Platform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:db2://sysmvs1.stl.ibm.com:5021/san_jose"));
        assertEquals(Db2Platform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:db2os390://sysmvs1.stl.ibm.com:5021/san_jose"));
        assertEquals(Db2Platform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:db2os390sqlj://sysmvs1.stl.ibm.com:5021/san_jose"));
        // DataDirect Connect
        assertEquals(Db2Platform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:datadirect:db2://server1:50000;DatabaseName=jdbc;User=test;Password=secret"));
        // i-net
        assertEquals(Db2Platform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:inetdb2://server1:50000"));
    }

    public void testCloudscapeUrl()
    {
        assertEquals(CloudscapePlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:db2j:net:database"));
        assertEquals(CloudscapePlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:cloudscape:net:database"));
    }

    public void testDerbyDriver()
    {
        assertEquals(DerbyPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType("org.apache.derby.jdbc.ClientDriver", null));
        assertEquals(DerbyPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType("org.apache.derby.jdbc.EmbeddedDriver", null));
    }

    public void testDerbyUrl()
    {
        assertEquals(DerbyPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:derby:sample"));
    }

    public void testFirebirdDriver()
    {
        assertEquals(FirebirdPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType("org.firebirdsql.jdbc.FBDriver", null));
    }

    public void testFirebirdUrl()
    {
        assertEquals(FirebirdPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:firebirdsql://localhost:8080/path/to/db.fdb"));
        assertEquals(FirebirdPlatform.DATABASENAME,
                    _platformUtils.determineDatabaseType(null, "jdbc:firebirdsql:native:localhost/8080:/path/to/db.fdb"));
        assertEquals(FirebirdPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:firebirdsql:local://localhost:8080:/path/to/db.fdb"));
        assertEquals(FirebirdPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:firebirdsql:embedded:localhost/8080:/path/to/db.fdb"));
    }

    public void testHsqldbDriver()
    {
        assertEquals(HsqlDbPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType("org.hsqldb.jdbcDriver", null));
    }

    public void testHsqldbUrl()
    {
        assertEquals(HsqlDbPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:hsqldb:/opt/db/testdb"));
    }

    public void testInterbaseDriver()
    {
        assertEquals(InterbasePlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType("interbase.interclient.Driver", null));
    }

    public void testInterbaseUrl()
    {
        assertEquals(InterbasePlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:interbase://localhost/e:/testbed/database/employee.gdb"));
    }

    public void testMckoiDriver()
    {
        assertEquals(MckoiPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType("com.mckoi.JDBCDriver", null));
    }

    public void testMckoiUrl()
    {
        assertEquals(MckoiPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:mckoi:local://./db.conf"));
        assertEquals(MckoiPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:mckoi://db.myhost.org/"));
    }

    public void testMsSqlDriver()
    {
        assertEquals(MSSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType("com.microsoft.jdbc.sqlserver.SQLServerDriver", null));
        // DataDirect Connect
        assertEquals(MSSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType("com.ddtek.jdbc.sqlserver.SQLServerDriver", null));
        // JNetDirect JSQLConnect
        assertEquals(MSSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType("com.jnetdirect.jsql.JSQLDriver", null));
        // i-net
        assertEquals(MSSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType("com.inet.tds.TdsDriver", null));
    }

    public void testMsSqlUrl()
    {
        assertEquals(MSSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:microsoft:sqlserver://localhost:1433"));
        // DataDirect Connect
        assertEquals(MSSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:datadirect:sqlserver://server1:1433;User=test;Password=secret"));
        // JNetDirect JSQLConnect
        assertEquals(MSSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:JSQLConnect://localhost/database=master/user=sa/sqlVersion=6"));
        // i-net
        assertEquals(MSSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:inetdae:210.1.164.19:1433"));
        assertEquals(MSSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:inetdae6:[2002:d201:a413::d201:a413]:1433"));
        assertEquals(MSSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:inetdae7:localHost:1433"));
        assertEquals(MSSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:inetdae7a://MyServer/pipe/sql/query"));
        assertEquals(MSSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:inetpool:inetdae:210.1.164.19:1433"));
        assertEquals(MSSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:inetpool:inetdae6:[2002:d201:a413::d201:a413]:1433"));
        assertEquals(MSSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:inetpool:inetdae7:localHost:1433"));
        assertEquals(MSSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:inetpool:inetdae7a://MyServer/pipe/sql/query"));
        assertEquals(MSSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:inetpool:jdbc:inetdae:210.1.164.19:1433"));
        assertEquals(MSSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:inetpool:jdbc:inetdae6:[2002:d201:a413::d201:a413]:1433"));
        assertEquals(MSSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:inetpool:jdbc:inetdae7:localHost:1433"));
        assertEquals(MSSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:inetpool:jdbc:inetdae7a://MyServer/pipe/sql/query"));
        // jTDS
        assertEquals(MSSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:jtds:sqlserver://localhost:8080/test"));
    }

    public void testMySqlDriver()
    {
        assertEquals(MySqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType("com.mysql.jdbc.Driver", null));
        assertEquals(MySqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType("org.gjt.mm.mysql.Driver", null));
    }

    public void testMySqlUrl()
    {
        assertEquals(MySqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:mysql://localhost:1234/test"));
    }

    public void testOracleDriver()
    {
        assertEquals(Oracle8Platform.DATABASENAME,
                     _platformUtils.determineDatabaseType("oracle.jdbc.driver.OracleDriver", null));
        assertEquals(Oracle8Platform.DATABASENAME,
                     _platformUtils.determineDatabaseType("oracle.jdbc.dnlddriver.OracleDriver", null));
        // DataDirect Connect
        assertEquals(Oracle8Platform.DATABASENAME,
                     _platformUtils.determineDatabaseType("com.ddtek.jdbc.oracle.OracleDriver", null));
        // i-net
        assertEquals(Oracle8Platform.DATABASENAME,
                     _platformUtils.determineDatabaseType("com.inet.ora.OraDriver", null));
    }

    public void testOracleUrl()
    {
        assertEquals(Oracle8Platform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:oracle:thin:@myhost:1521:orcl"));
        assertEquals(Oracle8Platform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:oracle:oci8:@(description=(address=(host=myhost)(protocol=tcp)(port=1521))(connect_data=(sid=orcl)))"));
        assertEquals(Oracle8Platform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:oracle:dnldthin:@myhost:1521:orcl"));
        assertEquals(Oracle8Platform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:oracle:dnldthin:@myhost:1521:orcl"));
        // DataDirect Connect
        assertEquals(Oracle8Platform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:datadirect:oracle://server3:1521;ServiceName=ORCL;User=test;Password=secret"));
        // i-net
        assertEquals(Oracle8Platform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:inetora:www.inetsoftware.de:1521:orcl?traceLevel=2"));
    }

    public void testPostgreSqlDriver()
    {
        assertEquals(PostgreSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType("org.postgresql.Driver", null));
    }

    public void testPostgreSqlUrl()
    {
        assertEquals(PostgreSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:postgresql://localhost:1234/test"));
        assertEquals(PostgreSqlPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:postgresql://[::1]:5740/accounting"));
    }

    public void testMaxDbDriver()
    {
        assertEquals(MaxDbPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType("com.sap.dbtech.jdbc.DriverSapDB", null));
    }

    public void testMaxDbUrl()
    {
        assertEquals(MaxDbPlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:sapdb://servermachine:9876/TST"));
    }

    public void testSybaseDriver()
    {
        assertEquals(SybasePlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType("com.sybase.jdbc.SybDriver", null));
        assertEquals(SybasePlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType("com.sybase.jdbc2.jdbc.SybDriver", null));
        // DataDirect Connect
        assertEquals(SybasePlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType("com.ddtek.jdbc.sybase.SybaseDriver", null));
        // i-net
        assertEquals(SybasePlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType("com.inet.syb.SybDriver", null));
    }

    public void testSybaseUrl()
    {
        assertEquals(SybasePlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:sybase:Tds:xyz:3767orjdbc:sybase:Tds:130.214.90.27:3767"));
        // DataDirect Connect
        assertEquals(SybasePlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:datadirect:sybase://server2:5000;User=test;Password=secret"));
        // i-net
        assertEquals(SybasePlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:inetsyb:www.inetsoftware.de:3333"));
        assertEquals(SybasePlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:inetpool:inetsyb:www.inetsoftware.de:3333"));
        assertEquals(SybasePlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:inetpool:jdbc:inetsyb:www.inetsoftware.de:3333"));
        // jTDS
        assertEquals(SybasePlatform.DATABASENAME,
                     _platformUtils.determineDatabaseType(null, "jdbc:jtds:sybase://localhost:8080/test"));
    }
}
