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
