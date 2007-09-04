package org.apache.ddlutils.model;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.ValuedEnum;

/**
 * Represents the different cascade actions for {@link ForeignKey#onDelete} and
 * {@link ForeignKey#onUdate}.
 * 
 * @version $Revision: $
 */
public class CascadeActionEnum extends ValuedEnum
{
    /** The integer value for the enum value for a cascading change. */
    public static final int VALUE_CASCADE  = 1;
    /** The integer value for the enum value for a set-null change. */
    public static final int VALUE_SETNULL  = 2;
    /** The integer value for the enum value for a restrict change. */
    public static final int VALUE_RESTRICT = 3;
    /** The integer value for the enum value for no-change. */
    public static final int VALUE_NONE     = 4;

    /** The enum value for a cascade action which directs the database to change the value
        of local column to the new value of the referenced column when it changes. */
    public static final CascadeActionEnum CASCADE  = new CascadeActionEnum("cascade",  VALUE_CASCADE);
    /** The enum value for a cascade action which directs the database to set the local
        column to null when the referenced column changes. */
    public static final CascadeActionEnum SETNULL  = new CascadeActionEnum("setnull",  VALUE_SETNULL);
    /** The enum value for a cascade action which directs the database to restrict the change
        changes to the referenced column. The interpretation of this is database-dependent. */
    public static final CascadeActionEnum RESTRICT = new CascadeActionEnum("restrict", VALUE_RESTRICT);
    /** The enum value for a cascade action which directs the database to take do nothing
        to the local column when the value of the referenced column changes. */
    public static final CascadeActionEnum NONE     = new CascadeActionEnum("none",     VALUE_NONE);

    /** Version id for this class as relevant for serialization. */
    private static final long serialVersionUID = -6378050861446415790L;

    /**
     * Creates a new enum object.
     * 
     * @param defaultTextRep The textual representation
     * @param value          The corresponding integer value
     */
    private CascadeActionEnum(String defaultTextRep, int value)
    {
        super(defaultTextRep, value);
    }

    /**
     * Returns the enum value that corresponds to the given textual
     * representation.
     * 
     * @param defaultTextRep The textual representation
     * @return The enum value
     */
    public static CascadeActionEnum getEnum(String defaultTextRep)
    {
        return (CascadeActionEnum)getEnum(CascadeActionEnum.class, defaultTextRep);
    }
    
    /**
     * Returns the enum value that corresponds to the given integer
     * representation.
     * 
     * @param intValue The integer value
     * @return The enum value
     */
    public static CascadeActionEnum getEnum(int intValue)
    {
        return (CascadeActionEnum)getEnum(CascadeActionEnum.class, intValue);
    }

    /**
     * Returns the map of enum values.
     * 
     * @return The map of enum values
     */
    public static Map getEnumMap()
    {
        return getEnumMap(CascadeActionEnum.class);
    }

    /**
     * Returns a list of all enum values.
     * 
     * @return The list of enum values
     */
    public static List getEnumList()
    {
        return getEnumList(CascadeActionEnum.class);
    }

    /**
     * Returns an iterator of all enum values.
     * 
     * @return The iterator
     */
    public static Iterator iterator()
    {
        return iterator(CascadeActionEnum.class);
    }
}
