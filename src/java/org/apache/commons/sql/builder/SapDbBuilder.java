package org.apache.commons.sql.builder;

/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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

import org.apache.commons.sql.model.Column;

/**
 * An SQL Builder for SapDB.
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision$
 */
public class SapDbBuilder extends SqlBuilder
{
    public static final String CHARACTER_TYPE_ASCII   = "ASCII";
    public static final String CHARACTER_TYPE_UNICODE = "UNICODE";

    /** The characater type */
    private String _characterType = "";

    public SapDbBuilder()
    {
        setCommentPrefix("/*");
        setCommentSuffix("*/");
        // CHAR and VARCHAR are handled by getSqlType
        addNativeTypeMapping(Types.BIGINT,        "FIXED(38,0)");
        addNativeTypeMapping(Types.BINARY,        "LONG BYTE");
        addNativeTypeMapping(Types.BLOB,          "LONG BYTE");
        addNativeTypeMapping(Types.BIT,           "FIXED(1,0)");
        addNativeTypeMapping(Types.DOUBLE,        "DOUBLE PRECISION");
        addNativeTypeMapping(Types.LONGVARBINARY, "LONG BYTE");
        addNativeTypeMapping(Types.TINYINT,       "SMALLINT");
        addNativeTypeMapping(Types.VARBINARY,     "LONG BYTE");
    }

    /* (non-Javadoc)
     * @see org.apache.commons.sql.builder.SqlBuilder#getDatabaseName()
     */
    public String getDatabaseName()
    {
        return "SapDB";
    }

    /**
     * Sets the character type of the database, either 'ASCII', 'UNICODE'.
     * 
     * @param characterType The character type
     */
    public void setCharacterType(String characterType)
    {
        if (characterType == null)
        {
            _characterType = "";
        }
        else if (CHARACTER_TYPE_ASCII.equalsIgnoreCase(characterType))
        {
            _characterType = CHARACTER_TYPE_ASCII;
        }
        else if (CHARACTER_TYPE_UNICODE.equalsIgnoreCase(characterType))
        {
            _characterType = CHARACTER_TYPE_UNICODE;
        }
        else
        {
            throw new IllegalArgumentException("Unknown character type "+characterType+", only "+
                                               CHARACTER_TYPE_ASCII+" and "+CHARACTER_TYPE_UNICODE+" or an empty string are allowed");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.commons.sql.builder.SqlBuilder#getSqlType(org.apache.commons.sql.model.Column)
     */
    protected String getSqlType(Column column)
    {
        switch (column.getTypeCode())
        {
            case Types.CHAR:
            case Types.VARCHAR:
                if (column.getSize() != null)
                {
                    StringBuffer sqlType = new StringBuffer(getNativeType(column));

                    sqlType.append(" (");
                    sqlType.append(column.getSize());
                    sqlType.append(") ");
                    sqlType.append(_characterType);
                    return sqlType.toString();
                }
                break;
            case Types.CLOB:
            case Types.LONGVARCHAR:
                return "LONG "+_characterType;
        }
        return super.getSqlType(column);
    }
}
