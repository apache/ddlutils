package org.apache.commons.sql.builder;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.Database;
import org.apache.commons.sql.model.ForeignKey;
import org.apache.commons.sql.model.Reference;
import org.apache.commons.sql.model.Table;

/**
 * A factory of SqlBuilder instances based on a case insensitive database name.
 * 
 * Ultimately this class could use a discovery mechanism (such as commons-discovery) to find
 * new databases on the classpath.
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.14 $
 */
public class SqlBuilderFactory {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(SqlBuilderFactory.class);

    private static Map databases = new HashMap();
        
    static {
        registerDatabases();
    }

    /**
     * Creates a new SqlBuilder for the given (case insensitive) database name
     * or returns null if the database is not recognized.
     */
    public static synchronized SqlBuilder newSqlBuilder(String databaseName) 
        throws IllegalAccessException, InstantiationException {
            
        Class theClass = (Class) databases.get(databaseName.toLowerCase());
        if (theClass != null) {
            return (SqlBuilder) theClass.newInstance();
        }
        return null;
    }


    /**
     * Register the common builders
     */
    public static synchronized void registerDatabase(String databaseName, Class sqlBuilderClass) {
        databases.put(databaseName.toLowerCase(), sqlBuilderClass);        
    }

    /**
     * Register the common builders
     */
    protected static void registerDatabases() {
        registerDatabase("mssql", MSSqlBuilder.class);
        registerDatabase("mysql", MySqlBuilder.class);
        registerDatabase("oracle", OracleBuilder.class);
        registerDatabase("sybase", SybaseBuilder.class);
    }
}
