package org.apache.ddlutils.util;

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

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.builder.SqlBuilder;
import org.apache.ddlutils.model.Database;

/**
 * DDLExecutor is a utility class which is capable of performing DDL
 * on a database connection such as to create a database, or drop it or alter it..
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision$
 */
public class DDLExecutor extends JdbcSupport {

    /** The Log to which logging calls will be made. */
    private final Log log = LogFactory.getLog( DDLExecutor.class );
    
    /** The Strategy for building SQL for the current physical database */
    private SqlBuilder sqlBuilder;
    
    /** Should we continue if a command fails */
    private boolean continueOnError = true;
    
    public DDLExecutor() {
    }

    public DDLExecutor(DataSource dataSource) {
        super(dataSource);
    }

    public DDLExecutor(DataSource dataSource, SqlBuilder sqlBuilder) {
        super(dataSource);
        this.sqlBuilder = sqlBuilder;
    }


    /**
     * Creates the given database using the current DataSource, optionally 
     * dropping tables first.
     */
    public void createDatabase(Database database, boolean dropTablesFirst) throws SQLException {
        String sql = null;
        try {
            StringWriter buffer = new StringWriter();
            getSqlBuilder().setWriter(buffer);
            getSqlBuilder().createDatabase(database, dropTablesFirst);
            sql = buffer.toString();
        }
        catch (IOException e) {
            throw new SQLException( "We should never get this exception!!: " + e );
        }
        evaluateBatch(sql);
    }
    
    /**
     * Drops the given database using the current DataSource
     */
    public void dropDatabase(Database database) throws SQLException {
        String sql = null;
        try {
            StringWriter buffer = new StringWriter();
            getSqlBuilder().setWriter(buffer);
            getSqlBuilder().dropDatabase(database);
            sql = buffer.toString();
        }
        catch (IOException e) {
            throw new SQLException( "We should never get this exception!!: " + e );
        }
        evaluateBatch(sql);
    }
    

    // Properties
    //-------------------------------------------------------------------------                

    /**
     * Returns the sqlBuilder.
     * @return SqlBuilder
     */
    public SqlBuilder getSqlBuilder() {
        return sqlBuilder;
    }

    /**
     * Sets the sqlBuilder.
     * @param sqlBuilder The sqlBuilder to set
     */
    public void setSqlBuilder(SqlBuilder sqlBuilder) {
        this.sqlBuilder = sqlBuilder;
    }

    /**
     * Returns the continueOnError.
     * @return boolean
     */
    public boolean isContinueOnError() {
        return continueOnError;
    }

    /**
     * Sets the continueOnError.
     * @param continueOnError The continueOnError to set
     */
    public void setContinueOnError(boolean continueOnError) {
        this.continueOnError = continueOnError;
    }

    // Implementation methods
    //-------------------------------------------------------------------------                

    /**
     * Executes a series of sql statements.  It is assumed that the parameter
     * string contains sql statements separated by a semicolon.
     *
     * @todo consider outputting a collection of String or some kind of statement
     * object from the SqlBuilder instead of having to parse strings here
     *
     * @param sql A list of sql statements
     *
     * @throws SQLException if an error occurs and isContinueOnError == false
     */
    public int evaluateBatch(String sql) throws SQLException {
        Connection connection = borrowConnection();
        Statement statement = null;
        int errors = 0;
        int commandCount = 0;
        try {
            statement = connection.createStatement();
            
            StringTokenizer tokenizer = new StringTokenizer( sql, ";" );
            while (tokenizer.hasMoreTokens()) {
                String command = tokenizer.nextToken();
                
                // ignore whitespace
                command = command.trim();
                if ( command.length() == 0 ) {
                    continue;
                }
                
                commandCount++;
                
                if (log.isDebugEnabled() ) {
                    log.debug( "About to execute sql: " + command );
                }
                
                if (continueOnError) {                                
                    try {
                        int results = statement.executeUpdate(command);
                        if (log.isDebugEnabled()) {
                            log.debug( "returned: " + results + " row(s) changed" );
                        }
                    }
                    catch (SQLException e) {
                        log.error("Command " + command + " failed", e);
                        System.err.println("Command " + command + " failed with " + e.getMessage());
                        errors++;
                    }
                }
                else {
                    int results = statement.executeUpdate(command);
                    if (log.isDebugEnabled()) {
                        log.debug( "returned: " + results + " row(s) changed" );
                    }
                }
                
                // lets display any warnings
                SQLWarning warning = connection.getWarnings();
                while (warning != null ) {
                    log.warn( warning.toString() );
                    warning = warning.getNextWarning();
                }
                connection.clearWarnings();
            }
            log.info( "Executed: "+ commandCount + " statement(s) with " + errors + " error(s)" );
        }
        finally {
            closeStatementAndConnection(statement);
            returnConnection(connection);
        }

        return errors;
    }
    

}
