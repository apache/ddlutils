package org.apache.ddlutils.io;

/*
 * Copyright 1999-2006 The Apache Software Foundation.
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

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Types;
import java.util.List;

import junit.framework.TestSuite;

import org.apache.commons.beanutils.DynaBean;
import org.apache.ddlutils.DdlUtilsException;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.TestDatabaseWriterBase;
import org.apache.ddlutils.dynabean.SqlDynaBean;
import org.apache.ddlutils.dynabean.SqlDynaClass;
import org.apache.ddlutils.dynabean.SqlDynaProperty;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.TypeMap;
import org.apache.ddlutils.platform.DefaultValueHelper;

/**
 * Base class for database roundtrip (creation & reconstruction from the database).
 * 
 * @author Thomas Dudziak
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
                    if (info.isSupportingDelimitedIdentifiers())
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
     * Specifies whether the test shall use delimited identifirs
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
        getPlatformInfo().setUseDelimitedIdentifiers(_useDelimitedIdentifiers);
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
     * Retrieves all rows from the given table.
     * 
     * @param tableName The table
     * @return The rows
     */
    protected List getRows(String tableName)
    {
        Table        table = getModel().findTable(tableName, getPlatformInfo().isCaseSensitive());
        StringBuffer query = new StringBuffer();

        query.append("SELECT * FROM ");
        if (getPlatformInfo().isUseDelimitedIdentifiers())
        {
            query.append(getPlatformInfo().getDelimiterToken());
        }
        query.append(table.getName());
        if (getPlatformInfo().isUseDelimitedIdentifiers())
        {
            query.append(getPlatformInfo().getDelimiterToken());
        }
        
        return getPlatform().fetch(getModel(), query.toString(), new Table[] { table });
    }

    /**
     * Returns the original model adjusted for type changes because of the native type mappings
     * which when read back from the database will map to different types.
     * 
     * @return The adjusted model
     */
    protected Database getAdjustedModel()
    {
        try
        {
            Database model = (Database)getModel().clone();

            for (int tableIdx = 0; tableIdx < model.getTableCount(); tableIdx++)
            {
                Table table = model.getTable(tableIdx);

                for (int columnIdx = 0; columnIdx < table.getColumnCount(); columnIdx++)
                {
                    Column column     = table.getColumn(columnIdx);
                    int    origType   = column.getTypeCode();
                    int    targetType = getPlatformInfo().getTargetJdbcType(origType);

                    // we adjust the column types if the native type would back-map to a
                    // different jdbc type
                    if (targetType != origType)
                    {
                        column.setTypeCode(targetType);
                        // we should also adapt the default value
                        if (column.getDefaultValue() != null)
                        {
                            DefaultValueHelper helper = getPlatform().getSqlBuilder().getDefaultValueHelper();

                            column.setDefaultValue(helper.convert(column.getDefaultValue(), origType, targetType));
                        }
                    }
                    // we also promote the default size if the column has no size
                    // spec of its own
                    if ((column.getSize() == null) && getPlatformInfo().hasSize(targetType))
                    {
                        Integer defaultSize = getPlatformInfo().getDefaultSize(targetType);

                        if (defaultSize != null)
                        {
                            column.setSize(defaultSize.toString());
                        }
                    }
                    // finally the platform might return a synthetic default value if the column
                    // is a primary key column
                    if (getPlatformInfo().isReturningDefaultValueForRequired() &&
                        (column.getDefaultValue() == null) && column.isRequired() && !column.isAutoIncrement())
                    {
                        switch (column.getTypeCode())
                        {
                            case Types.TINYINT:
                            case Types.SMALLINT:
                            case Types.INTEGER:
                            case Types.BIGINT:
                                column.setDefaultValue("0");
                                break;
                            case Types.REAL:
                            case Types.FLOAT:
                            case Types.DOUBLE:
                                column.setDefaultValue("0.0");
                                break;
                            case Types.BIT:
                                column.setDefaultValue("false");
                                break;
                            default:
                                column.setDefaultValue("");
                                break;
                        }
                    }
                }
                // we also add the default names to foreign keys that are initially unnamed
                for (int fkIdx = 0; fkIdx < table.getForeignKeyCount(); fkIdx++)
                {
                    ForeignKey fk = table.getForeignKey(fkIdx);

                    if (fk.getName() == null)
                    {
                        fk.setName(getPlatform().getSqlBuilder().getForeignKeyName(table, fk));
                    }
                }
            }
            return model;
        }
        catch (CloneNotSupportedException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Compares the specified attribute value of the given bean with the expected object.
     * 
     * @param expected The expected object
     * @param bean     The bean
     * @param attrName The attribute name
     */
    protected void assertEquals(Object expected, Object bean, String attrName)
    {
        DynaBean dynaBean = (DynaBean)bean;
        Object   value    = dynaBean.get(attrName);

        if ((value instanceof byte[]) && !(expected instanceof byte[]) && (dynaBean instanceof SqlDynaBean))
        {
            SqlDynaClass dynaClass = (SqlDynaClass)((SqlDynaBean)dynaBean).getDynaClass();
            Column       column    = ((SqlDynaProperty)dynaClass.getDynaProperty(attrName)).getColumn();

            if (TypeMap.isBinaryType(column.getTypeCode()))
            {
                value = new BinaryObjectsHelper().deserialize((byte[])value);
            }
        }
        assertEquals(expected, value);
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
        try
        {
            assertEquals("Model names do not match",
                         expected.getName(),
                         actual.getName());
            assertEquals("Not the same number of tables",
                         expected.getTableCount(),
                         actual.getTableCount());
            for (int tableIdx = 0; tableIdx < actual.getTableCount(); tableIdx++)
            {
                assertEquals(expected.getTable(tableIdx),
                             actual.getTable(tableIdx));
            }
        }
        catch (Throwable ex)
        {
            StringWriter writer = new StringWriter();
            DatabaseIO   dbIo   = new DatabaseIO();

            dbIo.write(expected, writer);

            System.err.println("Expected model:\n"+writer.toString());
            
            writer = new StringWriter();
            dbIo.write(actual, writer);

            System.err.println("Actual model:\n"+writer.toString());

            if (ex instanceof Error)
            {
                throw (Error)ex;
            }
            else
            {
                throw new DdlUtilsException(ex);
            }
        }
    }

    /**
     * Asserts that the two given database tables are equal.
     * 
     * @param expected The expected table
     * @param actual   The actual table
     */
    protected void assertEquals(Table expected, Table actual)
    {
        if (_useDelimitedIdentifiers)
        {
            assertEquals("Table names do not match",
                         expected.getName(),
                         actual.getName());
        }
        else
        {
            assertEquals("Table names do not match (ignoring case)",
                         expected.getName().toUpperCase(),
                         actual.getName().toUpperCase());
        }
        assertEquals("Not the same number of columns in table "+actual.getName(),
                     expected.getColumnCount(),
                     actual.getColumnCount());
        for (int columnIdx = 0; columnIdx < actual.getColumnCount(); columnIdx++)
        {
            assertEquals(expected.getColumn(columnIdx),
                         actual.getColumn(columnIdx));
        }
        assertEquals("Not the same number of foreign keys in table "+actual.getName(),
                     expected.getForeignKeyCount(),
                     actual.getForeignKeyCount());
        // order is not assumed with the way foreignkeys are returned.
        for (int fkIdx = 0; fkIdx < actual.getForeignKeyCount(); fkIdx++)
        {
            ForeignKey fk_expected = expected.getForeignKey(fkIdx);
            ForeignKey fk_actual = null;
            for (int i = 0; i < actual.getForeignKeyCount(); i++)
            {
                fk_actual = actual.getForeignKey(i);
                if (fk_actual.getName().equalsIgnoreCase(fk_expected.getName()))
                {
                    break;
                }
            }
            assertEquals(fk_expected, fk_actual);
        }
        assertEquals("Not the same number of indices in table "+actual.getName(),
                     expected.getIndexCount(),
                     actual.getIndexCount());
        for (int indexIdx = 0; indexIdx < actual.getIndexCount(); indexIdx++)
        {
            assertEquals(expected.getIndex(indexIdx),
                         actual.getIndex(indexIdx));
        }
    }

    /**
     * Asserts that the two given columns are equal.
     * 
     * @param expected The expected column
     * @param actual   The actual column
     */
    protected void assertEquals(Column expected, Column actual)
    {
        if (_useDelimitedIdentifiers)
        {
            assertEquals("Column names do not match",
                         expected.getName(),
                         actual.getName());
        }
        else
        {
            assertEquals("Column names do not match (ignoring case)",
                         expected.getName().toUpperCase(),
                         actual.getName().toUpperCase());
        }
        assertEquals("Primary key status not the same for column "+actual.getName(),
                     expected.isPrimaryKey(),
                     actual.isPrimaryKey());
        assertEquals("Required status not the same for column "+actual.getName(),
                     expected.isRequired(),
                     actual.isRequired());
        assertEquals("Auto-increment status not the same for column "+actual.getName(),
                     expected.isAutoIncrement(),
                     actual.isAutoIncrement());
        assertEquals("Type code not the same for column "+actual.getName(),
                     expected.getTypeCode(),
                     actual.getTypeCode());
        assertEquals("Parsed default values do not match for column "+actual.getName(),
                     expected.getParsedDefaultValue(),
                     actual.getParsedDefaultValue());

        // comparing the size makes only sense for types where it is relevant
        if ((expected.getTypeCode() == Types.NUMERIC) ||
            (expected.getTypeCode() == Types.DECIMAL))
        {
            assertEquals("Precision not the same for column "+actual.getName(),
                         expected.getSize(),
                         actual.getSize());
            assertEquals("Scale not the same for column "+actual.getName(),
                         expected.getScale(),
                         actual.getScale());
        }
        else if ((expected.getTypeCode() == Types.CHAR) ||
                 (expected.getTypeCode() == Types.VARCHAR) ||
                 (expected.getTypeCode() == Types.BINARY) ||
                 (expected.getTypeCode() == Types.VARBINARY))
        {
            assertEquals("Size not the same for column "+actual.getName(),
                         expected.getSize(),
                         actual.getSize());
        }
    }

    /**
     * Asserts that the two given foreign keys are equal.
     * 
     * @param expected The expected foreign key
     * @param actual   The actual foreign key
     */
    protected void assertEquals(ForeignKey expected, ForeignKey actual)
    {
        if (_useDelimitedIdentifiers)
        {
            assertEquals("Foreign key names do not match",
                         expected.getName(),
                         actual.getName());
            assertEquals("Referenced table names do not match",
                         expected.getForeignTableName(),
                         actual.getForeignTableName());
        }
        else
        {
            assertEquals("Foreign key names do not match (ignoring case)",
                         expected.getName().toUpperCase(),
                         actual.getName().toUpperCase());
            assertEquals("Referenced table names do not match (ignoring case)",
                         expected.getForeignTableName().toUpperCase(),
                         actual.getForeignTableName().toUpperCase());
        }
        assertEquals("Not the same number of references in foreign key "+actual.getName(),
                     expected.getReferenceCount(),
                     actual.getReferenceCount());
        for (int refIdx = 0; refIdx < actual.getReferenceCount(); refIdx++)
        {
            assertEquals(expected.getReference(refIdx),
                         actual.getReference(refIdx));
        }
    }

    /**
     * Asserts that the two given references are equal.
     * 
     * @param expected The expected reference
     * @param actual   The actual reference
     */
    protected void assertEquals(Reference expected, Reference actual)
    {
        if (_useDelimitedIdentifiers)
        {
            assertEquals("Local column names do not match",
                         expected.getLocalColumnName(),
                         actual.getLocalColumnName());
            assertEquals("Foreign column names do not match",
                         expected.getForeignColumnName(),
                         actual.getForeignColumnName());
        }
        else
        {
            assertEquals("Local column names do not match (ignoring case)",
                         expected.getLocalColumnName().toUpperCase(),
                         actual.getLocalColumnName().toUpperCase());
            assertEquals("Foreign column names do not match (ignoring case)",
                         expected.getForeignColumnName().toUpperCase(),
                         actual.getForeignColumnName().toUpperCase());
        }
    }

    /**
     * Asserts that the two given indices are equal.
     * 
     * @param expected The expected index
     * @param actual   The actual index
     */
    protected void assertEquals(Index expected, Index actual)
    {
        if (_useDelimitedIdentifiers)
        {
            assertEquals("Index names do not match",
                         expected.getName(),
                         actual.getName());
        }
        else
        {
            assertEquals("Index names do not match (ignoring case)",
                         expected.getName().toUpperCase(),
                         actual.getName().toUpperCase());
        }
        assertEquals("Unique status not the same for index "+actual.getName(),
                     expected.isUnique(),
                     actual.isUnique());
        assertEquals("Not the same number of columns in index "+actual.getName(),
                     expected.getColumnCount(),
                     actual.getColumnCount());
        for (int columnIdx = 0; columnIdx < actual.getColumnCount(); columnIdx++)
        {
            assertEquals(expected.getColumn(columnIdx),
                         actual.getColumn(columnIdx));
        }
    }

    /**
     * Asserts that the two given index columns are equal.
     * 
     * @param expected The expected index column
     * @param actual   The actual index column
     */
    protected void assertEquals(IndexColumn expected, IndexColumn actual)
    {
        if (_useDelimitedIdentifiers)
        {
            assertEquals("Index column names do not match",
                         expected.getName(),
                         actual.getName());
        }
        else
        {
            assertEquals("Index column names do not match (ignoring case)",
                         expected.getName().toUpperCase(),
                         actual.getName().toUpperCase());
        }
        assertEquals("Size not the same for index column "+actual.getName(),
                     expected.getSize(),
                     actual.getSize());
    }
}
