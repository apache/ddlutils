/*
 * $Header: /home/cvs/jakarta-commons-sandbox/jelly/src/java/org/apache/commons/jelly/CompilableTag.java,v 1.5 2002/05/17 15:18:12 jstrachan Exp $
 * $Revision: 1.5 $
 * $Date: 2002/05/17 15:18:12 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 * 
 * $Id: CompilableTag.java,v 1.5 2002/05/17 15:18:12 jstrachan Exp $
 */
package org.apache.commons.sql.model;


import java.util.Hashtable;
import java.sql.Types;

// I don't know if the peer system deals
// with the recommended mappings.
//
//import java.sql.Date;
//import java.sql.Time;
//import java.sql.Timestamp;

/**
 * A class that maps SQL type names to their JDBC type ID found in
 * {@link java.sql.Types}.
 *
 * <pre>
 * -------------------------------------------------------
 * SQL Type      | Java Type            | Peer Type
 * -------------------------------------------------------
 * CHAR          | String               | String
 * VARCHAR       | String               | String
 * LONGVARCHAR   | String               | String
 * NUMERIC       | java.math.BigDecimal | java.math.BigDecimal
 * DECIMAL       | java.math.BigDecimal | java.math.BigDecimal
 * BIT           | boolean OR Boolean   | Boolean
 * TINYINT       | byte OR Byte         | Byte
 * SMALLINT      | short OR Short       | Short
 * INTEGER       | int OR Integer       | Integer
 * BIGINT        | long OR Long         | Long
 * REAL          | float OR Float       | Float
 * FLOAT         | double OR Double     | Double
 * DOUBLE        | double OR Double     | Double
 * BINARY        | byte[]               | ?
 * VARBINARY     | byte[]               | ?
 * LONGVARBINARY | byte[]               | ?
 * DATE          | java.sql.Date        | java.util.Date
 * TIME          | java.sql.Time        | java.util.Date
 * TIMESTAMP     | java.sql.Timestamp   | java.util.Date
 *
 * -------------------------------------------------------
 * A couple variations have been introduced to cover cases
 * that may arise, but are not covered above
 * BOOLEANCHAR   | boolean OR Boolean   | String
 * BOOLEANINT    | boolean OR Boolean   | Integer
 * </pre>
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.14 $
 */
public class TypeMap
{
    public static final String CHAR = "CHAR";
    public static final String VARCHAR = "VARCHAR";
    public static final String LONGVARCHAR = "LONGVARCHAR";
    public static final String CLOB = "CLOB";
    public static final String NUMERIC = "NUMERIC";
    public static final String DECIMAL = "DECIMAL";
    public static final String BIT = "BIT";
    public static final String TINYINT = "TINYINT";
    public static final String SMALLINT = "SMALLINT";
    public static final String INTEGER = "INTEGER";
    public static final String BIGINT = "BIGINT";
    public static final String REAL = "REAL";
    public static final String FLOAT = "FLOAT";
    public static final String DOUBLE = "DOUBLE";
    public static final String BINARY = "BINARY";
    public static final String VARBINARY = "VARBINARY";
    public static final String LONGVARBINARY = "LONGVARBINARY";
    public static final String BLOB = "BLOB";
    public static final String DATE = "DATE";
    public static final String TIME = "TIME";
    public static final String TIMESTAMP = "TIMESTAMP";
    public static final String BOOLEANCHAR = "BOOLEANCHAR";
    public static final String BOOLEANINT = "BOOLEANINT";
    
    private static final String[] TEXT_TYPES =
    {
        CHAR, VARCHAR, LONGVARCHAR, CLOB, DATE, TIME, TIMESTAMP, BOOLEANCHAR
    };

    private static Hashtable sqlTypeNameToTypeID = new Hashtable();
    private static Hashtable sqlTypeIDToTypeName = new Hashtable();



    static {

        /*
         * Map the Strings to JDBC type IDs
         */
        registerSqlTypeID(new Integer(Types.CHAR), CHAR);
        registerSqlTypeID(new Integer(Types.VARCHAR), VARCHAR);
        registerSqlTypeID(new Integer(Types.LONGVARCHAR), LONGVARCHAR);
        registerSqlTypeID(new Integer(Types.CLOB), CLOB);
        registerSqlTypeID(new Integer(Types.NUMERIC), NUMERIC);
        registerSqlTypeID(new Integer(Types.DECIMAL), DECIMAL);
        registerSqlTypeID(new Integer(Types.BIT), BIT);
        registerSqlTypeID(new Integer(Types.TINYINT), TINYINT);
        registerSqlTypeID(new Integer(Types.SMALLINT), SMALLINT);
        registerSqlTypeID(new Integer(Types.INTEGER), INTEGER);
        registerSqlTypeID(new Integer(Types.BIGINT), BIGINT);
        registerSqlTypeID(new Integer(Types.REAL), REAL);
        registerSqlTypeID(new Integer(Types.FLOAT), FLOAT);
        registerSqlTypeID(new Integer(Types.DOUBLE), DOUBLE);
        registerSqlTypeID(new Integer(Types.BINARY), BINARY);
        registerSqlTypeID(new Integer(Types.VARBINARY), VARBINARY);
        registerSqlTypeID(new Integer(Types.LONGVARBINARY), LONGVARBINARY);
        registerSqlTypeID(new Integer(Types.BLOB), BLOB);
        registerSqlTypeID(new Integer(Types.DATE), DATE);
        registerSqlTypeID(new Integer(Types.TIME), TIME);
        registerSqlTypeID(new Integer(Types.TIMESTAMP), TIMESTAMP);
    }

    /**
     * Returns the JDBC type name which maps to {@link java.sql.Types}
     * for the given SQL name of type
     */
    public static int getSQLTypeCode(String typeName)
    {
        Integer answer = (Integer) sqlTypeNameToTypeID.get(typeName.toUpperCase());
        if ( answer != null ) 
        {
            return answer.intValue();
        }
        return Types.OTHER;
    }

    public static String getSQLTypeString(Integer typeCode)
    {
        String answer = (String) sqlTypeIDToTypeName.get(typeCode);
        if ( answer != null )
        {
            return answer;
        }
        return "UNKNOWN";
    }
    
    /**
     * Returns true if values for the type need to be quoted.
     *
     * @param type The type to check.
     */
    public static final boolean isTextType(String type)
    {
        for (int i = 0; i < TEXT_TYPES.length; i++)
        {
            if (type.equalsIgnoreCase(TEXT_TYPES[i]))
            {
                return true;
            }
        }

        // If we get this far, there were no matches.
        return false;
    }


    /**
     * Registers the fact that the given Integer SQL ID maps to the given SQL name
     */
    protected static void registerSqlTypeID(Integer sqlTypeID, String name) 
    {
        sqlTypeNameToTypeID.put(name, sqlTypeID);
        sqlTypeIDToTypeName.put(sqlTypeID, name);
    }
}

