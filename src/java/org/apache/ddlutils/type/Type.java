package org.apache.ddlutils.type;

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
