package org.apache.commons.sql.builder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.Database;
import org.apache.commons.sql.model.Table;

/**
 * An SQL Builder for Oracle
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.14 $
 */
public class OracleBuilder extends SqlBuilder {
    
    public OracleBuilder() {
    }
    
    public void dropTable(Table table) throws IOException { 
        writer.write( "drop table " );
        writer.write( table.getName() );
        writer.write( " CASCADE CONSTRAINTS" );
        writeEndOfStatement();
    }
    
    protected boolean isPrimaryKeyEmbedded() {
        return true;
    }
    
    protected boolean isForeignKeysEmbedded() {
        return true;
    }
}
