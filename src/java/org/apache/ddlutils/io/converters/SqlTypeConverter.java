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

/**
 * Interface for classes that convert between strings and sql data types.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public interface SqlTypeConverter
{
    /**
     * Converts the given textual representation to an instance of the target type.
     * 
     * @param textRep     The textual representation
     * @param sqlTypeCode The target sql type code, one of the constants in {@link java.sql.Types}
     * @return The corresponding object
     */
    public Object convertFromString(String textRep, int sqlTypeCode) throws Exception;

    /**
     * Converts the given object to a string representation.
     * 
     * @param obj         The object
     * @param sqlTypeCode The corresponding source type code
     * @return The textual representation
     */
    public String convertToString(Object obj, int sqlTypeCode) throws Exception;
}
