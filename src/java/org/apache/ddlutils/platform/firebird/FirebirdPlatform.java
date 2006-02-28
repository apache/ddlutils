package org.apache.ddlutils.platform.firebird;

/*
 * Copyright 2005-2006 The Apache Software Foundation.
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
    /** Database name of this platform. */
    public static final String DATABASENAME     = "Firebird";
    /** The standard Firebird jdbc driver. */
    public static final String JDBC_DRIVER      = "org.firebirdsql.jdbc.FBDriver";
    /** The subprotocol used by the standard Firebird driver. */
    public static final String JDBC_SUBPROTOCOL = "firebirdsql";
    /** The log for this platform. */
    private final Log _log = LogFactory.getLog(getClass());

    /**
     * Creates a new Firebird platform instance.
     */
    public FirebirdPlatform()
    {
        PlatformInfo info = new PlatformInfo();

        info.setMaxIdentifierLength(31);
        info.setRequiringNullAsDefaultValue(false);
        info.setPrimaryKeyEmbedded(true);
        info.setForeignKeysEmbedded(false);
        info.setIndicesEmbedded(false);
        info.setCommentPrefix("/*");
        info.setCommentSuffix("*/");
        //info.setSupportingDelimitedIdentifiers(false);

        info.addNativeTypeMapping(Types.ARRAY,         "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.BINARY,        "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.BIT,           "SMALLINT",           Types.SMALLINT);
        info.addNativeTypeMapping(Types.CLOB,          "BLOB SUB_TYPE TEXT", Types.LONGVARCHAR);
        info.addNativeTypeMapping(Types.DISTINCT,      "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.BLOB,          "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.DOUBLE,        "DOUBLE PRECISION");
        info.addNativeTypeMapping(Types.FLOAT,         "DOUBLE PRECISION",   Types.DOUBLE);
        info.addNativeTypeMapping(Types.JAVA_OBJECT,   "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.LONGVARBINARY, "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.LONGVARCHAR,   "BLOB SUB_TYPE TEXT");
        info.addNativeTypeMapping(Types.NULL,          "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.OTHER,         "BLOB",               Types.LONGVARBINARY);
        // This is back-mapped to REAL in the model reader
        info.addNativeTypeMapping(Types.REAL,          "FLOAT");
        info.addNativeTypeMapping(Types.TINYINT,       "SMALLINT",           Types.SMALLINT);
        info.addNativeTypeMapping(Types.REF,           "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.STRUCT,        "BLOB",               Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.VARBINARY,     "BLOB",               Types.LONGVARBINARY);
        
        info.addNativeTypeMapping("BOOLEAN",  "SMALLINT", "SMALLINT");
        info.addNativeTypeMapping("DATALINK", "BLOB",     "LONGVARBINARY");

        info.addDefaultSize(Types.VARCHAR, 254);
        info.addDefaultSize(Types.CHAR,    254);

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
     * Firebird-specific replacement for
     * {@link org.apache.ddlutils.platform.PlatformImplBase#evaluateBatch(Connection, String, boolean)}
     * that executes blocks delimited by the term {@link FirebirdBuilder#TERM_COMMAND}
     * (defined in the database via <code>SET TERM !!;</code>), in one go.
     *
     * @param connection      The connection to use
     * @param sql             The sql to process
     * @param continueOnError Whether to continue when an error occurs
     * @return The number of errors
     */
    public int evaluateBatch(Connection connection, String sql, boolean continueOnError) throws DynaSqlException
    {
        Statement statement    = null;
        int       commandCount = 0;
        int       errors       = 0;

        try
        {
            statement = connection.createStatement();

            StringTokenizer tokenizer = new StringTokenizer(sql, ";");
            StringBuffer    command   = new StringBuffer();
    
            while (tokenizer.hasMoreTokens())
            {
                String token = tokenizer.nextToken().trim();

                command.setLength(0);

                if (token.equals(FirebirdBuilder.TERM_COMMAND))
                {
                    while (tokenizer.hasMoreTokens())
                    {
                        token = tokenizer.nextToken().trim();  

                        if (token.length() > 0)
                        {
                            if (token.equals(FirebirdBuilder.TERM_COMMAND))
                            {
                                break;
                            }
                            else
                            {
                                command.append(token);
                                command.append(";");
                            }
                        }
                        
                    }
                }
                else
                {
                    command.append(token);
                }
                if (command.length() == 0)
                {
                    continue;
                }
                
                commandCount++;
                
                if (_log.isDebugEnabled())
                {
                    _log.debug("About to execute SQL " + command.toString());
                }
                try
                {
                    int results = statement.executeUpdate(command.toString());

                    if (_log.isDebugEnabled())
                    {
                        _log.debug("After execution, " + results + " row(s) have been changed");
                    }
                }
                catch (SQLException ex)
                {
                    if (continueOnError)
                    {
                        _log.error("SQL Command " + command.toString() + " failed", ex);
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
            if (_log.isInfoEnabled())
            {
                _log.info("Executed "+ commandCount + " SQL command(s) with " + errors + " error(s)");
            }
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
