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
package org.apache.commons.sql.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
     * @return a List of currently registered database types for which there is a
     * specific SqlBuilder.
     */
    public static synchronized List getDatabaseTypes() {
        // return a copy to prevent modification
        List answer = new ArrayList();
        answer.addAll( databases.keySet());
        return answer;
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
        registerDatabase("axion", AxionBuilder.class);
        registerDatabase("db2", Db2Builder.class);
        registerDatabase("hsqldb", HsqlDbBuilder.class);
        registerDatabase("mckoi", MckoiSqlBuilder.class);
        registerDatabase("mssql", MSSqlBuilder.class);
        registerDatabase("mysql", MySqlBuilder.class);
        registerDatabase("oracle", OracleBuilder.class);
        registerDatabase("postgresql", PostgreSqlBuilder.class);
        registerDatabase("sybase", SybaseBuilder.class);
    }
}
