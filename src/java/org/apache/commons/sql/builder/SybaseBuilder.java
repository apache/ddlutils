package org.apache.commons.sql.builder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.Database;
import org.apache.commons.sql.model.Table;

/**
 * An SQL Builder for Sybase
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.14 $
 */
public class SybaseBuilder extends SqlBuilder {
    
    public SybaseBuilder() {
    }
    
    protected void writeComment(String text) throws IOException { 
        writer.write( "/* " );
        writer.write( text );
        writer.println( " */" );
    }
}
