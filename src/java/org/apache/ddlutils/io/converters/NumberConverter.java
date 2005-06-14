package org.apache.ddlutils.io.converters;

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

import java.math.BigDecimal;
import java.sql.Types;

import org.apache.ddlutils.util.Jdbc3Utils;

/**
 * Converts between the various number types (including boolean types) and {@link java.lang.String}.
 */
public class NumberConverter implements SqlTypeConverter
{
    /* (non-Javadoc)
     * @see org.apache.ddlutils.io.converters.SqlTypeConverter#convertFromString(java.lang.String, int)
     */
    public Object convertFromString(String textRep, int sqlTypeCode) throws Exception
    {
        if (textRep == null)
        {
            return null;
        }
        else
        {
            if (Jdbc3Utils.supportsJava14JdbcTypes() &&
                (sqlTypeCode == Jdbc3Utils.determineBooleanTypeCode()))
            {
                return Boolean.valueOf(textRep);
            }
            switch (sqlTypeCode)
            {
                case Types.BIGINT:
                    return Long.valueOf(textRep);
                case Types.BIT:
                    int value = Byte.parseByte(textRep);

                    if (value == 0)
                    {
                        return Boolean.FALSE;
                    }
                    else if (value == 1)
                    {
                        return Boolean.TRUE;
                    }
                    else
                    {
                        throw new IllegalArgumentException("Cannot convert string "+textRep);
                    }
                case Types.DECIMAL:
                case Types.NUMERIC:
                    return new BigDecimal(textRep);
                case Types.DOUBLE:
                case Types.FLOAT:
                    return Double.valueOf(textRep);
                case Types.INTEGER:
                    return Integer.valueOf(textRep);
                case Types.REAL:
                    return Float.valueOf(textRep);
                case Types.SMALLINT:
                case Types.TINYINT:
                    return Short.valueOf(textRep);
                default:
                    return textRep;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.io.converters.SqlTypeConverter#convertToString(java.lang.Object, int)
     */
    public String convertToString(Object obj, int sqlTypeCode) throws Exception
    {
        if (obj == null)
        {
            return null;
        }
        else if (sqlTypeCode == Types.BIT)
        {
            return ((Boolean)obj).booleanValue() ? "1" : "0";
        }
        else
        {
            return obj.toString();
        }
    }
}
