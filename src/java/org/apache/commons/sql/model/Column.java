/*
 * $Header: /home/cvs/jakarta-commons-sandbox/jelly/src/java/org/apache/commons/jelly/CompilableTag.java,v 1.5 2002/05/17 15:18:12 jstrachan Exp $
 * $Revision: 1.5 $
 * $Date: 2002/05/17 15:18:12 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * $Id: CompilableTag.java,v 1.5 2002/05/17 15:18:12 jstrachan Exp $
 */
package org.apache.commons.sql.model;


public class Column
{
    private String name;
    private boolean primaryKey = false;
    private boolean required = false;
    private boolean autoIncrement = false;
    private int typeCode;
    private String type;
    private int size = 0;
    private String defaultValue = null;
    private int scale = 0;
    private int precisionRadix = 10;
    private int ordinalPosition = 0;

    public Column()
    {
    }


    public Column(String name, int typeCode, int size, boolean required, boolean
                  primaryKey, boolean autoIncrement, String defaultValue)
    {
        this.name = name;
        this.typeCode = typeCode;
        this.type = TypeMap.getJdbcTypeName(typeCode);
        this.size = size;
        this.required = required;
        this.primaryKey = primaryKey;
        this.autoIncrement = autoIncrement;
        this.defaultValue = defaultValue;
    }

    public Column(String name, String type, int size, boolean required, boolean
                  primaryKey, boolean autoIncrement, String defaultValue  )
    {
        this(name, TypeMap.getJdbcTypeCode(type), size, required, primaryKey, autoIncrement, defaultValue);
    }

    public Column(String name, int typeCode, int size, boolean required, boolean
                  primaryKey, boolean autoIncrement, String defaultValue,
                  int scale)
    {
        this.name = name;
        this.typeCode = typeCode;
        this.type = TypeMap.getJdbcTypeName(typeCode);
        this.size = size;
        this.required = required;
        this.primaryKey = primaryKey;
        this.autoIncrement = autoIncrement;
        this.defaultValue = defaultValue;
        this.scale = scale;
    }

    public String toString()
    {
        return super.toString() + "[name=" + name + ";type=" + type + "]";
    }

    public String toStringAll()
    {
        return "Column[name=" + name +
            ";type=" + type +
            ";typeCode=" + typeCode +
            ";size=" + size +
            ";required=" + required +
            ";pk=" + primaryKey +
            ";auto=" + autoIncrement +
            ";default=" + defaultValue +
            ";scale=" + scale +
            ";prec=" + precisionRadix +
            ";ord=" + ordinalPosition +
            "]";
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isPrimaryKey()
    {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey)
    {
        this.primaryKey = primaryKey;
    }

    public boolean isRequired()
    {
        return required;
    }

    public void setRequired(boolean required)
    {
        this.required = required;
    }

    public boolean isAutoIncrement()
    {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement)
    {
        this.autoIncrement = autoIncrement;
    }

    public int getTypeCode()
    {
        return typeCode;
    }

    public void setTypeCode(int typeCode)
    {
        this.typeCode = typeCode;
        this.type = TypeMap.getJdbcTypeName(typeCode);
    }

    public String getType()
    {
        return type;
    }

    /**
     * Set this columns type by name
     */
    public void setType(String type)
    {
        this.type = type;
        this.typeCode = TypeMap.getJdbcTypeCode(type);
    }

    public int getSize()
    {
        return size;
    }

    public void setSize(int size)
    {
        this.size = size;
    }

    public int getScale()
    {
        return this.scale;
    }

    public void setScale(int scale)
    {
        this.scale = scale;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public int getPrecisionRadix()
    {
        return this.precisionRadix;
    }

    public void setPrecisionRadix(int precisionRadix)
    {
        this.precisionRadix = precisionRadix;
    }

    public int getOrdinalPosition()
    {
        return this.ordinalPosition;
    }

    public void setOrdinalPosition(int ordinalPosition)
    {
        this.ordinalPosition = ordinalPosition;
    }



}
