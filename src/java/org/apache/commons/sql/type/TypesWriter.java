package org.apache.commons.sql.type;

import java.io.OutputStream;
import java.io.Writer;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanWriter;


/**
 * This class outputs a fully populated Types bean as XML.
 * 
 * @author <a href="mailto:tima@intalio.com">Tim Anderson</a>
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version 1.1
 */
public class TypesWriter extends BeanWriter {
    
    public TypesWriter(OutputStream out) {
        super(out);
        init();
    }
    
    public TypesWriter(Writer writer) {
        super(writer);
        init();
    }
    
    /**
     * Common initialization code
     */
    private void init() {
        setXMLIntrospector(TypesReader.newXMLIntrospector());
        enablePrettyPrint();
        setWriteIDs(false);
    }
}
