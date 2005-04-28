package org.apache.ddlutils.ddl;

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
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A factory of DDLBuilder instances based on a database provider name, and
 * optionally, provider version
 * 
 * @author <a href="mailto:tima@intalio.com">Tim Anderson</a>
 * @version 1.1
 */
public class DDLBuilderFactory {

    /**
     * The URL of the core taglib
     */
    private static URL taglibPath;

    /**
     * The base URL for provider resources
     */
    private static URL basePath;

    /**
     * The set of known database providers
     */
    private static Providers providers;

    /**
     * The path to locate resources
     */
    private static final String RESOURCE_PATH = "/META-INF/commons-sql/";

    /**
     * The path of the core taglib
     */
    private static final String TAGLIB_PATH = RESOURCE_PATH + "taglib.jelly";

    /**
     * The resource path to the providers configuration
     */
    private static final String PROVIDERS_PATH = 
        RESOURCE_PATH + "providers.xml";

    /**
     * The logger
     */
    private static final Log log = LogFactory.getLog(DDLBuilderFactory.class);


    /**
     * Creates a new DDLBuilder for the given (case insensitive) JDBC provider
     * name, or returns null if the provider is not recognized.
     *
     * @throws IOException if an I/O error is encountered
     */
    public static synchronized DDLBuilder newDDLBuilder(String name) 
        throws IOException {

        return newDDLBuilder(name, null);
    }

    /**
     * Creates a new DDLBuilder for the given (case insensitive) JDBC provider
     * name and version, or returns null if the provider is not recognized.
     *
     * @throws IOException if an I/O error is encountered
     */
    public static synchronized DDLBuilder newDDLBuilder(
        String name, String version)
        throws IOException {

        DDLBuilder result = null;
        
        if (providers == null) {
            init();
        }

        Provider provider  = providers.getProvider(name);
        if (provider != null) {
            ProviderVersion providerVersion = 
                provider.getProviderVersion(version);
            if (providerVersion != null) {
                result = new DDLBuilder(providerVersion, taglibPath, basePath);
            }
        }
        return result;
    }

    /**
     * Returns the list of registered database providers for which there 
     * is a specific DDLBuilder.
     *
     * @return a list of registered database types for which there is a 
     * specific DDLBuilder.
     * @throws IOException if an I/O error is encountered
     */
    public static synchronized List getProviders() throws IOException {
        if (providers == null) {
            init();
        }
        return providers.getProviders();
    }

    protected static void init() throws IOException {
        // get the core taglib path
        taglibPath = DDLBuilderFactory.class.getResource(TAGLIB_PATH);
        if (taglibPath == null) {
            String message = "Failed to locate core tag library, path=" + 
                TAGLIB_PATH;
            throw new IOException(message);
        } 

        // load the providers 
        URL url = DDLBuilderFactory.class.getResource(PROVIDERS_PATH);
        if (url == null) {
            String message = "Failed to locate providers, path=" + 
                PROVIDERS_PATH;
            throw new IOException(message);
        } 

        InputStream stream = null;
        try {
            stream = url.openStream();
            BeanReader reader = new ProvidersReader();
            providers = (Providers) reader.parse(stream);
        } catch (IOException exception) {
            throw exception;
        } catch (Exception exception) {
            String message = "Failed to load providers, path=" + 
                PROVIDERS_PATH;
            throw new IOException(message + ": " + exception.getMessage());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignore) {
                }
            }
        }

        if (providers == null) {
            throw new IOException("Failed to load providers, path=" +
                                  PROVIDERS_PATH);
        }

        // determine the base resource path
        // @todo - need to support multiple providers.xml resources and
        // merge them. This will require several basePaths
        String path = url.toString();
        basePath = new URL(path.substring(0, path.lastIndexOf("/") + 1));
    }

}
