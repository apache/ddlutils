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

import java.sql.Time;
import java.sql.Types;

/**
 * Converts between {@link java.sql.Time} and {@link java.lang.String} using the standard
 * representation "hh:mm:ss".
 */
public class TimeConverter implements SqlTypeConverter 
{
    /* (non-Javadoc)
     * @see org.apache.ddlutils.io.converters.SqlTypeConverter#convertFromString(java.lang.String, int)
     */
    public Object convertFromString(String textRep, int sqlTypeCode) throws Exception
    {
        if (sqlTypeCode != Types.TIME)
        {
            return textRep;
        }
        else if (textRep != null) 
        {
            // we're not using {@link java.sql.Time#valueOf(String)} as this method is too strict
            // it only parses the full spec "hh:mm:ss"

            String timeAsText = textRep;
            int    hours      = 0;
            int    minutes    = 0;
            int    seconds    = 0;
            int    slashPos   = timeAsText.indexOf(':');

            if (slashPos < 0)
            {
                hours = Integer.parseInt(timeAsText);
            }
            else
            {
                hours      = Integer.parseInt(timeAsText.substring(0, slashPos));
                timeAsText = timeAsText.substring(slashPos + 1);
                slashPos   = timeAsText.indexOf(':');
                if (slashPos < 0)
                {
                    minutes = Integer.parseInt(timeAsText);
                }
                else
                {
                    minutes = Integer.parseInt(timeAsText.substring(0, slashPos));
                    seconds = Integer.parseInt(timeAsText.substring(slashPos + 1));
                }
            }
            return new Time(hours, minutes, seconds);
            
        }
        else
        {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.io.converters.SqlTypeConverter#convertToString(java.lang.Object, int)
     */
    public String convertToString(Object obj, int sqlTypeCode)
    {
        return obj == null ? null : obj.toString();
    }
}
