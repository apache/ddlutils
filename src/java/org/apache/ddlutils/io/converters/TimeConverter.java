package org.apache.ddlutils.io.converters;

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

import java.sql.Time;
import java.sql.Types;
import java.util.Calendar;

/**
 * Converts between {@link java.sql.Time} and {@link java.lang.String} using the standard
 * representation "hh:mm:ss".
 * 
 * @version $Revision: 289996 $
 */
public class TimeConverter implements SqlTypeConverter 
{
	/** The calendar object to convert to/from times. */
	private Calendar _calendar;

	/**
	 * Creates a new time converter object.
	 */
	public TimeConverter()
	{
		_calendar = Calendar.getInstance();

		_calendar.setLenient(false);
	}

	/**
     * {@inheritDoc}
     */
    public Object convertFromString(String textRep, int sqlTypeCode) throws ConversionException
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

            try
            {
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

                _calendar.clear();
                _calendar.set(Calendar.HOUR_OF_DAY, hours);
                _calendar.set(Calendar.MINUTE, minutes);
                _calendar.set(Calendar.SECOND, seconds);
                return new Time(_calendar.getTimeInMillis());
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
        String result = null;

        if (obj != null)
        {
            if (!(obj instanceof Time))
            {
                throw new ConversionException("Expected object of type java.sql.Time, but instead received " + obj.getClass().getName());
            }
            result = obj.toString();
        }
        return result;
    }
}
