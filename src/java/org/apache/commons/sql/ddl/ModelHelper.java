package org.apache.commons.sql.ddl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.ForeignKey;
import org.apache.commons.sql.model.Reference;
import org.apache.commons.sql.model.Table;


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

