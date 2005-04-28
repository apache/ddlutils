package org.apache.ddlutils.type;

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

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.betwixt.strategy.HyphenatedNameMapper;
import org.xml.sax.SAXException;


/**
 * This class parsers XML and creates a fully populated Types bean.
 * This class is-a Digester and so can support configuration via custom rules.
 * 
 * @author <a href="mailto:tima@intalio.com">Tim Anderson</a>
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version 1.1
 */
public class TypesReader extends BeanReader {
    
    public TypesReader() throws IntrospectionException {
        setXMLIntrospector(newXMLIntrospector());
        registerBeanClass(Types.class);
    }

    /**
     * Helper to read a Types given its URL
     *
     * @param url The URL of the typeset
     * @return the typeset
     */
    public static Types read(URL url) throws IOException {
        Types result = null;
        InputStream stream = url.openStream();
        try {
            TypesReader reader = new TypesReader();
            result = (Types) reader.parse(stream);
        } catch (IntrospectionException exception) {
            throw new IOException("Failed to construct reader: " + 
                                  exception.getMessage());
        } catch (SAXException exception) {
            throw new IOException("Failed to parse " + url + ": " +
                                  exception.getMessage());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignore) {
                }
            }
        }
        return result;
    }

    /**
     * A factory method to create the default introspector used to turn
     * the Database object model into XML
     */    
    protected static XMLIntrospector newXMLIntrospector() {
        XMLIntrospector introspector = new XMLIntrospector();

        // configure the style of the XML, to brief and attribute based
        introspector.setAttributesForPrimitives(true);
        introspector.setWrapCollectionsInElement(false);

        introspector.setElementNameMapper(new HyphenatedNameMapper());

        return introspector;
    }
}
