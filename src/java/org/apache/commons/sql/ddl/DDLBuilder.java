/*
 * /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons-sandbox//sql/src/java/org/apache/commons/sql/ddl/DDLBuilder.java,v 1.3 2004/01/06 19:27:18 matth Exp
 * 1.3
 * 2004/01/06 19:27:18
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
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
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
 */
package org.apache.commons.sql.ddl;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.sql.SQLException;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.sql.model.Database;
import org.apache.commons.sql.type.Types;
import org.apache.commons.sql.type.TypesReader;


/**
 * Generates the DDL required to create and drop databases and tables.
 *
 * @version DDLBuilder.java,v 1.3 2004/01/06 19:27:18 matth Exp
 */
public class DDLBuilder {

    /** 
     * The current Writer used to output the SQL to 
     */
    private Writer writer;

    /**
     * The provider configuration
     */
    private ProviderVersion provider;

    /**
     * The URL of the core taglib
     */
    private URL taglibPath;

    /**
     * The path to locate provider resources (types, jelly scripts)
     */
    private URL basePath;

    /**
     * The types supported by the provider
     */
    private Types types;

    /**
     * The logger
     */
    private static final Log log = LogFactory.getLog(DDLBuilder.class);

    /**
     * JellyContext variable name for the Database instance
     */
    private static final String DATABASE_VAR = "database";

    /**
     * JellyContext variable name for the core taglib path
     */
    private static final String TAGLIB_VAR = "commons_sql.taglib";

    /**
     * JellyContext variable name for the Types instance
     */
    private static final String TYPES_VAR = "types";


    /**
     * Construct a new <code>DDLBuilder</code>
     *
     * @param provider the provider configuration
     * @param taglibPath the path of the core tag library
     * @param basePath the path to locate provider resources
     */
    public DDLBuilder(ProviderVersion provider, URL taglibPath, URL basePath) 
        throws IOException {

        if (provider == null) {
            throw new IllegalArgumentException("Argument 'provider' is null");
        }
        if (taglibPath == null) {
            throw new IllegalArgumentException(
                "Argument 'taglibPath' is null");
        }
        if (basePath == null) {
            throw new IllegalArgumentException("Argument 'basePath' is null");
        }
        this.provider = provider;
        this.taglibPath = taglibPath;
        this.basePath = basePath;

        URL path = new URL(getPath(provider.getTypes()));
        types = TypesReader.read(path);
    }

    /**
     * @return the Writer used to print the DDL to
     */
    public Writer getWriter() {
        return writer;
    }

    /**
     * Sets the writer used to print the DDL to
     */
    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    /**
     * Outputs the DDL required to drop and recreate the database 
     */
    public void createDatabase(Database database) 
        throws IOException, SQLException {
        createDatabase(database, true);
    }

    /**
     * Outputs the DDL required to drop and recreate the database 
     */
    public void createDatabase(Database database, boolean dropTable) 
        throws IOException, SQLException {

        // map the database to the types supported by the provider
        database = map(database);

        JellyContext context = new JellyContext();
        context.setVariable(DATABASE_VAR, database);
        context.setVariable(TAGLIB_VAR, taglibPath.toString());
        context.setVariable(TYPES_VAR, types);

        Writer writer = getWriter();
        XMLOutput output = XMLOutput.createXMLOutput(writer);

        String script = getPath(provider.getCreateScript());

        try {
            context.runScript(script, output);
            writer.flush();
        } catch (IOException exception) {
                throw exception;
        } catch (Exception exception) {
            throw new IOException(exception.getMessage());
        }
    }

    /**
     * Outputs the DDL required to drop the database 
     */
    public void dropDatabase(Database database) 
        throws IOException, SQLException {

        // map the database to the types supported by the provider
        database = map(database);

        JellyContext context = new JellyContext();
        context.setVariable(DATABASE_VAR, database);
        context.setVariable(TAGLIB_VAR, taglibPath.toString());
        context.setVariable(TYPES_VAR, types);

        Writer writer = getWriter();
        XMLOutput output = XMLOutput.createXMLOutput(writer);

        String script = getPath(provider.getDropScript());

        try {
            context.runScript(script, output);
            writer.flush();
        } catch (IOException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IOException(exception.getMessage());
        }
    }

    private Database map(Database database) throws SQLException {
        // @todo - make sense to support alternative mappers?
        DatabaseMapper mapper = new DefaultDatabaseMapper();
        mapper.setDatabase(database);
        mapper.setTypes(types);

        return mapper.map();
    }

    private String getPath(String resource) {
        String result = basePath.toString();
        if (!result.endsWith("/")) {
            result += "/";
        }
        result += resource;
        return result;
    }
}
