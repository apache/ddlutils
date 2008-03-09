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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.ddlutils.DdlUtilsException;

/**
 * Converts between {@link java.sql.Time} and {@link java.lang.String} using the standard
 * representation "hh:mm:ss".
 * 
 * @version $Revision: 289996 $
 */
public class TimeConverter implements SqlTypeConverter 
{
    /** The regular expression pattern for the parsing of ISO times. */
    private Pattern _timePattern;
	/** The calendar object to convert to/from times. */
	private Calendar _calendar;

	/**
	 * Creates a new time converter object.
	 */
	public TimeConverter()
	{
        try
        {
            _timePattern = Pattern.compile("(?:\\d{4}\\-\\d{2}\\-\\d{2}\\s)?(\\d{2})(?::(\\d{2}))?(?::(\\d{2}))?(?:\\..*)?");
        }
        catch (PatternSyntaxException ex)
        {
            throw new DdlUtilsException(ex);
        }

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
            Matcher matcher = _timePattern.matcher(textRep);
            int     hours   = 0;
            int     minutes = 0;
            int     seconds = 0;

            if (matcher.matches())
            {
                int numGroups = matcher.groupCount();

                try
                {
                    hours = Integer.parseInt(matcher.group(1));
                    if ((numGroups >= 2) && (matcher.group(2) != null))
                    {
                        minutes = Integer.parseInt(matcher.group(2));
                    }
                    if ((numGroups >= 3) && (matcher.group(3) != null))
                    {
                        seconds = Integer.parseInt(matcher.group(3));
                    }
                }
                catch (NumberFormatException ex)
                {
                    throw new ConversionException("Not a valid time : " + textRep, ex);
                }
                _calendar.clear();
                try
                {
                    _calendar.set(Calendar.HOUR_OF_DAY, hours);
                    _calendar.set(Calendar.MINUTE, minutes);
                    _calendar.set(Calendar.SECOND, seconds);
                    return new Time(_calendar.getTimeInMillis());
                }
                catch (IllegalArgumentException ex)
                {
                    throw new ConversionException("Not a valid time : " + textRep, ex);
                }
            }
            else
            {
                throw new ConversionException("Not a valid time : " + textRep);
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
