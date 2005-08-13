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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.DynaBean;
import org.apache.ddlutils.DynaSqlException;
import org.apache.ddlutils.dynabean.DynaClassCache;
import org.apache.ddlutils.dynabean.SqlDynaClass;

/**
 * Represents the database model, ie. the tables in the database. It also
 * contains the corresponding dyna classes for creating dyna beans for the
 * objects stored in the tables.
 *
 * @author John Marshall/Connectria
 * @author Matthew Hawthorne
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision$
 */
public class Database
{
    /** The name of the database model */
    private String _name;
    /** The method for generating primary keys (currently ignored) */
    private String _idMethod;
    /** The version of the model */
    private String _version;
    /** The tables */
    private List _tables = new ArrayList();

    /** The dyna class cache for this model */
    private DynaClassCache _dynaClassCache = new DynaClassCache();

    /**
     * Adds all tables from the other database to this database.
     * Note that the other database is not changed.
     * 
     * @param otherDb The other database model
     */
    public void mergeWith(Database otherDb) throws DynaSqlException
    {
        for (Iterator it = otherDb._tables.iterator(); it.hasNext();)
        {
            Table table = (Table)it.next();

            if (findTable(table.getName()) != null)
            {
                // TODO: It might make more sense to log a warning and overwrite the table (or merge them) ?
                throw new DynaSqlException("Cannot merge the models because table "+table.getName()+" already defined in this model");
            }
            try
            {
                addTable((Table)table.clone());
            }
            catch (CloneNotSupportedException ex)
            {
                // won't happen
            }
        }
    }

    /**
     * Returns the name of this database model.
     * 
     * @return The name
     */
    public String getName()
    {
        return _name;
    }

    /**
     * Sets the name of this database model.
     * 
     * @param name The name
     */
    public void setName(String name)
    {
        _name = name;
    }

    /**
     * Returns the version of this database model.
     * 
     * @return The version
     */
    public String getVersion()
    {
        return _version;
    }

    /**
     * Sets the version of this database model.
     * 
     * @param version The version
     */
    public void setVersion(String version)
    {
        _version = version;
    }

    /**
     * Returns the method for generating primary key values.
     * 
     * @return The method
     */
    public String getIdMethod()
    {
        return _idMethod;
    }

    /**
     * Sets the method for generating primary key values. Note that this
     * value is ignored by DdlUtils and only for compatibility with Torque.
     * 
     * @param idMethod The method
     */
    public void setIdMethod(String idMethod)
    {
        _idMethod = idMethod;
    }

    /**
     * Returns the list of tables in this model.
     * 
     * @return The tables
     */
    public List getTables()
    {
        return _tables;
    }

    /**
     * Returns the table at the specified position.
     * 
     * @param index The index of the table
     * @return The table
     */
    public Table getTable(int index)
    {
        return (Table)_tables.get(index);
    }

    /**
     * Adds a table.
     * 
     * @param table The table to add
     */
    public void addTable(Table table)
    {
        _tables.add(table);
    }

    /**
     * Replaces the table at the specified position.
     * 
     * @param index The index of the table
     * @param table The new table
     */
    public void setTable(int index, Table table)
    {
        _tables.set(index, table);
    }

    // Helper methods

    /**
     * Finds the table with the specified name, using case insensitive matching.
     * Note that this method is not called getTable(String) to avoid introspection
     * problems.
     * 
     * @param name The name of the table to find
     * @return The table or <code>null</code> if there is no such table
     */
    public Table findTable(String name)
    {
        return findTable(name, false);
    }

    /**
     * Finds the table with the specified name, using case insensitive matching.
     * Note that this method is not called getTable(String) to avoid introspection
     * problems.
     * 
     * @param name          The name of the table to find
     * @param caseSensitive Whether case matters for the names
     * @return The table or <code>null</code> if there is no such table
     */
    public Table findTable(String name, boolean caseSensitive)
    {
        for (Iterator iter = _tables.iterator(); iter.hasNext();)
        {
            Table table = (Table) iter.next();

            if (caseSensitive)
            {
                if (table.getName().equals(name))
                {
                    return table;
                }
            }
            else
            {
                if (table.getName().equalsIgnoreCase(name))
                {
                    return table;
                }
            }
        }
        return null;
    }

    /**
     * Returns the {@link org.apache.ddlutils.dynabean.SqlDynaClass} for the given table name. If the it does not
     * exist yet, a new one will be created based on the Table definition.
     * 
     * @return The <code>SqlDynaClass</code> for the indicated table or <code>null</code>
     *         if the model contains no such table
     */
    public SqlDynaClass getDynaClassFor(String tableName)
    {
        Table table = findTable(tableName);

        return table != null ? _dynaClassCache.getDynaClass(table) : null;
    }

    /**
     * Returns the {@link org.apache.ddlutils.dynabean.SqlDynaClass} for the given dyna bean.
     * 
     * @return The <code>SqlDynaClass</code> for the given bean
     */
    public SqlDynaClass getDynaClassFor(DynaBean bean)
    {
        return _dynaClassCache.getDynaClass(bean);
    }

    /**
     * Creates a new dyna bean for the given table.
     * 
     * @return The new dyna bean
     */
    public DynaBean createDynaBeanFor(Table table) throws DynaSqlException
    {
        return _dynaClassCache.createNewInstance(table);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return super.toString() + "[name=" + _name + ";tableCount=" + _tables.size() + "]";
    }
}
