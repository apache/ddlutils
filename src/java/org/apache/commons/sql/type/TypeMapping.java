package org.apache.commons.sql.type;

import org.apache.commons.sql.model.Column;


/**
 * Associates an SQL type with its JDBC mapping
 * 
 * @version     1.1 2003/02/05 08:08:37
 * @author      <a href="mailto:tima@intalio.com">Tim Anderson</a>
 */
public class TypeMapping {

    /**
     * The SQL type
     */
    private Type type;

    /**
     * The JDBC type mapping
     */
    private Mapping mapping;


    /**
     * Construct a new <code>TypeMapping</code>
     */
    public TypeMapping() {
    }

    /**
     * Construct a new <code>TypeMapping</code>
     */
    public TypeMapping(Type type, Mapping mapping) {
        this.type = type;
        this.mapping = mapping;
    }

    /**
     * Returns the SQL type
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the SQL type
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Returns the JDBC mapping of the type
     */
    public Mapping getMapping() {
        return mapping;
    }

    /**
     * Sets the JDBC mapping of the type
     */
    public void setMapping(Mapping mapping) {
        this.mapping = mapping;
    }

    /**
     * Returns the JBDC name of the type
     */
    public String getName() {
        return mapping.getName();
    }

    /**
     * Returns the SQL type name
     */
    public String getSQLName() {
        return type.getSQLName();
    }

    /**
     * Returns the maximum size (length or precision) of the type
     */
    public long getSize() {
        return type.getSize();
    }

    /**
     * Returns the minimum scale of the type
     */
    public short getMinimumScale() {
        return type.getMinimumScale();
    }

    /**
     * Returns the maximum scale of the type
     */
    public short getMaximumScale() {
        return type.getMaximumScale();
    }

    /**
     * Returns the format of the type
     */
    public String getFormat() {
        return mapping.getFormat();
    }

    /**
     * Returns the SQL type for a column
     *
     * @param column the column 
     * @return the SQL type of <code>column</code>
     */
    public String getSQLType(Column column) {
        return mapping.getSQLType(column);
    }

    /**
     * Helper to return a stringified version of the object, for debug purposes
     */
    public String toString() {
        return super.toString() + "[type=" + type + ";mapping=" + mapping + 
            "]";
    }
    
}
