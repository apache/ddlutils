package org.apache.commons.sql.ddl;

import java.sql.SQLException;

import org.apache.commons.sql.model.Database;
import org.apache.commons.sql.type.Types;


/**
 * Interface to map a database schema to that supported by a particular
 * database provider
 * 
 * @version     1.1 2003/02/05 08:08:36
 * @author      <a href="mailto:tima@intalio.com">Tim Anderson</a>
 */
public interface DatabaseMapper {

    /**
     * Set the database to be mapped
     */
    public void setDatabase(Database database);

    /**
     * Returns the database to be mapped
     */
    public Database getDatabase();

    /**
     * Set the type set supported by the database provider
     */
    public void setTypes(Types types);

    /**
     * Returns the type set supported by the database provider
     */
    public Types getTypes();

    /**
     * Map the database to that supported by the database provider
     *
     * @throws SQLException if the database cannot be mapped
     */
    public Database map() throws SQLException;

}
