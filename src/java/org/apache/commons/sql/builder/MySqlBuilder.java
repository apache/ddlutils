package org.apache.commons.sql.builder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.Database;
import org.apache.commons.sql.model.Table;

/**
 * An SQL Builder for MySQL
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.14 $
 */
public class MySqlBuilder extends SqlBuilder {
    
    public MySqlBuilder() {
    }
    
    public void dropTable(Table table) throws IOException { 
        writer.write( "drop table if exists " );
        writer.write( table.getName() );
        writeEndOfStatement();
    }
    
    protected void writeAutoIncrementColumn() throws IOException { 
        writer.write( "AUTO_INCREMENT " );
    }
}
