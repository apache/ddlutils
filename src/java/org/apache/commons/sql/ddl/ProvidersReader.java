package org.apache.commons.sql.ddl;

import java.beans.IntrospectionException;
import java.io.IOException;

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
