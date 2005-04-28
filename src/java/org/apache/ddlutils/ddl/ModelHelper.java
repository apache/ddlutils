package org.apache.ddlutils.ddl;

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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;


/**
 * Helper routines for operating on a database model
 * 
 * @version     1.1 2003/02/05 08:08:36
 * @author      <a href="mailto:tima@intalio.com">Tim Anderson</a>
 */
public class ModelHelper {

    /**
     * Returns the named column from the table. Column names are case 
     * insensitive
     *
     * @param table the table
     * @param name the column name
     * @return the column corresponding to <code>name</code>, or null, if
     * no such column exists
     */
    public static Column getColumn(Table table, String name) {
        Column result = null;
        Iterator columns = table.getColumns().iterator();
        while (columns.hasNext()) {
            Column column = (Column) columns.next();
            if (column.getName().equalsIgnoreCase(name)) {
                result = column;
                break;
            }
        }
        return result;
    }

    /**
     * Returns a list of <code>Reference</code> from a table which 
     * reference a column in another
     *
     * @param referencer the referencing table
     * @param referenced the referenced table
     * @param name the referenced column name
     * @return a list of <code>Reference</code>s
     */
    public static List getReferences(Table referencer, Table referenced,
                                     String name) {
        List result = Collections.EMPTY_LIST;
        List foreignKeys = getReferences(referencer, referenced);
        if (!foreignKeys.isEmpty()) {
            result = getReferences(foreignKeys, name);
        }
        return result;
    }

    /**
     * Returns a list of <code>ForeignKey<code>s from a table which 
     * reference another
     *
     * @param referencer the referencing table
     * @param referenced the referenced table
     * @return a list of <code>ForeignKey</code>s
     */
    public static List getReferences(Table referencer, Table referenced) {
        List result = new ArrayList();
        Iterator keys = referencer.getForeignKeys().iterator();
        String name = referenced.getName();
        while (keys.hasNext()) {
            ForeignKey key = (ForeignKey) keys.next();
            if (key.getForeignTable().equalsIgnoreCase(name)) {
                result.add(key);
            }
        }
        return result;
    }

    /**
     * Returns a list of <code>Reference</code>s from a set of foreign keys
     * which reference a column
     *
     * @param foreignKeys a list of <code>ForeignKey</code>s
     * @param name the name of the foreign key column
     * @return a list of <code>Reference</code>s
     */
    public static List getReferences(List foreignKeys, String name) {
        List result = new ArrayList();
        Iterator keys = foreignKeys.iterator();
        while (keys.hasNext()) {
            ForeignKey key = (ForeignKey) keys.next();
            Iterator references = key.getReferences().iterator();
            while (references.hasNext()) {
                Reference reference = (Reference) references.next();
                if (reference.getForeign().equalsIgnoreCase(name)) {
                    result.add(reference);
                }
            }
        }
        return result;
    }

}

