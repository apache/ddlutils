package org.apache.ddlutils.io;

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

import java.util.List;

import org.apache.commons.beanutils.DynaBean;
import org.apache.ddlutils.TestDatabaseWriterBase;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.UniqueIndex;

/**
 * Base class for database roundtrip (creation & reconstruction from the database).
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public abstract class RoundtripTestBase extends TestDatabaseWriterBase
{
    /** Test model with a simple BIT column. */
    protected static final String TEST_BIT_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='BIT'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a BIT column with a default value. */
    protected static final String TEST_BIT_MODEL_WITH_DEFAULT = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='BIT' required='true' default='FALSE'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple BOOLEAN column. */
    protected static final String TEST_BOOLEAN_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='BOOLEAN'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a BOOLEAN column with a default value. */
    protected static final String TEST_BOOLEAN_MODEL_WITH_DEFAULT = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='BOOLEAN' required='true' default='TRUE'/>\n"+
        "  </table>\n"+
        "</database>";

    /**
     * Inserts a row into the designated table.
     * 
     * @param tableName    The name of the table (case insensitive)
     * @param columnValues The values for the columns in order of definition
     */
    protected void insertRow(String tableName, Object[] columnValues)
    {
        Table    table = getModel().findTable(tableName);
        DynaBean bean  = getModel().createDynaBeanFor(table);

        for (int idx = 0; (idx < table.getColumnCount()) && (idx < columnValues.length); idx++)
        {
            Column column = table.getColumn(idx);

            bean.set(column.getName(), columnValues[idx]);
        }
        getPlatform().insert(getModel(), bean);
    }

    /**
     * Retrieves all rows from the given table.
     * 
     * @param tableName The table
     * @return The rows
     */
    protected List getRows(String tableName)
    {
        Table table = getModel().findTable(tableName);

        return getPlatform().fetch(getModel(),
                                   "SELECT * FROM " + tableName,
                                   new Table[] { table });
    }

    /**
     * Adds unique indices for the pks to the model (for comparison).
     */
    protected void addPrimaryKeyUniqueIndicesToModel()
    {
        for (int tableIdx = 0; tableIdx < getModel().getTableCount(); tableIdx++)
        {
            Table       table = getModel().getTable(tableIdx);
            UniqueIndex index = new UniqueIndex();
    
            for (int pkIdx = 0; pkIdx < table.getPrimaryKeyColumns().length; pkIdx++)
            {
                index.addColumn(new IndexColumn(table.getPrimaryKeyColumns()[pkIdx].getName()));
            }
            table.addIndex(index);
        }
    }
    
    /**
     * Compares the attribute value of the given bean to the expected object.
     * 
     * @param expected The expected object
     * @param bean     The bean
     * @param attrName The attribute name
     */
    protected void assertEquals(Object expected, Object bean, String attrName)
    {
        DynaBean dynaBean = (DynaBean)bean;

        assertEquals(expected,
                     dynaBean.get(attrName));
    }

    // boolean/bit columns
    // boolean with default value
    // simple bit
    // bit with default value
    
    // numerical columns
    // numeric/decimal incl. precision/scale
    // char/varchar columns incl. different sizes
    // time columns
    // binary/varbinary & java_object etc. columns
    // blob/clob columns

    // auto-increment
    // default values
    // null/not null
    // pk (incl. pk with auto-increment)
    // index/unique (incl. for pks)
    // fks (incl. multiple columns, circular references)
}
