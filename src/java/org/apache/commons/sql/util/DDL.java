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
package org.apache.commons.sql.util;

import java.io.FileInputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Parser;
import org.apache.commons.sql.ddl.DDLBuilder;
import org.apache.commons.sql.ddl.DDLBuilderFactory;
import org.apache.commons.sql.ddl.Provider;
import org.apache.commons.sql.ddl.ProviderVersion;
import org.apache.commons.sql.io.DatabaseReader;
import org.apache.commons.sql.model.Database;
import org.apache.commons.sql.type.Types;
import org.apache.commons.sql.type.TypesFactory;
import org.apache.commons.sql.type.TypesWriter;


/**
 * Command line utility to 
 * <ul>
 *   <li>Execute DDL from a schema against a particular database provider</li>
 *   <li>List the supported databases</li>
 *   <li>Create the types for a database</li>
 * </ul>
 */
public class DDL {

    public static void main(String args[]) {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(
            new Option("execute", false, "generate and execute DDL"));
        group.addOption(new Option("print", false, "generate DDL"));
        group.addOption(new Option("list", false, "list available providers"));
        group.addOption(new Option("types", false, "generate type meta-data"));
        options.addOptionGroup(group);

        options.addOption("db", true, "the database provider");
        options.addOption("url", true, "database URL");
        options.addOption("driver", true, "driver class");
        options.addOption("user", true, "user name");
        options.addOption("password", true, "password");
        options.addOption("drop", false, "generate drop statements");
        options.addOption("schema", true, "the database schema");
        options.addOption("version", true, "specifies the provider version");

        Parser parser = new GnuParser();
        try {
            CommandLine commands = parser.parse(options, args);

            boolean execute = commands.hasOption("execute");
            boolean print = commands.hasOption("print");
            boolean list = commands.hasOption("list");
            boolean types = commands.hasOption("types");
            String db = commands.getOptionValue("db");
            String version = commands.getOptionValue("version");
            String schema = commands.getOptionValue("schema");
            String driver = commands.getOptionValue("driver");
            String url = commands.getOptionValue("url");
            String user = commands.getOptionValue("user");
            String password = commands.getOptionValue("password");
            boolean drop = commands.hasOption("drop");

            if (execute) {
                checkArg(db, "db", options);
                checkArg(driver, "driver", options);
                checkArg(url, "url", options);
                checkArg(schema, "schema", options);
                execute(!drop, db, version, schema, driver, url, user, 
                        password);
            } else if (print) {
                checkArg(db, "db", options);
                checkArg(schema, "schema", options);
                print(!drop, db, version, schema);
            } else if (list) {
                list();
            } else if (types) {
                checkArg(driver, "driver", options);
                checkArg(url, "url", options);
                types(driver, url, user, password);
            } else {
                usage(options, null);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            System.exit(1);
        }
    }

    private static void execute(
        boolean create, String provider, String version, String schema, 
        String driver, String url, String user, String password)
        throws Exception {

        DatabaseReader reader = new DatabaseReader();
        Database database = (Database) reader.parse(
            new FileInputStream(schema));

        DDLBuilder builder = DDLBuilderFactory.newDDLBuilder(
            provider, version);
        if (builder == null) {
            throw new Exception("Database provider/version unknown: " + 
                                provider + "/" + version);
        }
        StringWriter buffer = new StringWriter();
        builder.setWriter(buffer);
        if (create) {
            builder.createDatabase(database, false);
        } else {
            builder.dropDatabase(database);
        }
        buffer.flush();

        Executor executor = new Executor(driver, url, user, password);
        executor.execute(buffer.toString());
    }

    private static void print(boolean create, String provider, String version,
                              String schema) throws Exception {
        DatabaseReader reader = new DatabaseReader();
        Database database = (Database) reader.parse(
            new FileInputStream(schema));
        
        DDLBuilder builder = DDLBuilderFactory.newDDLBuilder(
            provider, version);
        if (builder == null) {
            throw new Exception("Database provider/version unknown: " + 
                                provider + "/" + version);
        }
        StringWriter buffer = new StringWriter();
        builder.setWriter(buffer);
        if (create) {
            builder.createDatabase(database, false);
        } else {
            builder.dropDatabase(database);
        }
        buffer.flush();
        System.out.println(buffer.toString());
    }

    private static void list() throws Exception {
        List providers = DDLBuilderFactory.getProviders();
        Iterator iterator = providers.iterator();
        while (iterator.hasNext()) {
            Provider provider = (Provider) iterator.next();
            System.out.println(provider.getName());
            Iterator versions = provider.getProviderVersions().iterator();
            while (versions.hasNext()) {
                ProviderVersion version = (ProviderVersion) versions.next();
                System.out.println("  version: " + version.getName());
            }
        }
    }

    private static void types(String driver, String url, String user, 
                              String password) throws Exception {

        Class.forName(driver);
        Connection connection = DriverManager.getConnection(
            url, user, password);

        Types set = TypesFactory.create(connection);
        TypesWriter writer = new TypesWriter(System.out);
        writer.write(set);

        connection.close();
    }

    private static void checkArg(String value, String name, Options options) {
        if (value == null) {
            usage(options, "Missing argument: " + name);
        }
    }

    private static void usage(Options options, String error) {
        if (error != null) {
            System.err.println(error);
        }
        // HelpFormatter formatter = new HelpFormatter();
        // formatter.printHelp("DDL", options);
        System.err.println("usage: " + DDL.class.getName() + " <commands> ");
        System.err.println("commands: ");
        System.err.println("  -execute -db <db> [-version <version>] " +
                           "<db-props> -schema <path> [-drop]");
        System.err.println("  -print -db <provider> [-version <version>] " +
                           " [-drop]");
        System.err.println("  -list");
        System.err.println("  -types <db-props>");
        System.err.println();
        System.err.println("db-props: ");
        System.err.println("  -driver <driver> -url <url> [-user <user>] " +
                           "[-password <password>]");
        System.err.println();
        System.err.println("where: ");
        System.err.println("  -execute    executes DDL for the specified " +
                           "schema");
        System.err.println("  -print      prints DDL for the specified " +
                           "schema");
        System.err.println("  -list       lists available database providers" +
                           " and versions");
        System.err.println("  -types      generates type information from " +
                           "database meta-data");
        System.err.println("  -db         specifies the database provider");
        System.err.println("  -version    specifies the provider version");
        System.err.println("  -schema     specifies the path to a database " +
                           "schema");
        System.err.println("  -drop       specifies to generate DDL 'drop' " +
                           "statements");
        System.err.println("  -driver     specifies the driver class name");
        System.err.println("  -url        specifies the database URL");
        System.err.println("  -user       specifies the database user name");
        System.err.println("  -password   specifies the database user " +
                           "password");

        System.exit(1);
    }

    private static class Executor extends DDLExecutor {

        public Executor(String driver, String url, String user, 
                        String password) throws Exception {

            DataSourceWrapper dataSource = new DataSourceWrapper();
            dataSource.setDriverClassName(driver);
            dataSource.setJdbcURL(url);
            dataSource.setUserName(user);
            dataSource.setPassword(password);
            setDataSource(dataSource);
        }

        public void execute(String sql) throws SQLException {
            evaluateBatch(sql);
        }

    }
}
