/*
 * $Header: /home/cvs/jakarta-commons-sandbox/jelly/src/java/org/apache/commons/jelly/CompilableTag.java,v 1.5 2002/05/17 15:18:12 jstrachan Exp $
 * $Revision: 1.5 $
 * $Date: 2002/05/17 15:18:12 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 * 
 * $Id: CompilableTag.java,v 1.5 2002/05/17 15:18:12 jstrachan Exp $
 */
package org.apache.commons.sql.util;

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
import org.apache.commons.sql.builder.SqlBuilder;
import org.apache.commons.sql.model.Database;

/**
 * DDLExecutor is a utility class which is capable of performing DDL
 * on a database connection such as to create a database, or drop it or alter it..
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.14 $
 */
public class DDLExecutor extends JdbcSupport {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog( DDLExecutor.class );
    
    /** The Strategy for building SQL for the current physical database */
    private SqlBuilder sqlBuilder;
    
    /** Should we continue if a command fails */
    private boolean continueOnError = true;
    
    public DDLExecutor() {
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

    protected void evaluateBatch(String sql) throws SQLException {
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
                        log.error( "Command failed: " + command + ". Reason: " + e );
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
            closeStatement(statement);
            returnConnection(connection);
        }
    }
    

}
