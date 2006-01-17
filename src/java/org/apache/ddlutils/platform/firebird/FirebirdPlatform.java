package org.apache.ddlutils.platform.firebird;

/*
 * Copyright 1999-2006 The Apache Software Foundation.
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
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Types;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.DynaSqlException;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.platform.PlatformImplBase;

/**
 * The platform implementation for the Firebird database.
 * It is assumed that the database is configured with sql dialect 3!
 * 
 * @author Thomas Dudziak
 * @author Martin van den Bemt
 * @version $Revision: 231306 $
 */
public class FirebirdPlatform extends PlatformImplBase
{
    /** The log for this platform. */
    private final Log _log = LogFactory.getLog(getClass());
    /** Database name of this platform. */
    public static final String DATABASENAME     = "Firebird";
    /** The standard Firebird jdbc driver. */
    public static final String JDBC_DRIVER      = "org.firebirdsql.jdbc.FBDriver";
    /** The subprotocol used by the standard Firebird driver. */
    public static final String JDBC_SUBPROTOCOL = "firebirdsql";

    public FirebirdPlatform() {
        PlatformInfo info = new PlatformInfo();

        info.setMaxIdentifierLength(31);
        info.setRequiringNullAsDefaultValue(false);
        info.setPrimaryKeyEmbedded(true);
        info.setForeignKeysEmbedded(false);
        info.setIndicesEmbedded(false);
        info.setCommentPrefix("/*");
        info.setCommentSuffix("*/");
        info.setSupportingDelimitedIdentifiers(false);

        // BINARY and VARBINARY are also handled by the InterbaseBuilder.getSqlType method
        info.addNativeTypeMapping(Types.ARRAY,         "BLOB");
        info.addNativeTypeMapping(Types.BINARY,        "BLOB", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.BIT,           "SMALLINT", Types.BIT);
        info.addNativeTypeMapping(Types.CLOB,          "BLOB SUB_TYPE TEXT", Types.LONGVARCHAR);
        info.addNativeTypeMapping(Types.DISTINCT,      "BLOB", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.BLOB,          "BLOB", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.DOUBLE,        "DOUBLE PRECISION");
        info.addNativeTypeMapping(Types.FLOAT,         "DOUBLE PRECISION");
        info.addNativeTypeMapping(Types.JAVA_OBJECT,   "BLOB", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.LONGVARBINARY, "BLOB", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.LONGVARCHAR,   "BLOB SUB_TYPE TEXT");
        info.addNativeTypeMapping(Types.NULL,          "BLOB");
        info.addNativeTypeMapping(Types.OTHER,         "BLOB");
        info.addNativeTypeMapping(Types.REAL,          "FLOAT");
        info.addNativeTypeMapping(Types.TINYINT,       "SMALLINT");
        info.addNativeTypeMapping(Types.REF,           "BLOB");
        info.addNativeTypeMapping(Types.STRUCT,        "BLOB");
        info.addNativeTypeMapping(Types.VARBINARY,     "BLOB", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.BOOLEAN, "SMALLINT");
        info.addNativeTypeMapping("DATALINK", "BLOB");

        /**
         * This value is set to 128, to give multiple column index a chance
         * to stay below the maximum key size of 256.
         * If you use different encodings, you most likely need to decrease this value.
         */
        info.addDefaultSize(Types.VARCHAR, 128);
        info.addDefaultSize(Types.CHAR, 128);
        info.addDefaultSize(Types.SMALLINT, 5);

        setSqlBuilder(new FirebirdBuilder(info));
        setModelReader(new FirebirdModelReader(info));
    }
    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return DATABASENAME;
    }
    
    /**
     * {@inheritDoc}
     */
    public void createTables(Connection connection, Database model, boolean dropTablesFirst, boolean continueOnError) throws DynaSqlException
    {
        String sql = getCreateTablesSql(model, dropTablesFirst, continueOnError);
        runBatch(connection, sql, continueOnError);
    }

    /**
     * Runbatch is a replacement for evaluateBatch.
     * Especially the <code>SET TERM !!;</code> blocks need to be executed in one go.
     *
     * @param connection the connection to use
     * @param sql the sql to process
     * @param continueOnError needs to continue when an error occurs.
     */
    public int runBatch(Connection connection, String sql, boolean continueOnError) throws DynaSqlException
    {
        int errors       = 0;
        Statement statement    = null;
        int commandCount = 0;

        try {
            statement = connection.createStatement();
            StringTokenizer tokenizer = new StringTokenizer(sql, ";");
    
            while (tokenizer.hasMoreTokens())
            {
                String command = tokenizer.nextToken().trim();
                if (command.equals("--TERM--"))
                {
                    command = "";
                    while(tokenizer.hasMoreTokens())
                    {
                        String termSql = tokenizer.nextToken().trim();  
                        if(termSql.equals("--TERM--"))
                        {
                            break;
                        }
                        if (termSql.length() > 0)
                        {
                            command+=termSql+";";
                        }
                        
                    }
                }
                if (command.length() == 0)
                {
                    continue;
                }
                
                System.err.println("SQL Command :\n" + command);
                commandCount++;
                
                if (_log.isDebugEnabled())
                {
                    _log.debug("About to execute SQL " + command);
                }
                try
                {
                    int results = statement.executeUpdate(command);

                    if (_log.isDebugEnabled())
                    {
                        _log.debug("After execution, " + results + " row(s) have been changed");
                    }
                }
                catch (SQLException ex)
                {
                    if (continueOnError)
                    {
                        System.err.println("SQL Command " + command + " failed with " + ex.getMessage());
                        errors++;
                    }
                    else
                    {
                        throw new DynaSqlException("Error while executing SQL "+command, ex);
                    }
                }

                // lets display any warnings
                SQLWarning warning = connection.getWarnings();

                while (warning != null)
                {
                    _log.warn(warning.toString());
                    warning = warning.getNextWarning();
                }
                connection.clearWarnings();
            }
            _log.info("Executed "+ commandCount + " SQL command(s) with " + errors + " error(s)");
        }
        catch (SQLException ex)
        {
            throw new DynaSqlException("Error while executing SQL", ex);
        }
        finally
        {
            closeStatement(statement);
        }

        return errors;
    }

}
