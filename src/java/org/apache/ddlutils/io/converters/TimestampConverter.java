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

import java.sql.Timestamp;
import java.sql.Types;

/**
 * Converts between {@link java.sql.Timestamp} and {@link java.lang.String} using the standard
 * representation "yyyy-mm-dd hh:mm:ss.fffffffff".
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class TimestampConverter implements SqlTypeConverter 
{
    /**
     * {@inheritDoc}
     */
    public Object convertFromString(String textRep, int sqlTypeCode) throws Exception
    {
        return sqlTypeCode == Types.TIMESTAMP ? Timestamp.valueOf(textRep) : (Object)textRep;
    }

    /**
     * {@inheritDoc}
     */
    public String convertToString(Object obj, int sqlTypeCode)
    {
        return ((Timestamp)obj).toString();
    }
}
