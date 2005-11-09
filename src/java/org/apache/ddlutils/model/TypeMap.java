package org.apache.ddlutils.model;

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

import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.ddlutils.util.Jdbc3Utils;

/**
 * A class that maps SQL type names to their JDBC type ID found in
 * {@link java.sql.Types} and vice versa.
 *
 * @author Jason van Zyl
 * @author James Strachan
 * @author Thomas Dudziak
 * @version $Revision$
 */
public abstract class TypeMap
{
    /** The string representation of the {@link java.sql.Types#ARRAY} constant. */
    public static final String ARRAY         = "ARRAY";
    /** The string representation of the {@link java.sql.Types#BIGINT} constant. */
    public static final String BIGINT        = "BIGINT";
    /** The string representation of the {@link java.sql.Types#BINARY} constant. */
    public static final String BINARY        = "BINARY";
    /** The string representation of the {@link java.sql.Types#BIT} constant. */
    public static final String BIT           = "BIT";
    /** The string representation of the {@link java.sql.Types#BLOB} constant. */
    public static final String BLOB          = "BLOB";
    /** The string representation of the {@link java.sql.Types#BOOLEAN} constant. */
    public static final String BOOLEAN       = "BOOLEAN";
    /** The string representation of the {@link java.sql.Types#CHAR} constant. */
    public static final String CHAR          = "CHAR";
    /** The string representation of the {@link java.sql.Types#CLOB} constant. */
    public static final String CLOB          = "CLOB";
    /** The string representation of the {@link java.sql.Types#DATALINK} constant. */
    public static final String DATALINK      = "DATALINK";
    /** The string representation of the {@link java.sql.Types#DATE} constant. */
    public static final String DATE          = "DATE";
    /** The string representation of the {@link java.sql.Types#DECIMAL} constant. */
    public static final String DECIMAL       = "DECIMAL";
    /** The string representation of the {@link java.sql.Types#DISTINCT} constant. */
    public static final String DISTINCT      = "DISTINCT";
    /** The string representation of the {@link java.sql.Types#DOUBLE} constant. */
    public static final String DOUBLE        = "DOUBLE";
    /** The string representation of the {@link java.sql.Types#FLOAT} constant. */
    public static final String FLOAT         = "FLOAT";
    /** The string representation of the {@link java.sql.Types#INTEGER} constant. */
    public static final String INTEGER       = "INTEGER";
    /** The string representation of the {@link java.sql.Types#JAVA_OBJECT} constant. */
    public static final String JAVA_OBJECT   = "JAVA_OBJECT";
    /** The string representation of the {@link java.sql.Types#LONGVARBINARY} constant. */
    public static final String LONGVARBINARY = "LONGVARBINARY";
    /** The string representation of the {@link java.sql.Types#LONGVARCHAR} constant. */
    public static final String LONGVARCHAR   = "LONGVARCHAR";
    /** The string representation of the {@link java.sql.Types#NULL} constant. */
    public static final String NULL          = "NULL";
    /** The string representation of the {@link java.sql.Types#NUMERIC} constant. */
    public static final String NUMERIC       = "NUMERIC";
    /** The string representation of the {@link java.sql.Types#OTHER} constant. */
    public static final String OTHER         = "OTHER";
    /** The string representation of the {@link java.sql.Types#REAL} constant. */
    public static final String REAL          = "REAL";
    /** The string representation of the {@link java.sql.Types#REF} constant. */
    public static final String REF           = "REF";
    /** The string representation of the {@link java.sql.Types#SMALLINT} constant. */
    public static final String SMALLINT      = "SMALLINT";
    /** The string representation of the {@link java.sql.Types#STRUCT} constant. */
    public static final String STRUCT        = "STRUCT";
    /** The string representation of the {@link java.sql.Types#TIME} constant. */
    public static final String TIME          = "TIME";
    /** The string representation of the {@link java.sql.Types#TIMESTAMP} constant. */
    public static final String TIMESTAMP     = "TIMESTAMP";
    /** The string representation of the {@link java.sql.Types#TINYINT} constant. */
    public static final String TINYINT       = "TINYINT";
    /** The string representation of the {@link java.sql.Types#VARBINARY} constant. */
    public static final String VARBINARY     = "VARBINARY";
    /** The string representation of the {@link java.sql.Types#VARCHAR} constant. */
    public static final String VARCHAR       = "VARCHAR";

    /** Maps type names to the corresponding {@link java.sql.Types} constants. */
    private static HashMap _typeNameToTypeCode = new HashMap();
    /** Maps {@link java.sql.Types} type code constants to the corresponding type names. */
    private static HashMap _typeCodeToTypeName = new HashMap();
    /** Contains the type codes of the numeric types. */
    private static HashSet _numericTypes        = new HashSet();
    /** Contains the type codes of the text types. */
    private static HashSet _textTypes           = new HashSet();
    /** Contains the type codes of the binary types. */
    private static HashSet _binaryTypes         = new HashSet();
    /** Contains the type codes of the special types (eg. OTHER, REF etc.). */
    private static HashSet _specialTypes        = new HashSet();

    static
    {
        registerJdbcType(Types.ARRAY,         ARRAY,         false, false, false, true);
        registerJdbcType(Types.BIGINT,        BIGINT,        true,  false, false, false);
        registerJdbcType(Types.BINARY,        BINARY,        false, false, true,  false);
        registerJdbcType(Types.BIT,           BIT,           true,  false, false, false);
        registerJdbcType(Types.BLOB,          BLOB,          false, false, true,  false);
        registerJdbcType(Types.CHAR,          CHAR,          false, true,  false, false);
        registerJdbcType(Types.CLOB,          CLOB,          false, true,  false, false);
        registerJdbcType(Types.DATE,          DATE,          false, false, false, false);
        registerJdbcType(Types.DECIMAL,       DECIMAL,       true,  false, false, false);
        registerJdbcType(Types.DISTINCT,      DISTINCT,      false, false, false, true);
        registerJdbcType(Types.DOUBLE,        DOUBLE,        true,  false, false, false);
        registerJdbcType(Types.FLOAT,         FLOAT,         true,  false, false, false);
        registerJdbcType(Types.INTEGER,       INTEGER,       true,  false, false, false);
        registerJdbcType(Types.JAVA_OBJECT,   JAVA_OBJECT,   false, false, false, true);
        registerJdbcType(Types.LONGVARBINARY, LONGVARBINARY, false, false, true,  false);
        registerJdbcType(Types.LONGVARCHAR,   LONGVARCHAR,   false, true,  false, false);
        registerJdbcType(Types.NULL,          NULL,          false, false, false, true);
        registerJdbcType(Types.NUMERIC,       NUMERIC,       true,  false, false, false);
        registerJdbcType(Types.OTHER,         OTHER,         false, false, false, true);
        registerJdbcType(Types.REAL,          REAL,          true,  false, false, false);
        registerJdbcType(Types.REF,           REF,           false, false, false, true);
        registerJdbcType(Types.SMALLINT,      SMALLINT,      true,  false, false, false);
        registerJdbcType(Types.STRUCT,        STRUCT,        false, false, false, true);
        registerJdbcType(Types.TIME,          TIME,          false, false, false, false);
        registerJdbcType(Types.TIMESTAMP,     TIMESTAMP,     false, false, false, false);
        registerJdbcType(Types.TINYINT,       TINYINT,       true,  false, false, false);
        registerJdbcType(Types.VARBINARY,     VARBINARY,     false, false, true,  false);
        registerJdbcType(Types.VARCHAR,       VARCHAR,       false, true,  false, false);

        // only available in JDK 1.4 and above:
        if (Jdbc3Utils.supportsJava14JdbcTypes())
        {
            registerJdbcType(Jdbc3Utils.determineBooleanTypeCode(),  BOOLEAN,  true,  false, false, false);
            registerJdbcType(Jdbc3Utils.determineDatalinkTypeCode(), DATALINK, false, false, false, true);
        }

        // Torque/Turbine extensions which we only support when reading from an XML schema
        _typeNameToTypeCode.put("BOOLEANINT",  new Integer(Types.TINYINT));
        _typeNameToTypeCode.put("BOOLEANCHAR", new Integer(Types.CHAR));
    }

    /**
     * Returns the JDBC type code (one of the {@link java.sql.Types} constants) that
     * corresponds to the given JDBC type name.
     * 
     * @param typeName The JDBC type name (case is ignored)
     * @return The type code or <code>null</code> if the type is unknown
     */
    public static Integer getJdbcTypeCode(String typeName)
    {
        return (Integer)_typeNameToTypeCode.get(typeName.toUpperCase());
    }

    /**
     * Returns the JDBC type name that corresponds to the given type code
     * (one of the {@link java.sql.Types} constants).
     * 
     * @param typeCode The type code
     * @return The JDBC type name (one of the constants in this class) or
     *         <code>null</code> if the type is unknown
     */
    public static String getJdbcTypeName(int typeCode)
    {
        return (String)_typeCodeToTypeName.get(new Integer(typeCode));
    }

    /**
     * Registers a JDBC type.
     * 
     * @param typeCode      The type code (one of the {@link java.sql.Types} constants)
     * @param typeName      The type name (case is ignored)
     * @param isNumericType Whether the type is a numeric type
     * @param isTextType    Whether the type is a text type
     * @param isBinaryType  Whether the type is a binary type
     * @param isSpecialType Whether the type is a special type
     */
    protected static void registerJdbcType(int typeCode, String typeName, boolean isNumericType, boolean isTextType, boolean isBinaryType, boolean isSpecialType) 
    {
        Integer typeId = new Integer(typeCode);

        _typeNameToTypeCode.put(typeName.toUpperCase(), typeId);
        _typeCodeToTypeName.put(typeId, typeName.toUpperCase());
        if (isNumericType)
        {
            _numericTypes.add(typeId);
        }
        if (isTextType)
        {
            _textTypes.add(typeId);
        }
        if (isBinaryType)
        {
            _binaryTypes.add(typeId);
        }
        if (isSpecialType)
        {
            _specialTypes.add(typeId);
        }
    }

    /**
     * Determines whether the given sql type (one of the {@link java.sql.Types} constants)
     * is a numeric type.
     * 
     * @param sqlTypeID The type code
     * @return <code>true</code> if the type is a numeric one
     */
    public static boolean isNumericType(int sqlTypeID)
    {
        return _numericTypes.contains(new Integer(sqlTypeID));
    }

    /**
     * Determines whether the given sql type (one of the {@link java.sql.Types} constants)
     * is a text type.
     * 
     * @param sqlTypeID The type code
     * @return <code>true</code> if the type is a text one
     */
    public static boolean isTextType(int sqlTypeID)
    {
        return _textTypes.contains(new Integer(sqlTypeID));
    }

    /**
     * Determines whether the given sql type (one of the {@link java.sql.Types} constants)
     * is a binary type.
     * 
     * @param sqlTypeID The type code
     * @return <code>true</code> if the type is a binary one
     */
    public static boolean isBinaryType(int sqlTypeID)
    {
        return _binaryTypes.contains(new Integer(sqlTypeID));
    }

    /**
     * Determines whether the given sql type (one of the {@link java.sql.Types} constants)
     * is a special type.
     * 
     * @param sqlTypeID The type code
     * @return <code>true</code> if the type is a special one
     */
    public static boolean isSpecialType(int sqlTypeID)
    {
        return _specialTypes.contains(new Integer(sqlTypeID));
    }
}
