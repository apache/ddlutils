package org.apache.ddlutils.util;

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

import java.sql.Types;

import org.apache.ddlutils.model.TypeMap;

/**
 * Little helper class providing functions for dealing with the newer JDBC functionality.
 */
public abstract class Jdbc3Utils
{
    /**
     * Determines whether the system supports the Java 1.4 JDBC Types, DATALINK
     * and BOOLEAN.
     *   
     * @return <code>true</code> if BOOLEAN and DATALINK are available
     */
    public static boolean supportsJava14JdbcTypes()
    {
        try
        {
            return (Types.class.getField(TypeMap.BOOLEAN) != null) &&
                   (Types.class.getField(TypeMap.DATALINK) != null);
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * Determines the type code for the BOOLEAN JDBC type.
     * 
     * @return The type code
     * @throws UnsupportedOperationException If the BOOLEAN type is not supported
     */
    public static int determineBooleanTypeCode() throws UnsupportedOperationException
    {
        try
        {
            return Types.class.getField(TypeMap.BOOLEAN).getInt(null);
        }
        catch (Exception ex)
        {
            throw new UnsupportedOperationException("The jdbc type BOOLEAN is not supported");
        }
    }

    /**
     * Determines the type code for the DATALINK JDBC type.
     * 
     * @return The type code
     * @throws UnsupportedOperationException If the DATALINK type is not supported
     */
    public static int determineDatalinkTypeCode() throws UnsupportedOperationException
    {
        try
        {
            return Types.class.getField(TypeMap.DATALINK).getInt(null);
        }
        catch (Exception ex)
        {
            throw new UnsupportedOperationException("The jdbc type DATALINK is not supported");
        }
    }
}
