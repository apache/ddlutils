package org.apache.ddlutils.io;

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
import org.apache.ddlutils.model.Database;

/**
 * This class parsers XML and creates a fully populated Database bean.
 * This class is-a Digester and so can support configuration via custom rules.
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision$
 */
public class DatabaseReader extends BeanReader
{
    /** Whether to use the internal dtd that comes with commons-sql */
    private boolean _useInternalDtd = false;

    public DatabaseReader() throws IntrospectionException {
        setXMLIntrospector( newXMLIntrospector() );
        registerBeanClass(Database.class);
        setValidating(false);
    }

    /**
     * Returns whether the internal dtd that comes with commons-sql is used.
     * 
     * @return <code>true</code> if parsing uses the internal dtd
     */
    public boolean isUseInternalDtd()
    {
        return _useInternalDtd;
    }

    /**
     * Specifies whether the internal dtd is to be used.
     *
     * @param useInternalDtd Whether to use the internal dtd 
     */
    public void setUseInternalDtd(boolean useInternalDtd)
    {
        _useInternalDtd = useInternalDtd;
        if (_useInternalDtd)
        {
            setEntityResolver(new LocalEntityResolver());
        }
        else
        {
            setEntityResolver(this);
        }
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

        // set the mixed case name mapper
        introspector.setElementNameMapper(new HyphenatedNameMapper());
        //introspector.setElementNameMapper(new DecapitalizeNameMapper());

        return introspector;
    }
}
