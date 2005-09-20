package org.apache.ddlutils.model;

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
import java.util.HashMap;
import java.util.HashSet;

import org.apache.ddlutils.util.Jdbc3Utils;

/**
 * A class that maps SQL type names to their JDBC type ID found in
 * {@link java.sql.Types} and vice versa.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision$
 */
public class TypeMap
{
    public static final String ARRAY         = "ARRAY";
    public static final String BIGINT        = "BIGINT";
    public static final String BINARY        = "BINARY";
    public static final String BIT           = "BIT";
    public static final String BLOB          = "BLOB";
    public static final String BOOLEAN       = "BOOLEAN";
    public static final String CHAR          = "CHAR";
    public static final String CLOB          = "CLOB";
    public static final String DATALINK      = "DATALINK";
    public static final String DATE          = "DATE";
    public static final String DECIMAL       = "DECIMAL";
    public static final String DISTINCT      = "DISTINCT";
    public static final String DOUBLE        = "DOUBLE";
    public static final String FLOAT         = "FLOAT";
    public static final String INTEGER       = "INTEGER";
    public static final String JAVA_OBJECT   = "JAVA_OBJECT";
    public static final String LONGVARBINARY = "LONGVARBINARY";
    public static final String LONGVARCHAR   = "LONGVARCHAR";
    public static final String NULL          = "NULL";
    public static final String NUMERIC       = "NUMERIC";
    public static final String OTHER         = "OTHER";
    public static final String REAL          = "REAL";
    public static final String REF           = "REF";
    public static final String SMALLINT      = "SMALLINT";
    public static final String STRUCT        = "STRUCT";
    public static final String TIME          = "TIME";
    public static final String TIMESTAMP     = "TIMESTAMP";
    public static final String TINYINT       = "TINYINT";
    public static final String VARBINARY     = "VARBINARY";
    public static final String VARCHAR       = "VARCHAR";

    private static HashMap _sqlTypeNameToTypeID = new HashMap();
    private static HashMap _typeIdToSqlTypeName = new HashMap();
    private static HashSet _numericTypes        = new HashSet();
    private static HashSet _textTypes           = new HashSet();
    private static HashSet _binaryTypes         = new HashSet();
    private static HashSet _specialTypes        = new HashSet();

    static
    {
        registerSqlTypeID(Types.ARRAY,         ARRAY,         false, false, false, true);
        registerSqlTypeID(Types.BIGINT,        BIGINT,        true,  false, false, false);
        registerSqlTypeID(Types.BINARY,        BINARY,        false, false, true,  false);
        registerSqlTypeID(Types.BIT,           BIT,           true,  false, false, false);
        registerSqlTypeID(Types.BLOB,          BLOB,          false, false, true,  false);
        registerSqlTypeID(Types.CHAR,          CHAR,          false, true,  false, false);
        registerSqlTypeID(Types.CLOB,          CLOB,          false, true,  false, false);
        registerSqlTypeID(Types.DATE,          DATE,          false, false, false, false);
        registerSqlTypeID(Types.DECIMAL,       DECIMAL,       true,  false, false, false);
        registerSqlTypeID(Types.DISTINCT,      DISTINCT,      false, false, false, true);
        registerSqlTypeID(Types.DOUBLE,        DOUBLE,        true,  false, false, false);
        registerSqlTypeID(Types.FLOAT,         FLOAT,         true,  false, false, false);
        registerSqlTypeID(Types.INTEGER,       INTEGER,       true,  false, false, false);
        registerSqlTypeID(Types.JAVA_OBJECT,   JAVA_OBJECT,   false, false, false, true);
        registerSqlTypeID(Types.LONGVARBINARY, LONGVARBINARY, false, false, true,  false);
        registerSqlTypeID(Types.LONGVARCHAR,   LONGVARCHAR,   false, true,  false, false);
        registerSqlTypeID(Types.NULL,          NULL,          false, false, false, true);
        registerSqlTypeID(Types.NUMERIC,       NUMERIC,       true,  false, false, false);
        registerSqlTypeID(Types.OTHER,         OTHER,         false, false, false, true);
        registerSqlTypeID(Types.REAL,          REAL,          true,  false, false, false);
        registerSqlTypeID(Types.REF,           REF,           false, false, false, true);
        registerSqlTypeID(Types.SMALLINT,      SMALLINT,      true,  false, false, false);
        registerSqlTypeID(Types.STRUCT,        STRUCT,        false, false, false, true);
        registerSqlTypeID(Types.TIME,          TIME,          false, false, false, false);
        registerSqlTypeID(Types.TIMESTAMP,     TIMESTAMP,     false, false, false, false);
        registerSqlTypeID(Types.TINYINT,       TINYINT,       true,  false, false, false);
        registerSqlTypeID(Types.VARBINARY,     VARBINARY,     false, false, true,  false);
        registerSqlTypeID(Types.VARCHAR,       VARCHAR,       false, true,  false, false);

        // only available in JDK 1.4 and above:
        if (Jdbc3Utils.supportsJava14JdbcTypes())
        {
            registerSqlTypeID(Jdbc3Utils.determineBooleanTypeCode(),  BOOLEAN,  true,  false, false, false);
            registerSqlTypeID(Jdbc3Utils.determineDatalinkTypeCode(), DATALINK, false, false, false, true);
        }
    }

    
    /**
     * Returns the JDBC type name which maps to {@link java.sql.Types}
     * for the given SQL name of type
     */
    public static int getJdbcTypeCode(String typeName)
    {
        Integer answer = (Integer)_sqlTypeNameToTypeID.get(typeName.toUpperCase());

        return answer != null ? answer.intValue() : Types.OTHER;
    }

    /**
     * Returns the name which maps to the given {@link java.sql.Types} 
     * type code 
     */
    public static String getJdbcTypeName(int typeCode)
    {
        String answer = (String)_typeIdToSqlTypeName.get(new Integer(typeCode));

        return answer == null ? OTHER : answer;
    }

    /**
     * Registers the fact that the given Integer SQL ID maps to the given SQL name
     */
    protected static void registerSqlTypeID(int sqlTypeID, String name, boolean isNumericType, boolean isTextType, boolean isBinaryType, boolean isSpecialType) 
    {
        Integer typeId = new Integer(sqlTypeID);

        _sqlTypeNameToTypeID.put(name, typeId);
        _typeIdToSqlTypeName.put(typeId, name);
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

