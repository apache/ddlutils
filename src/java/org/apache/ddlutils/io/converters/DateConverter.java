package org.apache.ddlutils.io.converters;

/*
 * Copyright 1999-2006 The Apache Software Foundation.
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

import java.sql.Date;
import java.sql.Types;

/**
 * Converts between {@link java.sql.Date} and {@link java.lang.String} using the standard
 * representation "yyyy", or "yyyy-mm", or "yyyy-mm-dd".
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class DateConverter implements SqlTypeConverter 
{
    /**
     * {@inheritDoc}
     */
    public Object convertFromString(String textRep, int sqlTypeCode) throws ConversionException
    {
        if (sqlTypeCode != Types.DATE)
        {
            return textRep;
        }
        else if (textRep != null) 
        {
            // we're not using {@link java.sql.Date#valueOf(String)} as this method is too strict
            // it only parses the full spec "yyyy-mm-dd"

            String dateAsText = textRep;
            int    year       = 1970;
            int    month      = 1;
            int    day        = 1;
            int    slashPos   = dateAsText.indexOf('-');

            try
            {
                if (slashPos < 0)
                {
                    year = Integer.parseInt(dateAsText);
                }
                else
                {
                    year       = Integer.parseInt(dateAsText.substring(0, slashPos));
                    dateAsText = dateAsText.substring(slashPos + 1);
                    slashPos   = dateAsText.indexOf('-');
                    if (slashPos < 0)
                    {
                        month = Integer.parseInt(dateAsText);
                    }
                    else
                    {
                        month = Integer.parseInt(dateAsText.substring(0, slashPos));
                        day   = Integer.parseInt(dateAsText.substring(slashPos + 1));
                    }
                }
                return new Date(year - 1900, month - 1, day);
            }
            catch (NumberFormatException ex)
            {
                throw new ConversionException(ex);
            }
            catch (IllegalArgumentException ex)
            {
                throw new ConversionException(ex);
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String convertToString(Object obj, int sqlTypeCode) throws ConversionException
    {
        return obj == null ? null : obj.toString();
    }
}
