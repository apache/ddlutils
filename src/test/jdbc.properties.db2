# JDBC properties for Db2 UDB

# Use this property if ddlutils does not recognize the platform from the settings
#ddlutils.platform=Db2

# Properties starting with "datasource." will be fed into the datasource instance of the
# class configured via the datasource.class property

datasource.class=org.apache.commons.dbcp.BasicDataSource

datasource.driverClassName=COM.ibm.db2.jdbc.net.DB2Driver
datasource.url=jdbc:db2://localhost:6789/ddlutils
datasource.username=ddlutils
datasource.password=ddlutils
