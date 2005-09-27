package org.apache.ddlutils;

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
 * This exception is thrown when a database operation failed.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class DynaSqlException extends DdlUtilsException 
{
    /** Constant for serializing instances of this class. */
    private static final long serialVersionUID = 7524362294381844776L;

    /**
     * Creates a new empty exception object.
     */
    public DynaSqlException()
    {
        super();
    }

    /**
     * Creates a new exception object.
     * 
     * @param msg The exception message
     */
    public DynaSqlException(String msg)
    {
        super(msg);
    }

    /**
     * Creates a new exception object.
     * 
     * @param baseEx The base exception
     */
    public DynaSqlException(Throwable baseEx)
    {
        super(baseEx);
    }

    /**
     * Creates a new exception object.
     * 
     * @param msg    The exception message
     * @param baseEx The base exception
     */
    public DynaSqlException(String msg, Throwable baseEx)
    {
        super(msg, baseEx);
    }

}
