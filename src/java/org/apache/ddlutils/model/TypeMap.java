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
import java.util.Hashtable;

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

    private static final String[] TEXT_TYPES =
    {
        CHAR, VARCHAR, LONGVARCHAR, CLOB
    };
    private static final String[] BINARY_TYPES =
    {
        BINARY, VARBINARY, LONGVARBINARY, BLOB
    };
    private static final String[] DECIMAL_TYPES =
    {
        NUMERIC, DECIMAL
    };

    private static Hashtable sqlTypeNameToTypeID = new Hashtable();
    private static Hashtable typeIdToSqlTypeName = new Hashtable();



    static
    {
        registerSqlTypeID(Types.ARRAY,         ARRAY);
        registerSqlTypeID(Types.BIGINT,        BIGINT);
        registerSqlTypeID(Types.BINARY,        BINARY);
        registerSqlTypeID(Types.BIT,           BIT);
        registerSqlTypeID(Types.BLOB,          BLOB);
        registerSqlTypeID(Types.CHAR,          CHAR);
        registerSqlTypeID(Types.CLOB,          CLOB);
        registerSqlTypeID(Types.DATE,          DATE);
        registerSqlTypeID(Types.DECIMAL,       DECIMAL);
        registerSqlTypeID(Types.DISTINCT,      DISTINCT);
        registerSqlTypeID(Types.DOUBLE,        DOUBLE);
        registerSqlTypeID(Types.FLOAT,         FLOAT);
        registerSqlTypeID(Types.INTEGER,       INTEGER);
        registerSqlTypeID(Types.JAVA_OBJECT,   JAVA_OBJECT);
        registerSqlTypeID(Types.LONGVARBINARY, LONGVARBINARY);
        registerSqlTypeID(Types.LONGVARCHAR,   LONGVARCHAR);
        registerSqlTypeID(Types.NULL,          NULL);
        registerSqlTypeID(Types.NUMERIC,       NUMERIC);
        registerSqlTypeID(Types.OTHER,         OTHER);
        registerSqlTypeID(Types.REAL,          REAL);
        registerSqlTypeID(Types.REF,           REF);
        registerSqlTypeID(Types.SMALLINT,      SMALLINT);
        registerSqlTypeID(Types.STRUCT,        STRUCT);
        registerSqlTypeID(Types.TIME,          TIME);
        registerSqlTypeID(Types.TIMESTAMP,     TIMESTAMP);
        registerSqlTypeID(Types.TINYINT,       TINYINT);
        registerSqlTypeID(Types.VARBINARY,     VARBINARY);
        registerSqlTypeID(Types.VARCHAR,       VARCHAR);

        // only available in JDK 1.4 and above:
        if (Jdbc3Utils.supportsJava14JdbcTypes())
        {
            registerSqlTypeID(Jdbc3Utils.determineBooleanTypeCode(),  BOOLEAN);
            registerSqlTypeID(Jdbc3Utils.determineDatalinkTypeCode(), DATALINK);
        }
    }

    
    /**
     * Returns the JDBC type name which maps to {@link java.sql.Types}
     * for the given SQL name of type
     */
    public static int getJdbcTypeCode(String typeName)
    {
        Integer answer = (Integer)sqlTypeNameToTypeID.get(typeName.toUpperCase());

        return answer != null ? answer.intValue() : Types.OTHER;
    }

    /**
     * Returns the name which maps to the given {@link java.sql.Types} 
     * type code 
     */
    public static String getJdbcTypeName(int typeCode)
    {
        String answer = (String)typeIdToSqlTypeName.get(new Integer(typeCode));

        return answer == null ? OTHER : answer;
    }

    /**
     * Determines whether the indicated type is a textual type.
     *
     * @param type The code of type to check (as defined by {@link java.sql.Types}
     */
    public static final boolean isTextType(int type)
    {
        return isTextType(getJdbcTypeName(type));
    }

    /**
     * Determines whether the indicated type is a textual type.
     *
     * @param type The type to check
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
     * Determines whether the indicated type is a binary type.
     *
     * @param type The code of type to check (as defined by {@link java.sql.Types}
     */
    public static final boolean isBinaryType(int type)
    {
        return isBinaryType(getJdbcTypeName(type));
    }

    /**
     * Determines whether the indicated type is a binary type.
     *
     * @param type The type to check
     */
    public static final boolean isBinaryType(String type)
    {
        for (int i = 0; i < BINARY_TYPES.length; i++)
        {
            if (type.equalsIgnoreCase(BINARY_TYPES[i]))
            {
                return true;
            }
        }

        // If we get this far, there were no matches.
        return false;
    }

    /**
     * Returns true if values for the type need have size and scale measurements
     *
     * @param type The type to check.
     */
    public static final boolean typeHasScaleAndPrecision(int type)
    {
        return typeHasScaleAndPrecision(getJdbcTypeName(type));
    }

    /**
     * Returns true if values for the type need have size and scale measurements
     *
     * @param type The type to check.
     */
    public static final boolean typeHasScaleAndPrecision(String type)
    {
        for (int i = 0; i < DECIMAL_TYPES.length; i++)
        {
            if (type.equalsIgnoreCase(DECIMAL_TYPES[i]))
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
    protected static void registerSqlTypeID(int sqlTypeID, String name) 
    {
        Integer typeId = new Integer(sqlTypeID);

        sqlTypeNameToTypeID.put(name, typeId);
        typeIdToSqlTypeName.put(typeId, name);
    }
}

