package org.apache.ddlutils.io.converters;

/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.ddlutils.util.Jdbc3Utils;

/**
 * Converts between the various number types (including boolean types) and {@link java.lang.String}.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class NumberConverter implements SqlTypeConverter
{
    /**
     * {@inheritDoc}
     */
    public Object convertFromString(String textRep, int sqlTypeCode) throws ConversionException
    {
        if (textRep == null)
        {
            return null;
        }
        else
        {
            Class  targetClass = null;

            switch (sqlTypeCode)
            {
                case Types.BIGINT:
                    targetClass = Long.class;
                    break;
                case Types.BIT:
                    targetClass = Boolean.class;
                    break;
                case Types.DECIMAL:
                case Types.NUMERIC:
                    targetClass = BigDecimal.class;
                    break;
                case Types.DOUBLE:
                case Types.FLOAT:
                    targetClass = Double.class;
                    break;
                case Types.INTEGER:
                    targetClass = Integer.class;
                    break;
                case Types.REAL:
                    targetClass = Float.class;
                    break;
                case Types.SMALLINT:
                case Types.TINYINT:
                    targetClass = Short.class;
                    break;
                default:
                    if (Jdbc3Utils.supportsJava14JdbcTypes() &&
                        (sqlTypeCode == Jdbc3Utils.determineBooleanTypeCode()))
                    {
                        targetClass = Boolean.class;
                    }
                    break;
            }
            return targetClass == null ? textRep : ConvertUtils.convert(textRep, targetClass);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String convertToString(Object obj, int sqlTypeCode) throws ConversionException
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
