package org.apache.ddlutils.type;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.model.Column;


/**
 * Manages the set of types supported by a database provider, 
 * and the mappings from standard JDBC types to the provider types.
 * 
 * @version     1.1 2003/02/05 08:08:37
 * @author      <a href="mailto:tima@intalio.com">Tim Anderson</a>
 */
public class Types {

    /**
     * The set of Type instances
     */
    private ListOrderedMap types = new ListOrderedMap();

    /**
     * A list of all mappings, in the order they were registered
     */
    private List mappingList = new ArrayList();

    /**
     * A map of JDBC type names to a <code>List</code> of corresponding 
     * database types. A list is required as some vendors alias their types
     */
    private HashMap mappings = new HashMap();

    /**
     * A list of all auto-increment mappings, in the order they were registered
     */
    private List autoIncList = new ArrayList();

    /**
     * A map of JDBC type names to a <code>List</code> of corresponding 
     * database types, which may be used as auto-increment types. 
     * A list is required as some vendors alias their types
     */
    private HashMap autoIncMappings = new HashMap();

    /**
     * The logger
     */
    private static final Log log = LogFactory.getLog(Types.class);


    /**
     * Construct a new empty <code>Types</code>
     */
    public Types() {
    }

    /**
     * Returns all types
     */
    public List getTypes() {
        return new ArrayList(types.values());
    }

    /**
     * Returns a type, based on its name
     *
     * @param sqlName the name of the type
     * @return the type corresponding to <code>sqlName</code>, or 
     * <code>null</code> if no such type exists
     */
    public Type getType(String sqlName) {
        return (Type) types.get(sqlName);
    }

    /**
     * Add a type. Types are uniquely identified by their 
     * {@link Type#getSQLName} - if a type already exists with the same
     * name, it will be replaced
     */
    public void addType(Type type) {
        if (log.isDebugEnabled()) {
            log.debug("Adding type: " + type);
        }
        types.put(type.getSQLName(), type);
    }

    /**
     * Returns all mappings that the database supports
     */
    public List getMappings() {
        return mappingList;
    }

    /**
     * Add a mapping. Mappings are identified by their {@link Mapping#getName}.
     * Multiple mappings can be added with the same name.
     *
     * @throws IllegalArgumentException if no there is no corresponding
     * {@link Type} registered for {@link Mapping#getSQLName}
     */
    public void addMapping(Mapping mapping) {
        if (log.isDebugEnabled()) {
            log.debug("Adding mapping: " + mapping);
        }
        if (getType(mapping.getSQLName()) == null) {
            throw new IllegalArgumentException(
                "No type registered for mapping: " + mapping);
        }
        mappingList.add(mapping);

        List list = (List) mappings.get(mapping.getName());
        if (list == null) {
            list = new ArrayList();
            mappings.put(mapping.getName(), list);
        }
        list.add(mapping);
    }

    /**
     * Returns all auto-increment mappings
     */
    public List getAutoIncrementMappings() {
        return autoIncList;
    }

    /**
     * Add an auto-increment mapping. Mappings are identified by their 
     * {@link Mapping#getName}. Multiple mappings can be added with the same 
     * name.
     *
     * @throws IllegalArgumentException if no there is no corresponding
     * {@link Type} registered for {@link Mapping#getSQLName}
     */
    public void addAutoIncrementMapping(Mapping mapping) {
        if (log.isDebugEnabled()) {
            log.debug("Adding auto-increment mapping: " + mapping);
        }
        if (getType(mapping.getSQLName()) == null) {
            throw new IllegalArgumentException(
                "No type registered for mapping: " + mapping);
        }

        autoIncList.add(mapping);

        List list = (List) autoIncMappings.get(mapping.getName());
        if (list == null) {
            list = new ArrayList();
            autoIncMappings.put(mapping.getName(), list);
        }
        list.add(mapping);
    }

    /**
     * Returns the first type matching the requested type name and size
     *
     * @param name the JDBC type name
     * @param size the requested size. A size of 0 indicates that the type 
     * has no size
     * @return the closest matching type, or <code>null</code> if none exists
     */
    public TypeMapping getTypeMapping(String name, long size) {
        TypeMapping result = null;

        List list = (List) mappings.get(name);
        if (list != null) {
            result = getTypeMapping(list, size);
        }
        if (log.isDebugEnabled()) {
            log.debug("getTypeMapping(" + name + ", " + size + ") => " + 
                      result);
                      
        }
        return result;
    }

    /**
     * Returns the closest auto-increment type mapping matching the requested
     * type name and size
     *
     * @param name the JDBC type name
     * @param size the requested size. A size &lt;= 0 indicates that the type 
     * has no size
     * @return the closest matching mapping, or <code>null</code> if none 
     * exists
     */
    public TypeMapping getAutoIncrementMapping(String name, long size) {
        TypeMapping result = null;

        List list = (List) autoIncMappings.get(name);
        if (list != null) {
            result = getTypeMapping(list, size);
        }
        if (log.isDebugEnabled()) {
            log.debug("getAutoIncrementMapping(" + name + ", " + size + 
                      ") => " + result);
                      
        }
        return result;
    }

    /**
     * Promote a type to that supported by the database provider.
     * Note that this only promotes types when the type semantics remain
     * unchanged - it will not promote a CHAR to a VARCHAR for example
     * as some providers do not support indexes on VARCHAR columns. If this
     * is the desired behaviour, add a mapping.
     *
     * @param name the JDBC type name
     * @param size the requested size
     * @return the promoted type mapping, or <code>null</code> if the type
     * can't be promoted
     */
    public TypeMapping promote(String name, long size) {
        TypeMapping result = null;
        if (TypeMap.isVarChar(name)) {
            result = promote(name, size, TypeMap.VARCHARS);
        } else if (TypeMap.isExactNumeric(name)) {
            result = promote(name, size, TypeMap.EXACT_NUMERICS, 
                             TypeMap.NUMERIC);
        } else if (TypeMap.isApproxNumeric(name)) {
            result = promote(name, size, TypeMap.APPROX_NUMERICS);
        }
        return result;
    }

    /**
     * Promote a type to another type
     *
     * @param fromName the JDBC type name to promote
     * @param toName the JDBC type name to promote to
     * @param size the requested size
     * @return the promoted type mapping, or <code>null</code> if the 
     * promotion is invalid, or isn't supported by the database provider
     */
    public TypeMapping promote(String fromName, String toName, long size) {
        TypeMapping result = null;
        boolean valid = false;
        if (TypeMap.isVarChar(fromName) && TypeMap.isVarChar(toName)) {
            valid = true;
        } else if (TypeMap.isExactNumeric(fromName) && 
                   TypeMap.isExactNumeric(toName)) {
            valid = true;
        } else if (TypeMap.isApproxNumeric(fromName) && 
                   TypeMap.isApproxNumeric(toName)) {
            valid = true;
        }
        if (valid) {
            result = getTypeMapping(toName, size);
        }
        return result;
    }

    /**
     * Helper to return the SQL type for a column
     *
     * @param column the column 
     * @return the SQL type of <code>column</code>
     */
    public String getSQLType(Column column) {
        String result = null;
        TypeMapping mapping = getTypeMapping(column.getType(), 
                                             column.getSizeAsInt());
        if (mapping != null) {
            result = mapping.getSQLType(column);
        }
        return result;
    }

    /**
     * Returns the closest type mapping for a requested size, from a list
     * of <code>Mapping</code>s.
     * Note that the returned mapping may specify a size less than that 
     * requested.
     *
     * @param mappings a list of <code>Mappings</code>
     * @param size the requested size
     * @return the closest type mapping
     */
    protected TypeMapping getTypeMapping(List mappings, long size) {
        TypeMapping result = null;
        Iterator iterator = mappings.iterator();
        Mapping mapping = null;
        Type type = null;
        while (iterator.hasNext()) {
            mapping = (Mapping) iterator.next();
            type = getType(mapping.getSQLName());
            if (type == null) {
                throw new IllegalStateException(
                    "Invalid mapping " + mapping + 
                    ". Type " + mapping.getSQLName() + " does not exist");
            }
            if (size == type.getSize()) {
                // exact match
                result = new TypeMapping(type, mapping);
                break;
            } else if (result == null) {
                result = new TypeMapping(type, mapping);
            } else if (size < type.getSize()) {
                // closer match
                if (type.getSize() < result.getSize()) {
                    result.setType(type);
                }
            } else {
                // size exceeded, but closer
                if (type.getSize() > result.getSize()) {
                    result.setType(type);
                }
            }
        } 
        return result;
    } 

    /**
     * Promote a type to that supported by the database provider.
     *
     * @param name the JDBC type name
     * @param size the requested size
     * @param types the set of JDBC types that <code>name</code> may be
     * promoted to
     * @return the promoted type mapping, or <code>null</code> if the type
     * can't be promoted
     */
    protected TypeMapping promote(String name, long size, String[] types) {
        return promote(name, size, types, null);
    }

    /**
     * Promote a type to that supported by the database provider.
     *
     * @param name the JDBC type name
     * @param size the requested size
     * @param types the set of JDBC types that <code>name</code> may be
     * promoted to
     * @param defaultName the default type name to promote to. If non-null,
     * this is used in preference to those listed by <code>types</code>
     * @return the promoted type mapping, or <code>null</code> if the type
     * can't be promoted
     */
    protected TypeMapping promote(String name, long size, String[] types, 
                                  String defaultName) {
        TypeMapping result = null;
        int index = -1;
        for (int i = 0; i < types.length; ++i) {
            if (types[i].equals(name)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            for (int i = index + 1; i < types.length && result == null; ++i) {
                if (defaultName != null) {
                    result = getTypeMapping(defaultName, size);
                    if (result != null) {
                        break;
                    }
                }
                result = getTypeMapping(types[i], size);
            }
        }
        return result;
    }

}
