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
 * @author <a href="mailto:tima@intalio.com">Tim Anderson</a>
 * @version 1.1
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
