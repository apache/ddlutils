package org.apache.commons.sql.type;


/**
 * Describes an SQL type
 * 
 * @version     1.1 2003/02/05 08:08:37
 * @author      <a href="mailto:tima@intalio.com">Tim Anderson</a>
 */
public class Type {

    /**
     * The name of the type.
     */
    private String sqlName;

    /**
     * The maximum precision or value of the type
     */
    private long size;

    /**
     * The minimum scale supported by the type
     */
    private short minScale;

    /**
     * The maximum scale supported by the type
     */
    private short maxScale;


    /**
     * Construct a new <code>Type</code>
     */
    public Type() {
    }

    /**
     * Construct a new <code>Type</code>
     *
     * @param sqlName the SQL name of the type
     * @param size the maximum size/precision of the type
     * @param minScale the minimum scale supported by the type
     * @param maxScale the maximum scale supported by the type
     */
    public Type(String sqlName, long size, short minScale, short maxScale) {
        this.sqlName = sqlName;
        this.size = size;
        this.minScale = minScale;
        this.maxScale = maxScale;
    }

    /**
     * Returns the SQL name of the type
     */
    public String getSQLName() {
        return sqlName;
    }

    /**
     * Sets the SQL name of the type
     */
    public void setSQLName(String name) {
        this.sqlName = name;
    }

    /**
     * Returns the maximum size (or precision) of the type
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets the maximum size (or precision) of the type
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * Returns the minimum scale of the type
     */
    public short getMinimumScale() {
        return minScale;
    }

    /**
     * Sets the minimum scale of the type
     */
    public void setMinimumScale(short scale) {
        minScale = scale;
    }

    /**
     * Returns the maximum scale of the type
     */
    public short getMaximumScale() {
        return maxScale;
    }

    /**
     * Sets the maximum scale of the type
     */
    public void setMaximumScale(short scale) {
        maxScale = scale;
    }

    /**
     * Helper to return a stringified version of the type, for debug purposes
     */
    public String toString() {
        return super.toString() + "[SQLName=" + sqlName + ";size=" + size +
            ";minimumScale=" + minScale + ";maximumScale="+ maxScale + "]";
    }
    
}
