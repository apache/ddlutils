package org.apache.commons.sql.task;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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
 */

import java.util.Hashtable;
import java.sql.Types;

// I don't know if the peer system deals
// with the recommended mappings.
//
//import java.sql.Date;
//import java.sql.Time;
//import java.sql.Timestamp;

/**
 * A class that maps JDBC types to their corresponding
 * Java object types, and Java native types. Used
 * by Column.java to perform object/native mappings.
 *
 * These are the official SQL type to Java type mappings.
 * These don't quite correspond to the way the peer
 * system works so we'll have to make some adjustments.
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
 * @version $Id: TypeMap.java,v 1.5 2002/04/11 22:02:06 mpoeschl Exp $
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

    private static Hashtable torqueTypeToJdbcTypeMap = null;
    private static Hashtable jdbcToTorqueTypeMap = null;
    private static boolean isInitialized = false;

    /**
     * Initializes the SQL to Java map so that it
     * can be used by client code.
     */
    public synchronized static void initialize()
    {
        if (isInitialized == false)
        {
            /*
             * Create JDBC -> Java object mappings.
             */

            torqueTypeToJdbcTypeMap = new Hashtable();

            torqueTypeToJdbcTypeMap.put(CHAR, CHAR);
            torqueTypeToJdbcTypeMap.put(VARCHAR, VARCHAR);
            torqueTypeToJdbcTypeMap.put(LONGVARCHAR, LONGVARCHAR);
            torqueTypeToJdbcTypeMap.put(CLOB, CLOB);
            torqueTypeToJdbcTypeMap.put(NUMERIC, NUMERIC);
            torqueTypeToJdbcTypeMap.put(DECIMAL, DECIMAL);
            torqueTypeToJdbcTypeMap.put(BIT, BIT);
            torqueTypeToJdbcTypeMap.put(TINYINT, TINYINT);
            torqueTypeToJdbcTypeMap.put(SMALLINT, SMALLINT);
            torqueTypeToJdbcTypeMap.put(INTEGER, INTEGER);
            torqueTypeToJdbcTypeMap.put(BIGINT, BIGINT);
            torqueTypeToJdbcTypeMap.put(REAL, REAL);
            torqueTypeToJdbcTypeMap.put(FLOAT, FLOAT);
            torqueTypeToJdbcTypeMap.put(DOUBLE, DOUBLE);
            torqueTypeToJdbcTypeMap.put(BINARY, BINARY);
            torqueTypeToJdbcTypeMap.put(VARBINARY, VARBINARY);
            torqueTypeToJdbcTypeMap.put(LONGVARBINARY, LONGVARBINARY);
            torqueTypeToJdbcTypeMap.put(BLOB, BLOB);
            torqueTypeToJdbcTypeMap.put(DATE, DATE);
            torqueTypeToJdbcTypeMap.put(TIME, TIME);
            torqueTypeToJdbcTypeMap.put(TIMESTAMP, TIMESTAMP);

            /*
             * Create JDBC type code to torque type map.
             */
            jdbcToTorqueTypeMap = new Hashtable();

            jdbcToTorqueTypeMap.put(new Integer(Types.CHAR), CHAR);
            jdbcToTorqueTypeMap.put(new Integer(Types.VARCHAR), VARCHAR);
            jdbcToTorqueTypeMap.put(new Integer(Types.LONGVARCHAR), LONGVARCHAR);
            jdbcToTorqueTypeMap.put(new Integer(Types.CLOB), CLOB);
            jdbcToTorqueTypeMap.put(new Integer(Types.NUMERIC), NUMERIC);
            jdbcToTorqueTypeMap.put(new Integer(Types.DECIMAL), DECIMAL);
            jdbcToTorqueTypeMap.put(new Integer(Types.BIT), BIT);
            jdbcToTorqueTypeMap.put(new Integer(Types.TINYINT), TINYINT);
            jdbcToTorqueTypeMap.put(new Integer(Types.SMALLINT), SMALLINT);
            jdbcToTorqueTypeMap.put(new Integer(Types.INTEGER), INTEGER);
            jdbcToTorqueTypeMap.put(new Integer(Types.BIGINT), BIGINT);
            jdbcToTorqueTypeMap.put(new Integer(Types.REAL), REAL);
            jdbcToTorqueTypeMap.put(new Integer(Types.FLOAT), FLOAT);
            jdbcToTorqueTypeMap.put(new Integer(Types.DOUBLE), DOUBLE);
            jdbcToTorqueTypeMap.put(new Integer(Types.BINARY), BINARY);
            jdbcToTorqueTypeMap.put(new Integer(Types.VARBINARY), VARBINARY);
            jdbcToTorqueTypeMap.put(new Integer(Types.LONGVARBINARY), LONGVARBINARY);
            jdbcToTorqueTypeMap.put(new Integer(Types.BLOB), BLOB);
            jdbcToTorqueTypeMap.put(new Integer(Types.DATE), DATE);
            jdbcToTorqueTypeMap.put(new Integer(Types.TIME), TIME);
            jdbcToTorqueTypeMap.put(new Integer(Types.TIMESTAMP), TIMESTAMP);

            isInitialized = true;
        }
    }

    /**
     * Report whether this object has been initialized.
     */
    public static boolean isInitialized()
    {
        return isInitialized;
    }

    /**
     * Returns the correct jdbc type for torque added types
     */
    public static String getJdbcType(String type)
    {
        // Make sure the we are initialized.
        if (isInitialized == false)
        {
            initialize();
        }
        return (String) torqueTypeToJdbcTypeMap.get(type);
    }

    /**
     * Returns Torque type constant corresponding to JDBC type code.
     * Used but Torque JDBC task.
     */
    public static String getTorqueType(Integer sqlType)
    {
        // Make sure the we are initialized.
        if (isInitialized == false)
        {
            initialize();
        }
        return (String) jdbcToTorqueTypeMap.get(sqlType);
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
            if (type.equals(TEXT_TYPES[i]))
            {
                return true;
            }
        }

        // If we get this far, there were no matches.
        return false;
    }
}
