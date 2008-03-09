package org.apache.ddlutils.io;

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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import junit.framework.TestSuite;

import org.apache.commons.beanutils.DynaBean;
import org.apache.ddlutils.DdlUtilsException;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.TestDatabaseWriterBase;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;

/**
 * Base class for database roundtrip (creation & reconstruction from the database).
 * 
 * @version $Revision: 289996 $
 */
public abstract class RoundtripTestBase extends TestDatabaseWriterBase
{
    /**
     * Creates the test suite for the given test class which must be a sub class of
     * {@link RoundtripTestBase}. If the platform supports it, it will be tested
     * with both delimited and undelimited identifiers.
     * 
     * @param testedClass The tested class
     * @return The tests
     */
    protected static TestSuite getTests(Class testedClass)
    {
        if (!RoundtripTestBase.class.isAssignableFrom(testedClass) ||
            Modifier.isAbstract(testedClass.getModifiers()))
        {
            throw new DdlUtilsException("Cannot create parameterized tests for class "+testedClass.getName());
        }

        TestSuite suite = new TestSuite();

        try
        {
            Method[]          methods = testedClass.getMethods();
            PlatformInfo      info    = null;
            RoundtripTestBase newTest;
    
            for (int idx = 0; (methods != null) && (idx < methods.length); idx++)
            {
                if (methods[idx].getName().startsWith("test") &&
                    ((methods[idx].getParameterTypes() == null) || (methods[idx].getParameterTypes().length == 0)))
                {
                    newTest = (RoundtripTestBase)testedClass.newInstance();
                    newTest.setName(methods[idx].getName());
                    newTest.setUseDelimitedIdentifiers(false);
                    suite.addTest(newTest);

                    if (info == null)
                    {
                        info = PlatformFactory.createNewPlatformInstance(newTest.getDatabaseName()).getPlatformInfo();
                    }
                    if (info.isDelimitedIdentifiersSupported())
                    {
                        newTest = (RoundtripTestBase)testedClass.newInstance();
                        newTest.setName(methods[idx].getName());
                        newTest.setUseDelimitedIdentifiers(true);
                        suite.addTest(newTest);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            throw new DdlUtilsException(ex);
        }
        
        return suite;
    }

    /** Whether to use delimited identifiers for the test. */
    private boolean _useDelimitedIdentifiers;
    
    /**
     * Specifies whether the test shall use delimited identifiers.
     * 
     * @param useDelimitedIdentifiers Whether to use delimited identifiers
     */
    protected void setUseDelimitedIdentifiers(boolean useDelimitedIdentifiers)
    {
        _useDelimitedIdentifiers = useDelimitedIdentifiers;
    }
    
    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        getPlatform().setDelimitedIdentifierModeOn(_useDelimitedIdentifiers);
    }

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
     * Updates the row in the designated table.
     * 
     * @param tableName    The name of the table (case insensitive)
     * @param oldBean      The bean representing the current row
     * @param columnValues The values for the columns in order of definition
     */
    protected void updateRow(String tableName, DynaBean oldBean, Object[] columnValues)
    {
        Table    table = getModel().findTable(tableName);
        DynaBean bean  = getModel().createDynaBeanFor(table);

        for (int idx = 0; (idx < table.getColumnCount()) && (idx < columnValues.length); idx++)
        {
            Column column = table.getColumn(idx);

            bean.set(column.getName(), columnValues[idx]);
        }
        getPlatform().update(getModel(), oldBean, bean);
    }

    /**
     * Deletes the specified row from the table.
     * 
     * @param tableName      The name of the table (case insensitive)
     * @param pkColumnValues The values for the pk columns in order of definition
     */
    protected void deleteRow(String tableName, Object[] pkColumnValues)
    {
        Table    table     = getModel().findTable(tableName);
        DynaBean bean      = getModel().createDynaBeanFor(table);
        Column[] pkColumns = table.getPrimaryKeyColumns();

        for (int idx = 0; (idx < pkColumns.length) && (idx < pkColumnValues.length); idx++)
        {
            bean.set(pkColumns[idx].getName(), pkColumnValues[idx]);
        }
        getPlatform().delete(getModel(), bean);
    }

    /**
     * Returns a "SELECT * FROM [table name]" statement. It also takes
     * delimited identifier mode into account if enabled.
     *  
     * @param table       The table
     * @param orderColumn The column to order the rows by (can be <code>null</code>)
     * @return The statement
     */
    protected String getSelectQueryForAllString(Table table, String orderColumn)
    {
        StringBuffer query = new StringBuffer();

        query.append("SELECT * FROM ");
        if (getPlatform().isDelimitedIdentifierModeOn())
        {
            query.append(getPlatformInfo().getDelimiterToken());
        }
        query.append(table.getName());
        if (getPlatform().isDelimitedIdentifierModeOn())
        {
            query.append(getPlatformInfo().getDelimiterToken());
        }
        if (orderColumn != null)
        {
            query.append(" ORDER BY ");
            if (getPlatform().isDelimitedIdentifierModeOn())
            {
                query.append(getPlatformInfo().getDelimiterToken());
            }
            query.append(orderColumn);
            if (getPlatform().isDelimitedIdentifierModeOn())
            {
                query.append(getPlatformInfo().getDelimiterToken());
            }
        }
        return query.toString();
    }

    /**
     * Retrieves all rows from the given table.
     * 
     * @param tableName The table
     * @return The rows
     */
    protected List getRows(String tableName)
    {
        Table table = getModel().findTable(tableName, getPlatform().isDelimitedIdentifierModeOn());
        
        return getPlatform().fetch(getModel(),
                                   getSelectQueryForAllString(table, null),
                                   new Table[] { table });
    }

    /**
     * Retrieves all rows from the given table.
     * 
     * @param tableName   The table
     * @param orderColumn The column to order the rows by
     * @return The rows
     */
    protected List getRows(String tableName, String orderColumn)
    {
        Table table = getModel().findTable(tableName, getPlatform().isDelimitedIdentifierModeOn());
        
        return getPlatform().fetch(getModel(),
                                   getSelectQueryForAllString(table, orderColumn),
                                   new Table[] { table });
    }

    /**
     * Asserts that the two given database models are equal, and if not, writes both of them
     * in XML form to <code>stderr</code>.
     * 
     * @param expected The expected model
     * @param actual   The actual model
     */
    protected void assertEquals(Database expected, Database actual)
    {
        assertEquals(expected, actual, _useDelimitedIdentifiers);
    }

    /**
     * Asserts that the two given database tables are equal.
     * 
     * @param expected The expected table
     * @param actual   The actual table
     */
    protected void assertEquals(Table expected, Table actual)
    {
        assertEquals(expected, actual, _useDelimitedIdentifiers);
    }

    /**
     * Asserts that the two given columns are equal.
     * 
     * @param expected The expected column
     * @param actual   The actual column
     */
    protected void assertEquals(Column expected, Column actual)
    {
        assertEquals(expected, actual, _useDelimitedIdentifiers);
    }

    /**
     * Asserts that the two given foreign keys are equal.
     * 
     * @param expected The expected foreign key
     * @param actual   The actual foreign key
     */
    protected void assertEquals(ForeignKey expected, ForeignKey actual)
    {
        assertEquals(expected, actual, _useDelimitedIdentifiers);
    }

    /**
     * Asserts that the two given references are equal.
     * 
     * @param expected The expected reference
     * @param actual   The actual reference
     */
    protected void assertEquals(Reference expected, Reference actual)
    {
        assertEquals(expected, actual, _useDelimitedIdentifiers);
    }

    /**
     * Asserts that the two given indices are equal.
     * 
     * @param expected The expected index
     * @param actual   The actual index
     */
    protected void assertEquals(Index expected, Index actual)
    {
        assertEquals(expected, actual, _useDelimitedIdentifiers);
    }

    /**
     * Asserts that the two given index columns are equal.
     * 
     * @param expected The expected index column
     * @param actual   The actual index column
     */
    protected void assertEquals(IndexColumn expected, IndexColumn actual)
    {
        assertEquals(expected, actual, _useDelimitedIdentifiers);
    }
}
