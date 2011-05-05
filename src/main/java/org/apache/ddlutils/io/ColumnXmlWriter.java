package org.apache.ddlutils.io;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.ddlutils.model.Column;

/**
 * Helper class for writing columns to XML.  
 */
public class ColumnXmlWriter extends ModelXmlWriter
{
    private final int AS_TABLE_ATTRIBUTE  = 0;
    private final int AS_SUBTAG           = 1;
    private final int AS_COLUMN_ATTRIBUTE = 2;
    private final int AS_VALUE            = 3;

    private final String columnName;
    private final String columnValue;
    private final boolean nameBase64Encoded;
    private final boolean valueBase64Encoded;
    private final int columnFormattingMethod;

    /**
     * Creates a new column writer.
     * 
     * @param column The column, cannot be null
     * @param value  The value, cannot be null
     */
    public ColumnXmlWriter(Column column, String value)
    {
        /*
         * - attribute "column name"="column value" in the parent's (table) element
         *   iff the column name is a valid attribute name and is not "table-name" and not "column",
         *   and the value is a valid attribute value not longer than 255 characters
         * - otherwise, writes a sub-element <column> with an attribute column-name that contains the name
         *   of the column, and the body of that sub-element contains the column value,
         *   iff the column name is a valid attribute value not longer than 255 characters. If the column
         *   value contains illegal characters, then the column sub element will have a "base64" attribute
         *   with the value "true" and the value will be base64 encoded
         * - otherwise writes a sub-element <column> with a sub-element <column-name> whose
         *   body is the name of the column, and another sub-element <column-value> whose body contains
         *   the column value. If either the column name or value contain illegal characters, then the
         *   corresponding sub element will have a "base64" attribute with the value "true" and its body will
         *   be base64 encoded.
         */
        if (XMLUtils.hasIllegalXMLCharacters(value))
        {
            columnValue        = XMLUtils.base64Encode(value);
            valueBase64Encoded = true;
        }
        else
        {
            columnValue        = value;
            valueBase64Encoded = false;
        }

        if (XMLUtils.hasIllegalXMLCharacters(column.getName())) {
            columnName             = XMLUtils.base64Encode(column.getName());
            nameBase64Encoded      = true;
            columnFormattingMethod = AS_VALUE;
        }
        else
        {
            columnName        = column.getName();
            nameBase64Encoded = false;
            if (columnName.length() > XMLUtils.MAX_NAME_LENGTH)
            {
                columnFormattingMethod = AS_VALUE;
            }
            else if ("table-name".equals(columnName) ||
                     DatabaseIO.BASE64_ATTR_NAME.equals(columnName) ||
                     !XMLUtils.isWellFormedXMLName(columnName))
            {
                columnFormattingMethod = AS_COLUMN_ATTRIBUTE;
            }
            else if (valueBase64Encoded || (value.length() > XMLUtils.MAX_ATTRIBUTE_LENGTH))
            {
                columnFormattingMethod = AS_SUBTAG;
            }
            else
            {
                columnFormattingMethod = AS_TABLE_ATTRIBUTE;
            }
        }
    }

    /**
     * Writes the column data as an attribute of the parent element if possible.
     * Does nothing if the column name or value cannot be used in an attribute.
     * 
     * @param writer The writer to write to
     * @return <code>true</code> if something was written
     */
    public boolean writeAttribute(DataWriter writer)
    {
        if (columnFormattingMethod == AS_TABLE_ATTRIBUTE)
        {
            writer.writeAttribute(null, columnName, columnValue);
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Writes any sub elements necessary for the column. If no sub elements
     * are required, then this method does nothing.
     * 
     * @param writer The writer to write to
     * @return <code>true</code> if something was written
     */
    public boolean writeSubElement(DataWriter writer)
    {
        if (columnFormattingMethod != AS_TABLE_ATTRIBUTE)
        {
            writer.printlnIfPrettyPrinting();
            writer.indentIfPrettyPrinting(2);
            if (columnFormattingMethod == AS_SUBTAG)
            {
                writer.writeElementStart(null, columnName);
                writeText(writer, columnValue, valueBase64Encoded);
            }
            else
            {
                writer.writeElementStart(null, "column");
                if (columnFormattingMethod == AS_COLUMN_ATTRIBUTE)
                {
                    writer.writeAttribute(null, "column-name", columnName);
                    writeText(writer, columnValue, valueBase64Encoded);
                }
                else if (columnFormattingMethod == AS_VALUE)
                {
                    writer.printlnIfPrettyPrinting();
                    writer.indentIfPrettyPrinting(3);
                    writer.writeElementStart(null, "column-name");
                    writeText(writer, columnName, nameBase64Encoded);
                    writer.writeElementEnd();

                    writer.printlnIfPrettyPrinting();
                    writer.indentIfPrettyPrinting(3);
                    writer.writeElementStart(null, "column-value");
                    writeText(writer, columnValue, valueBase64Encoded);
                    writer.writeElementEnd();
                    writer.printlnIfPrettyPrinting();
                    writer.indentIfPrettyPrinting(2);
                }
            }
            writer.writeElementEnd();
            return true;
        }
        else
        {
            return false;
        }
    }
}
