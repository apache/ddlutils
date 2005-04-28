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

import java.beans.IntrospectionException;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.betwixt.strategy.HyphenatedNameMapper;


/**
 * This class parses XML and creates a fully populated Providers bean.
 * This class is-a Digester and so can support configuration via custom rules.
 * 
 * @author <a href="mailto:tima@intalio.com">Tim Anderson</a>
 * @version 1.1
 */
public class ProvidersReader extends BeanReader {
    
    public ProvidersReader() throws IntrospectionException {
        setXMLIntrospector(newXMLIntrospector());
        registerBeanClass(Providers.class);
    }

    /**
     * A factory method to create the default introspector used to turn
     * the Database object model into XML
     */    
    protected static XMLIntrospector newXMLIntrospector() {
        XMLIntrospector introspector = new XMLIntrospector();

        introspector.setAttributesForPrimitives(false);
        introspector.setWrapCollectionsInElement(false);
        introspector.setElementNameMapper(new HyphenatedNameMapper());
        return introspector;
    }
}
