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

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Types;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.model.CascadeActionEnum;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.ModelException;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.TypeMap;

/**
 * Tests the database XML reading/writing via the {@link org.apache.ddlutils.io.DatabaseIO} class.
 * 
 * @version $Revision: 289996 $
 */
public class TestDatabaseIO extends TestCase
{
    /**
     * Reads the database model from the given string.
     * 
     * @param modelAsXml The database model XML
     * @return The database model
     */
    private Database readModel(String modelAsXml)
    {
        DatabaseIO dbIO = new DatabaseIO();

        dbIO.setUseInternalDtd(true);
        dbIO.setValidateXml(true);
        return dbIO.read(new StringReader(modelAsXml));
    }

    /**
     * Writes the given database model to a string.
     * 
     * @param model The database model
     * @return The database model XML
     */
    private String writeModel(Database model)
    {
        StringWriter writer = new StringWriter();

        new DatabaseIO().write(model, writer);
        return StringUtils.replace(writer.toString(), "\r\n", "\n");
    }

    /**
     * Asserts the data in a table object.
     * 
     * @param name                    The expected name
     * @param description             The expected description
     * @param numColumns              The expected number of columns
     * @param numPrimaryKeyColumns    The expected number of primary key columns
     * @param numAutoIncrementColumns The expected number of auto increment columns
     * @param numForeignKeys          The expected number of foreign keys
     * @param numIndexes              The expected number of indexes
     * @param table                   The table
     */
    private void assertEquals(String name,
                              String description,
                              int    numColumns,
                              int    numPrimaryKeyColumns,
                              int    numAutoIncrementColumns,
                              int    numForeignKeys,
                              int    numIndexes,
                              Table  table)
    {
        assertEquals(name, table.getName());
        assertEquals(description, table.getDescription());
        assertEquals(numColumns, table.getColumnCount());
        assertEquals(numPrimaryKeyColumns, table.getPrimaryKeyColumns().length);
        assertEquals(numAutoIncrementColumns, table.getAutoIncrementColumns().length);
        assertEquals(numForeignKeys, table.getForeignKeyCount());
        assertEquals(numIndexes, table.getIndexCount());
    }

    /**
     * Asserts the data in a column object.
     * 
     * @param name            The expected name
     * @param typeCode        The exected JDBC type code
     * @param size            The expected size value
     * @param scale           The expected scale value
     * @param defaultValue    The expected default value
     * @param description     The expected description
     * @param javaName        The expected java name
     * @param isPrimaryKey    The expected primary key status
     * @param isRequired      The expected required satus
     * @param isAutoIncrement The expected auto increment status
     * @param column          The column
     */
    private void assertEquals(String  name,
                              int     typeCode,
                              int     size,
                              int     scale,
                              String  defaultValue,
                              String  description,
                              String  javaName,
                              boolean isPrimaryKey,
                              boolean isRequired,
                              boolean isAutoIncrement,
                              Column  column)
    {
        assertEquals(name, column.getName());
        assertEquals(TypeMap.getJdbcTypeName(typeCode), column.getType());
        assertEquals(typeCode, column.getTypeCode());
        assertEquals(size, column.getSizeAsInt());
        assertEquals(size, column.getPrecisionRadix());
        assertEquals(scale, column.getScale());
        if ((size <= 0) && (scale <= 0)) {
            assertNull(column.getSize());
        }
        else if (scale == 0) {
            assertEquals("" + size, column.getSize());
        }
        else {
            assertEquals("" + size + "," + scale, column.getSize());
        }
        assertEquals(defaultValue, column.getDefaultValue());
        assertEquals(description, column.getDescription());
        assertEquals(javaName, column.getJavaName());
        assertEquals(isPrimaryKey, column.isPrimaryKey());
        assertEquals(isRequired, column.isRequired());
        assertEquals(isAutoIncrement, column.isAutoIncrement());
    }

    /**
     * Asserts data in a foreign key object.
     * 
     * @param name            The expected name
     * @param onUpdate        The expected onUpdate action
     * @param onDelete        The expected onDelete action
     * @param referencedTable The expected referenced table
     * @param numReferences   The expected number of references
     * @param foreignKey      The foreign key
     */
    private void assertEquals(String            name,
                              CascadeActionEnum onUpdate,
                              CascadeActionEnum onDelete,
                              Table             referencedTable,
                              int               numReferences,
                              ForeignKey        foreignKey)
    {
        assertEquals(name, foreignKey.getName());
        assertEquals(onUpdate, foreignKey.getOnUpdate());
        assertEquals(onDelete, foreignKey.getOnDelete());
        assertEquals(referencedTable, foreignKey.getForeignTable());
        assertEquals(referencedTable.getName(), foreignKey.getForeignTableName());
        assertEquals(numReferences, foreignKey.getReferenceCount());
    }

    /**
     * Asserts data in a reference object.
     * 
     * @param localColumn   The expected local column
     * @param foreignColumn The expected foreign column
     * @param ref           The reference
     */
    private void assertEquals(Column localColumn, Column foreignColumn, Reference ref)
    {
        assertEquals(localColumn, ref.getLocalColumn());
        assertEquals(localColumn.getName(), ref.getLocalColumnName());
        assertEquals(foreignColumn, ref.getForeignColumn());
        assertEquals(foreignColumn.getName(), ref.getForeignColumnName());
    }

    /**
     * Asserts data in an index object.
     * 
     * @param name       The expected name
     * @param isUnique   The expected unique status
     * @param numColumns The expected number of columns
     * @param index      The index
     */
    private void assertEquals(String name, boolean isUnique, int numColumns, Index index)
    {
        assertEquals(name, index.getName());
        assertEquals(isUnique, index.isUnique());
        assertEquals(numColumns, index.getColumnCount());
    }

    /**
     * Asserts data in an index column object.
     * 
     * @param column      The expected column
     * @param size        The expected size value 
     * @param indexColumn The index column
     */
    private void assertEquals(Column column, String size, IndexColumn indexColumn)
    {
        assertEquals(column, indexColumn.getColumn());
        assertEquals(column.getName(), indexColumn.getName());
        assertEquals(size, indexColumn.getSize());
    }

    /**
     * Asserts that the given database model, written to XML, is equal to the given
     * expected XML.
     * 
     * @param expectedXml The expected XML
     * @param model       The database model
     */
    private void assertEquals(String expectedXml, Database model)
    {
        assertEquals(expectedXml, writeModel(model));
    }

    /**
     * Tests an XML document without a database element.
     */
    public void testNoDatabaseElement()
    {
        try
        {
            readModel("<data-base xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "'></data-base>");

            fail();
        }
        catch (DdlUtilsXMLException ex)
        {}
    }

    /**
     * Tests that an exception is generated by the validation when the database element has no namespace attribute.
     */
    public void testDatabaseWithoutNamespace()
    {
        try
        {
            readModel("<database></database>");

            fail();
        }
        catch (DdlUtilsXMLException ex)
        {}
    }

    /**
     * Tests that an exception is generated when the database element has no name attribute.
     */
    public void testDatabaseWithoutName()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "'>\n" +
                "  <table name='TestTable'>\n" +
                "    <column name='id'\n" +
                "            type='INTEGER'\n" +
                "            primaryKey='true'\n" +
                "            required='true'/>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (DdlUtilsXMLException ex)
        {}
    }

    /**
     * Tests a database model without tables.
     */
    public void testNoTables() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "</database>");

        assertEquals("test",
                     model.getName());
        assertEquals(0,
                     model.getTableCount());

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\" />\n",
            model);
    }

    /**
     * Tests a database model with a table without columns.
     */
    public void testTableWithoutColumns() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='SomeTable'\n" +
            "         description='Some table'>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(1, model.getTableCount());
        assertEquals("SomeTable", "Some table", 0, 0, 0, 0, 0,
                     model.getTable(0));

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"SomeTable\" description=\"Some table\" />\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests that an exception is generated when the table element has no name attribute.
     */
    public void testTableWithoutName()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table>\n" +
                "    <column name='id'\n" +
                "            type='INTEGER'\n" +
                "            primaryKey='true'\n" +
                "            required='true'/>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (DdlUtilsXMLException ex)
        {}
    }

    /**
     * Tests a database model with a table with a single column.
     */
    public void testSingleColumn() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='SomeTable'\n" +
            "         description='Some table'>\n" +
            "    <column name='ID'\n" +
            "            type='INTEGER'/>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(1, model.getTableCount());
        
        Table table = model.getTable(0);

        assertEquals("SomeTable", "Some table", 1, 0, 0, 0, 0,
                     table);
        assertEquals("ID", Types.INTEGER, 0, 0, null, null, null, false, false, false,
                     table.getColumn(0));

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"SomeTable\" description=\"Some table\">\n" +
            "    <column name=\"ID\" primaryKey=\"false\" required=\"false\" type=\"INTEGER\" autoIncrement=\"false\" />\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests that an exception is generated when the column element has no name attribute.
     */
    public void testColumnWithoutName()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='TestTable'>\n" +
                "    <column type='INTEGER'\n" +
                "            primaryKey='true'\n" +
                "            required='true'/>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (DdlUtilsXMLException ex)
        {}
    }

    /**
     * Tests that an exception is generated when the column element has no type attribute.
     */
    public void testColumnWithoutType()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='TestTable'>\n" +
                "    <column name='id'\n" +
                "            primaryKey='true'\n" +
                "            required='true'/>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (DdlUtilsXMLException ex)
        {}
    }

    /**
     * Tests a database model columns of all possible datatypes.
     */
    public void testColumnTypes() throws Exception
    {
        StringBuffer modelXml = new StringBuffer();
        int[]        types    = TypeMap.getSuportedJdbcTypes();

        modelXml.append("<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n");
        modelXml.append("  <table name='SomeTable'\n");
        modelXml.append("         description='Some table'>\n");
        for (int idx = 0; idx < types.length; idx++)
        {
            modelXml.append("    <column name='ID");
            modelXml.append(idx);
            modelXml.append("' type='");
            modelXml.append(TypeMap.getJdbcTypeName(types[idx]));
            modelXml.append("'/>\n");
        }
        modelXml.append("  </table>\n");
        modelXml.append("</database>");

        Database model = readModel(modelXml.toString());

        assertEquals("test", model.getName());
        assertEquals(1, model.getTableCount());
        
        Table table = model.getTable(0);

        assertEquals("SomeTable", "Some table", types.length, 0, 0, 0, 0,
                     table);

        for (int idx = 0; idx < types.length; idx++)
        {
            assertEquals("ID" + idx, types[idx], 0, 0, null, null, null, false, false, false,
                         table.getColumn(idx));
        }

        modelXml.setLength(0);
        modelXml.append("<?xml version='1.0' encoding='UTF-8'?>\n");
        modelXml.append("<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n");
        modelXml.append("  <table name=\"SomeTable\" description=\"Some table\">\n");
        for (int idx = 0; idx < types.length; idx++)
        {
            modelXml.append("    <column name=\"ID");
            modelXml.append(idx);
            modelXml.append("\" primaryKey=\"false\" required=\"false\" type=\"");
            modelXml.append(TypeMap.getJdbcTypeName(types[idx]));
            modelXml.append("\" autoIncrement=\"false\" />\n");
        }
        modelXml.append("  </table>\n");
        modelXml.append("</database>\n");

        assertEquals(modelXml.toString(), model);
    }

    /**
     * Tests an illegal column type.
     */
    public void testColumnWithIllegalType()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='TestTable'>\n" +
                "    <column name='id'\n" +
                "            type='illegal'\n" +
                "            primaryKey='true'\n" +
                "            required='true'/>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (DdlUtilsXMLException ex)
        {}
    }

    /**
     * Tests a database model with a table with a primary key column.
     */
    public void testPrimaryKeyColumn() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='SomeTable'\n" +
            "         description='Some table'>\n" +
            "    <column name='ID'\n" +
            "            type='INTEGER'\n" +
            "            primaryKey='true'/>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(1, model.getTableCount());
        
        Table table = model.getTable(0);

        assertEquals("SomeTable", "Some table", 1, 1, 0, 0, 0,
                     table);

        Column column = table.getColumn(0);

        assertEquals("ID", Types.INTEGER, 0, 0, null, null, null, true, false, false,
                     column);

        assertEquals(column, table.getPrimaryKeyColumns()[0]);
        
        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"SomeTable\" description=\"Some table\">\n" +
            "    <column name=\"ID\" primaryKey=\"true\" required=\"false\" type=\"INTEGER\" autoIncrement=\"false\" />\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests a database model with a table with a required column.
     */
    public void testRequiredColumn() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='SomeTable'\n" +
            "         description='Some table'>\n" +
            "    <column name='ID'\n" +
            "            type='INTEGER'\n" +
            "            required='true'/>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(1, model.getTableCount());
        
        Table table = model.getTable(0);

        assertEquals("SomeTable", "Some table", 1, 0, 0, 0, 0,
                     table);
        assertEquals("ID", Types.INTEGER, 0, 0, null, null, null, false, true, false,
                     table.getColumn(0));

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"SomeTable\" description=\"Some table\">\n" +
            "    <column name=\"ID\" primaryKey=\"false\" required=\"true\" type=\"INTEGER\" autoIncrement=\"false\" />\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests a database model with a table with an autoincrement column.
     */
    public void testAutoIncrementColumn() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='SomeTable'\n" +
            "         description='Some table'>\n" +
            "    <column name='ID'\n" +
            "            type='INTEGER'\n" +
            "            autoIncrement='true'/>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(1, model.getTableCount());
        
        Table table = model.getTable(0);

        assertEquals("SomeTable", "Some table", 1, 0, 1, 0, 0,
                     table);

        Column column = table.getColumn(0);

        assertEquals("ID", Types.INTEGER, 0, 0, null, null, null, false, false, true,
                     column);

        assertEquals(column, table.getAutoIncrementColumns()[0]);

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"SomeTable\" description=\"Some table\">\n" +
            "    <column name=\"ID\" primaryKey=\"false\" required=\"false\" type=\"INTEGER\" autoIncrement=\"true\" />\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests a database model with a table with a column with a size spec.
     */
    public void testColumnWithSize1() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='SomeTable'\n" +
            "         description='Some table'>\n" +
            "    <column name='ID'\n" +
            "            type='VARCHAR'\n" +
            "            size='20'/>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(1, model.getTableCount());
        
        Table table = model.getTable(0);

        assertEquals("SomeTable", "Some table", 1, 0, 0, 0, 0,
                     table);
        assertEquals("ID", Types.VARCHAR, 20, 0, null, null, null, false, false, false,
                     table.getColumn(0));

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"SomeTable\" description=\"Some table\">\n" +
            "    <column name=\"ID\" primaryKey=\"false\" required=\"false\" type=\"VARCHAR\" size=\"20\" autoIncrement=\"false\" />\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests a database model with a table with a column with a size spec.
     */
    public void testColumnWithSize2() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='SomeTable'\n" +
            "         description='Some table'>\n" +
            "    <column name='ID'\n" +
            "            type='DECIMAL'\n" +
            "            size='10,3'/>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(1, model.getTableCount());
        
        Table table = model.getTable(0);

        assertEquals("SomeTable", "Some table", 1, 0, 0, 0, 0,
                     table);
        assertEquals("ID", Types.DECIMAL, 10, 3, null, null, null, false, false, false,
                     table.getColumn(0));

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"SomeTable\" description=\"Some table\">\n" +
            "    <column name=\"ID\" primaryKey=\"false\" required=\"false\" type=\"DECIMAL\" size=\"10,3\" autoIncrement=\"false\" />\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests a database model with a table with a column with a description.
     */
    public void testColumnWithDescription() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='SomeTable'\n" +
            "         description='Some table'>\n" +
            "    <column name='ID'\n" +
            "            type='INTEGER'\n" +
            "            description='Foo'/>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(1, model.getTableCount());
        
        Table table = model.getTable(0);

        assertEquals("SomeTable", "Some table", 1, 0, 0, 0, 0,
                     table);
        assertEquals("ID", Types.INTEGER, 0, 0, null, "Foo", null, false, false, false,
                     table.getColumn(0));

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"SomeTable\" description=\"Some table\">\n" +
            "    <column name=\"ID\" primaryKey=\"false\" required=\"false\" type=\"INTEGER\" autoIncrement=\"false\" description=\"Foo\" />\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests a database model with a table with a column with a default.
     */
    public void testColumnWithDefault() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='SomeTable'\n" +
            "         description='Some table'>\n" +
            "    <column name='ID'\n" +
            "            type='VARCHAR'\n" +
            "            size='32'\n" +
            "            default='Test string'/>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(1, model.getTableCount());
        
        Table table = model.getTable(0);

        assertEquals("SomeTable", "Some table", 1, 0, 0, 0, 0,
                     table);
        assertEquals("ID", Types.VARCHAR, 32, 0, "Test string", null, null, false, false, false,
                     table.getColumn(0));

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"SomeTable\" description=\"Some table\">\n" +
            "    <column name=\"ID\" primaryKey=\"false\" required=\"false\" type=\"VARCHAR\" size=\"32\" default=\"Test string\" autoIncrement=\"false\" />\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests a database model with a table with a column with a java name.
     */
    public void testColumnWithJavaName() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='SomeTable'\n" +
            "         description='Some table'>\n" +
            "    <column name='ID'\n" +
            "            type='DOUBLE'\n" +
            "            javaName='testString'/>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(1, model.getTableCount());
        
        Table table = model.getTable(0);

        assertEquals("SomeTable", "Some table", 1, 0, 0, 0, 0,
                     table);
        assertEquals("ID", Types.DOUBLE, 0, 0, null, null, "testString", false, false, false,
                     table.getColumn(0));

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"SomeTable\" description=\"Some table\">\n" +
            "    <column name=\"ID\" primaryKey=\"false\" required=\"false\" type=\"DOUBLE\" autoIncrement=\"false\" javaName=\"testString\" />\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests a database model containing a single foreignkey.
     */
    public void testSingleForeignkey() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='SomeTable'\n" +
            "         description='Some table'>\n" +
            "    <column name='ID'\n" +
            "            type='VARCHAR'\n" +
            "            size='16'\n" +
            "            primaryKey='true'\n" +
            "            required='true'\n" +
            "            description='The primary key'/>\n" +
            "  </table>\n" +
            "  <table name='AnotherTable'\n" +
            "         description='And another table'>\n" +
            "    <column name='Some_ID'\n" +
            "            type='VARCHAR'\n" +
            "            size='16'\n" +
            "            description='The foreign key'/>\n" +
            "    <foreign-key foreignTable='SomeTable'>\n" +
            "       <reference local='Some_ID' foreign='ID'/>\n" +
            "    </foreign-key>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(2, model.getTableCount());

        Table someTable = model.getTable(0);

        assertEquals("SomeTable", "Some table", 1, 1, 0, 0, 0,
                     someTable);

        Column pkColumn = someTable.getColumn(0);

        assertEquals("ID", Types.VARCHAR, 16, 0, null, "The primary key", null, true, true, false,
                     pkColumn);

        Table anotherTable = model.getTable(1);

        assertEquals("AnotherTable", "And another table", 1, 0, 0, 1, 0,
                     anotherTable);

        Column fkColumn = anotherTable.getColumn(0);

        assertEquals("Some_ID", Types.VARCHAR, 16, 0, null, "The foreign key", null, false, false, false,
                     fkColumn);

        ForeignKey fk = anotherTable.getForeignKey(0);

        assertEquals(null, CascadeActionEnum.NONE, CascadeActionEnum.NONE, someTable, 1, fk);
        assertEquals(fkColumn, pkColumn, fk.getFirstReference());

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"SomeTable\" description=\"Some table\">\n" +
            "    <column name=\"ID\" primaryKey=\"true\" required=\"true\" type=\"VARCHAR\" size=\"16\" autoIncrement=\"false\" description=\"The primary key\" />\n" +
            "  </table>\n" +
            "  <table name=\"AnotherTable\" description=\"And another table\">\n" +
            "    <column name=\"Some_ID\" primaryKey=\"false\" required=\"false\" type=\"VARCHAR\" size=\"16\" autoIncrement=\"false\" description=\"The foreign key\" />\n" +
            "    <foreign-key foreignTable=\"SomeTable\">\n" +
            "      <reference local=\"Some_ID\" foreign=\"ID\" />\n" +
            "    </foreign-key>\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests a database model containing a foreignkey with two references.
     */
    public void testForeignkeyWithTwoReferences() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='SomeTable'\n" +
            "         description='Some table'>\n" +
            "    <column name='ID'\n" +
            "            type='VARCHAR'\n" +
            "            size='16'\n" +
            "            primaryKey='true'\n" +
            "            required='true'\n" +
            "            description='The primary key'/>\n" +
            "    <column name='VALUE1'\n" +
            "            type='INTEGER'\n" +
            "            required='false'\n" +
            "            description='A value'/>\n" +
            "    <column name='VALUE2'\n" +
            "            type='DOUBLE'\n" +
            "            required='false'\n" +
            "            description='Another value'/>\n" +
            "  </table>\n" +
            "  <table name='AnotherTable'\n" +
            "         description='And another table'>\n" +
            "    <column name='Some_ID'\n" +
            "            type='VARCHAR'\n" +
            "            size='16'\n" +
            "            description='The foreign key'/>\n" +
            "    <column name='Some_Value'\n" +
            "            type='DOUBLE'/>\n" +
            "    <foreign-key foreignTable='SomeTable'>\n" +
            "       <reference local='Some_ID' foreign='ID'/>\n" +
            "       <reference local='Some_Value' foreign='VALUE2'/>\n" +
            "    </foreign-key>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(2, model.getTableCount());

        Table someTable = model.getTable(0);

        assertEquals("SomeTable", "Some table", 3, 1, 0, 0, 0,
                     someTable);
        assertEquals("ID", Types.VARCHAR, 16, 0, null, "The primary key", null, true, true, false,
                     someTable.getColumn(0));

        Table anotherTable = model.getTable(1);

        assertEquals("AnotherTable", "And another table", 2, 0, 0, 1, 0,
                     anotherTable);
        assertEquals("Some_ID", Types.VARCHAR, 16, 0, null, "The foreign key", null, false, false, false,
                     anotherTable.getColumn(0));

        ForeignKey fk = anotherTable.getForeignKey(0);

        assertEquals(null, CascadeActionEnum.NONE, CascadeActionEnum.NONE, someTable, 2, fk);
        assertEquals(anotherTable.getColumn(0), someTable.getColumn(0), fk.getReference(0));
        assertEquals(anotherTable.getColumn(1), someTable.getColumn(2), fk.getReference(1));

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"SomeTable\" description=\"Some table\">\n" +
            "    <column name=\"ID\" primaryKey=\"true\" required=\"true\" type=\"VARCHAR\" size=\"16\" autoIncrement=\"false\" description=\"The primary key\" />\n" +
            "    <column name=\"VALUE1\" primaryKey=\"false\" required=\"false\" type=\"INTEGER\" autoIncrement=\"false\" description=\"A value\" />\n" +
            "    <column name=\"VALUE2\" primaryKey=\"false\" required=\"false\" type=\"DOUBLE\" autoIncrement=\"false\" description=\"Another value\" />\n" +
            "  </table>\n" +
            "  <table name=\"AnotherTable\" description=\"And another table\">\n" +
            "    <column name=\"Some_ID\" primaryKey=\"false\" required=\"false\" type=\"VARCHAR\" size=\"16\" autoIncrement=\"false\" description=\"The foreign key\" />\n" +
            "    <column name=\"Some_Value\" primaryKey=\"false\" required=\"false\" type=\"DOUBLE\" autoIncrement=\"false\" />\n" +
            "    <foreign-key foreignTable=\"SomeTable\">\n" +
            "      <reference local=\"Some_ID\" foreign=\"ID\" />\n" +
            "      <reference local=\"Some_Value\" foreign=\"VALUE2\" />\n" +
            "    </foreign-key>\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests a foreign key without references.
     */
    public void testForeignKeyWithoutReferences()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='SomeTable'\n" +
                "         description='Some table'>\n" +
                "    <column name='ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            primaryKey='true'\n" +
                "            required='true'\n" +
                "            description='The primary key'/>\n" +
                "  </table>\n" +
                "  <table name='AnotherTable'\n" +
                "         description='And another table'>\n" +
                "    <column name='Some_ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            description='The foreign key'/>\n" +
                "    <foreign-key foreignTable='SomeTable'>\n" +
                "    </foreign-key>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (DdlUtilsXMLException ex)
        {}
    }

    /**
     * Tests a database model containing a named foreignkey.
     */
    public void testNamedForeignkey() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='SomeTable'\n" +
            "         description='Some table'>\n" +
            "    <column name='ID'\n" +
            "            type='VARCHAR'\n" +
            "            size='16'\n" +
            "            primaryKey='true'\n" +
            "            required='true'\n" +
            "            description='The primary key'/>\n" +
            "  </table>\n" +
            "  <table name='AnotherTable'\n" +
            "         description='And another table'>\n" +
            "    <column name='Some_ID'\n" +
            "            type='VARCHAR'\n" +
            "            size='16'\n" +
            "            description='The foreign key'/>\n" +
            "    <foreign-key name='The foreignkey' foreignTable='SomeTable'>\n" +
            "       <reference local='Some_ID' foreign='ID'/>\n" +
            "    </foreign-key>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(2, model.getTableCount());

        Table someTable = model.getTable(0);

        assertEquals("SomeTable", "Some table", 1, 1, 0, 0, 0,
                     someTable);
        assertEquals("ID", Types.VARCHAR, 16, 0, null, "The primary key", null, true, true, false,
                     someTable.getColumn(0));

        Table anotherTable = model.getTable(1);

        assertEquals("AnotherTable", "And another table", 1, 0, 0, 1, 0,
                     anotherTable);
        assertEquals("Some_ID", Types.VARCHAR, 16, 0, null, "The foreign key", null, false, false, false,
                     anotherTable.getColumn(0));

        ForeignKey fk = anotherTable.getForeignKey(0);

        assertEquals("The foreignkey", CascadeActionEnum.NONE, CascadeActionEnum.NONE, someTable, 1, fk);
        assertEquals(anotherTable.getColumn(0), someTable.getColumn(0), fk.getReference(0));

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"SomeTable\" description=\"Some table\">\n" +
            "    <column name=\"ID\" primaryKey=\"true\" required=\"true\" type=\"VARCHAR\" size=\"16\" autoIncrement=\"false\" description=\"The primary key\" />\n" +
            "  </table>\n" +
            "  <table name=\"AnotherTable\" description=\"And another table\">\n" +
            "    <column name=\"Some_ID\" primaryKey=\"false\" required=\"false\" type=\"VARCHAR\" size=\"16\" autoIncrement=\"false\" description=\"The foreign key\" />\n" +
            "    <foreign-key foreignTable=\"SomeTable\" name=\"The foreignkey\">\n" +
            "      <reference local=\"Some_ID\" foreign=\"ID\" />\n" +
            "    </foreign-key>\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests a database model containing foreignkeys with onUpdate values.
     */
    public void testForeignkeysWithOnUpdate() throws Exception
    {
        StringBuffer modelXml = new StringBuffer();

        modelXml.append("<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n");
        modelXml.append("  <table name='SomeTable'\n");
        modelXml.append("         description='Some table'>\n");
        modelXml.append("    <column name='ID'\n");
        modelXml.append("            type='VARCHAR'\n");
        modelXml.append("            size='16'\n");
        modelXml.append("            primaryKey='true'\n");
        modelXml.append("            required='true'\n");
        modelXml.append("            description='The primary key'/>\n");
        modelXml.append("  </table>\n");
        modelXml.append("  <table name='AnotherTable'\n");
        modelXml.append("         description='And another table'>\n");
        modelXml.append("    <column name='Some_ID'\n");
        modelXml.append("            type='VARCHAR'\n");
        modelXml.append("            size='16'\n");
        modelXml.append("            description='The foreign key'/>\n");
        for (Iterator it = CascadeActionEnum.iterator(); it.hasNext();)
        {
            CascadeActionEnum enumValue = (CascadeActionEnum)it.next();

            modelXml.append("    <foreign-key name='foreignkey ");
            modelXml.append(enumValue.getName());
            modelXml.append("' foreignTable='SomeTable' onUpdate='");
            modelXml.append(enumValue.getName());
            modelXml.append("'>\n");
            modelXml.append("       <reference local='Some_ID' foreign='ID'/>\n");
            modelXml.append("    </foreign-key>\n");
        }
        modelXml.append("  </table>\n");
        modelXml.append("</database>");

        Database model = readModel(modelXml.toString());

        assertEquals("test", model.getName());
        assertEquals(2, model.getTableCount());

        Table someTable = model.getTable(0);

        assertEquals("SomeTable", "Some table", 1, 1, 0, 0, 0,
                     someTable);
        assertEquals("ID", Types.VARCHAR, 16, 0, null, "The primary key", null, true, true, false,
                     someTable.getColumn(0));

        Table anotherTable = model.getTable(1);

        assertEquals("AnotherTable", "And another table", 1, 0, 0, CascadeActionEnum.getEnumList().size(), 0,
                     anotherTable);
        assertEquals("Some_ID", Types.VARCHAR, 16, 0, null, "The foreign key", null, false, false, false,
                     anotherTable.getColumn(0));

        int idx = 0;
        
        for (Iterator it = CascadeActionEnum.iterator(); it.hasNext(); idx++)
        {
            CascadeActionEnum enumValue = (CascadeActionEnum)it.next();
            ForeignKey        fk        = anotherTable.getForeignKey(idx);

            assertEquals("foreignkey " + enumValue.getName(), enumValue, CascadeActionEnum.NONE, someTable, 1, fk);
            assertEquals(anotherTable.getColumn(0), someTable.getColumn(0), fk.getReference(0));
        }

        modelXml.setLength(0);
        modelXml.append("<?xml version='1.0' encoding='UTF-8'?>\n");
        modelXml.append("<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n");
        modelXml.append("  <table name=\"SomeTable\" description=\"Some table\">\n");
        modelXml.append("    <column name=\"ID\" primaryKey=\"true\" required=\"true\" type=\"VARCHAR\" size=\"16\" autoIncrement=\"false\" description=\"The primary key\" />\n");
        modelXml.append("  </table>\n");
        modelXml.append("  <table name=\"AnotherTable\" description=\"And another table\">\n");
        modelXml.append("    <column name=\"Some_ID\" primaryKey=\"false\" required=\"false\" type=\"VARCHAR\" size=\"16\" autoIncrement=\"false\" description=\"The foreign key\" />\n");
        for (Iterator it = CascadeActionEnum.iterator(); it.hasNext(); idx++)
        {
            CascadeActionEnum enumValue = (CascadeActionEnum)it.next();

            modelXml.append("    <foreign-key foreignTable=\"SomeTable\" name=\"foreignkey ");
            modelXml.append(enumValue.getName());
            if (enumValue != CascadeActionEnum.NONE)
            {
                modelXml.append("\" onUpdate=\"");
                modelXml.append(enumValue.getName());
            }
            modelXml.append("\">\n");
            modelXml.append("      <reference local=\"Some_ID\" foreign=\"ID\" />\n");
            modelXml.append("    </foreign-key>\n");
        }
        modelXml.append("  </table>\n");
        modelXml.append("</database>\n");

        assertEquals(modelXml.toString(), model);
    }

    /**
     * Tests a database model containing foreignkeys with onDelete values.
     */
    public void testForeignkeysWithOnDelete() throws Exception
    {
        StringBuffer modelXml = new StringBuffer();

        modelXml.append("<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n");
        modelXml.append("  <table name='SomeTable'\n");
        modelXml.append("         description='Some table'>\n");
        modelXml.append("    <column name='ID'\n");
        modelXml.append("            type='VARCHAR'\n");
        modelXml.append("            size='16'\n");
        modelXml.append("            primaryKey='true'\n");
        modelXml.append("            required='true'\n");
        modelXml.append("            description='The primary key'/>\n");
        modelXml.append("  </table>\n");
        modelXml.append("  <table name='AnotherTable'\n");
        modelXml.append("         description='And another table'>\n");
        modelXml.append("    <column name='Some_ID'\n");
        modelXml.append("            type='VARCHAR'\n");
        modelXml.append("            size='16'\n");
        modelXml.append("            description='The foreign key'/>\n");
        for (Iterator it = CascadeActionEnum.iterator(); it.hasNext();)
        {
            CascadeActionEnum enumValue = (CascadeActionEnum)it.next();

            modelXml.append("    <foreign-key name='foreignkey ");
            modelXml.append(enumValue.getName());
            modelXml.append("' foreignTable='SomeTable' onDelete='");
            modelXml.append(enumValue.getName());
            modelXml.append("'>\n");
            modelXml.append("       <reference local='Some_ID' foreign='ID'/>\n");
            modelXml.append("    </foreign-key>\n");
        }
        modelXml.append("  </table>\n");
        modelXml.append("</database>");

        Database model = readModel(modelXml.toString());

        assertEquals("test", model.getName());
        assertEquals(2, model.getTableCount());

        Table someTable = model.getTable(0);

        assertEquals("SomeTable", "Some table", 1, 1, 0, 0, 0,
                     someTable);
        assertEquals("ID", Types.VARCHAR, 16, 0, null, "The primary key", null, true, true, false,
                     someTable.getColumn(0));

        Table anotherTable = model.getTable(1);

        assertEquals("AnotherTable", "And another table", 1, 0, 0, CascadeActionEnum.getEnumList().size(), 0,
                     anotherTable);
        assertEquals("Some_ID", Types.VARCHAR, 16, 0, null, "The foreign key", null, false, false, false,
                     anotherTable.getColumn(0));

        int idx = 0;
        
        for (Iterator it = CascadeActionEnum.iterator(); it.hasNext(); idx++)
        {
            CascadeActionEnum enumValue = (CascadeActionEnum)it.next();
            ForeignKey        fk        = anotherTable.getForeignKey(idx);

            assertEquals("foreignkey " + enumValue.getName(), CascadeActionEnum.NONE, enumValue, someTable, 1, fk);
            assertEquals(anotherTable.getColumn(0), someTable.getColumn(0), fk.getReference(0));
        }

        modelXml.setLength(0);
        modelXml.append("<?xml version='1.0' encoding='UTF-8'?>\n");
        modelXml.append("<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n");
        modelXml.append("  <table name=\"SomeTable\" description=\"Some table\">\n");
        modelXml.append("    <column name=\"ID\" primaryKey=\"true\" required=\"true\" type=\"VARCHAR\" size=\"16\" autoIncrement=\"false\" description=\"The primary key\" />\n");
        modelXml.append("  </table>\n");
        modelXml.append("  <table name=\"AnotherTable\" description=\"And another table\">\n");
        modelXml.append("    <column name=\"Some_ID\" primaryKey=\"false\" required=\"false\" type=\"VARCHAR\" size=\"16\" autoIncrement=\"false\" description=\"The foreign key\" />\n");
        for (Iterator it = CascadeActionEnum.iterator(); it.hasNext(); idx++)
        {
            CascadeActionEnum enumValue = (CascadeActionEnum)it.next();

            modelXml.append("    <foreign-key foreignTable=\"SomeTable\" name=\"foreignkey ");
            modelXml.append(enumValue.getName());
            if (enumValue != CascadeActionEnum.NONE)
            {
                modelXml.append("\" onDelete=\"");
                modelXml.append(enumValue.getName());
            }
            modelXml.append("\">\n");
            modelXml.append("      <reference local=\"Some_ID\" foreign=\"ID\" />\n");
            modelXml.append("    </foreign-key>\n");
        }
        modelXml.append("  </table>\n");
        modelXml.append("</database>\n");

        assertEquals(modelXml.toString(), model);
    }

    /**
     * Tests a foreign key with an illegal onUpdate value.
     */
    public void testForeignKeyWithIllegalOnUpdateValue()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='SomeTable'\n" +
                "         description='Some table'>\n" +
                "    <column name='ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            primaryKey='true'\n" +
                "            required='true'\n" +
                "            description='The primary key'/>\n" +
                "  </table>\n" +
                "  <table name='AnotherTable'\n" +
                "         description='And another table'>\n" +
                "    <column name='Some_ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            description='The foreign key'/>\n" +
                "    <foreign-key foreignTable='SomeTable' onUpdate='illegal'>\n" +
                "       <reference local='Some_ID' foreign='ID'/>\n" +
                "    </foreign-key>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (DdlUtilsXMLException ex)
        {}
    }

    /**
     * Tests a foreign key with an empty onUpdate value.
     */
    public void testForeignKeyWithEmptyOnUpdateValue()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='SomeTable'\n" +
                "         description='Some table'>\n" +
                "    <column name='ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            primaryKey='true'\n" +
                "            required='true'\n" +
                "            description='The primary key'/>\n" +
                "  </table>\n" +
                "  <table name='AnotherTable'\n" +
                "         description='And another table'>\n" +
                "    <column name='Some_ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            description='The foreign key'/>\n" +
                "    <foreign-key foreignTable='SomeTable' onUpdate=''>\n" +
                "       <reference local='Some_ID' foreign='ID'/>\n" +
                "    </foreign-key>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (DdlUtilsXMLException ex)
        {}
    }

    /**
     * Tests a foreign key with an illegal onDelete value.
     */
    public void testForeignKeyWithIllegalOnDeleteValue()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='SomeTable'\n" +
                "         description='Some table'>\n" +
                "    <column name='ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            primaryKey='true'\n" +
                "            required='true'\n" +
                "            description='The primary key'/>\n" +
                "  </table>\n" +
                "  <table name='AnotherTable'\n" +
                "         description='And another table'>\n" +
                "    <column name='Some_ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            description='The foreign key'/>\n" +
                "    <foreign-key foreignTable='SomeTable' onDelete='illegal'>\n" +
                "       <reference local='Some_ID' foreign='ID'/>\n" +
                "    </foreign-key>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (DdlUtilsXMLException ex)
        {}
    }

    /**
     * Tests a foreign key with an empty onDelete value.
     */
    public void testForeignKeyWithEmptyOnDeleteValue()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='SomeTable'\n" +
                "         description='Some table'>\n" +
                "    <column name='ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            primaryKey='true'\n" +
                "            required='true'\n" +
                "            description='The primary key'/>\n" +
                "  </table>\n" +
                "  <table name='AnotherTable'\n" +
                "         description='And another table'>\n" +
                "    <column name='Some_ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            description='The foreign key'/>\n" +
                "    <foreign-key foreignTable='SomeTable' onDelete=''>\n" +
                "       <reference local='Some_ID' foreign='ID'/>\n" +
                "    </foreign-key>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (DdlUtilsXMLException ex)
        {}
    }

    /**
     * Tests a foreign key referencing a non-existing table.
     */
    public void testForeignKeyReferencingUndefinedTable()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='SomeTable'\n" +
                "         description='Some table'>\n" +
                "    <column name='ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            primaryKey='true'\n" +
                "            required='true'\n" +
                "            description='The primary key'/>\n" +
                "  </table>\n" +
                "  <table name='AnotherTable'\n" +
                "         description='And another table'>\n" +
                "    <column name='Some_ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            description='The foreign key'/>\n" +
                "    <foreign-key foreignTable='TheTable'>\n" +
                "       <reference local='Some_ID' foreign='ID'/>\n" +
                "    </foreign-key>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (ModelException ex)
        {}
    }

    /**
     * Tests a foreign key using a non-existing column in the local table.
     */
    public void testForeignKeyUsingUndefinedColumn()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='SomeTable'\n" +
                "         description='Some table'>\n" +
                "    <column name='ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            primaryKey='true'\n" +
                "            required='true'\n" +
                "            description='The primary key'/>\n" +
                "  </table>\n" +
                "  <table name='AnotherTable'\n" +
                "         description='And another table'>\n" +
                "    <column name='Some_ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            description='The foreign key'/>\n" +
                "    <foreign-key foreignTable='SomeTable'>\n" +
                "       <reference local='ID' foreign='ID'/>\n" +
                "    </foreign-key>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (ModelException ex)
        {}
    }

    /**
     * Tests a foreign key referencing a non-existing column in the foreign table.
     */
    public void testForeignKeyReferencingUndefinedColumn()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='SomeTable'\n" +
                "         description='Some table'>\n" +
                "    <column name='ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            primaryKey='true'\n" +
                "            required='true'\n" +
                "            description='The primary key'/>\n" +
                "  </table>\n" +
                "  <table name='AnotherTable'\n" +
                "         description='And another table'>\n" +
                "    <column name='Some_ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            description='The foreign key'/>\n" +
                "    <foreign-key foreignTable='SomeTable'>\n" +
                "       <reference local='Some_ID' foreign='TheID'/>\n" +
                "    </foreign-key>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (ModelException ex)
        {}
    }

    /**
     * Tests a foreign key without a local column.
     */
    public void testForeignKeyWithoutLocalColumn()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='SomeTable'\n" +
                "         description='Some table'>\n" +
                "    <column name='ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            primaryKey='true'\n" +
                "            required='true'\n" +
                "            description='The primary key'/>\n" +
                "  </table>\n" +
                "  <table name='AnotherTable'\n" +
                "         description='And another table'>\n" +
                "    <column name='Some_ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            description='The foreign key'/>\n" +
                "    <foreign-key foreignTable='SomeTable'>\n" +
                "       <reference foreign='ID'/>\n" +
                "    </foreign-key>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (DdlUtilsXMLException ex)
        {}
    }

    /**
     * Tests a foreign key without a remote column.
     */
    public void testForeignKeyWithoutRemoteColumn()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='SomeTable'\n" +
                "         description='Some table'>\n" +
                "    <column name='ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            primaryKey='true'\n" +
                "            required='true'\n" +
                "            description='The primary key'/>\n" +
                "  </table>\n" +
                "  <table name='AnotherTable'\n" +
                "         description='And another table'>\n" +
                "    <column name='Some_ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            description='The foreign key'/>\n" +
                "    <foreign-key foreignTable='SomeTable'>\n" +
                "       <reference local='Some_ID'/>\n" +
                "    </foreign-key>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (DdlUtilsXMLException ex)
        {}
    }

    /**
     * Tests a database model containing two foreignkeys.
     */
    public void testTwoForeignkeys() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='SomeTable'\n" +
            "         description='Some table'>\n" +
            "    <column name='ID'\n" +
            "            type='VARCHAR'\n" +
            "            size='16'\n" +
            "            primaryKey='true'\n" +
            "            required='true'\n" +
            "            description='The primary key'/>\n" +
            "    <column name='VALUE1'\n" +
            "            type='INTEGER'\n" +
            "            required='false'\n" +
            "            description='A value'/>\n" +
            "    <column name='VALUE2'\n" +
            "            type='DOUBLE'\n" +
            "            required='false'\n" +
            "            description='Another value'/>\n" +
            "  </table>\n" +
            "  <table name='AnotherTable'\n" +
            "         description='And another table'>\n" +
            "    <column name='Some_ID'\n" +
            "            type='VARCHAR'\n" +
            "            size='16'\n" +
            "            description='The foreign key'/>\n" +
            "    <column name='Some_Value'\n" +
            "            type='DOUBLE'/>\n" +
            "    <foreign-key foreignTable='SomeTable'>\n" +
            "       <reference local='Some_ID' foreign='ID'/>\n" +
            "    </foreign-key>\n" +
            "    <foreign-key foreignTable='SomeTable'>\n" +
            "       <reference local='Some_Value' foreign='VALUE2'/>\n" +
            "    </foreign-key>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(2, model.getTableCount());

        Table someTable = model.getTable(0);

        assertEquals("SomeTable", "Some table", 3, 1, 0, 0, 0,
                     someTable);
        assertEquals("ID", Types.VARCHAR, 16, 0, null, "The primary key", null, true, true, false,
                     someTable.getColumn(0));

        Table anotherTable = model.getTable(1);

        assertEquals("AnotherTable", "And another table", 2, 0, 0, 2, 0,
                     anotherTable);
        assertEquals("Some_ID", Types.VARCHAR, 16, 0, null, "The foreign key", null, false, false, false,
                     anotherTable.getColumn(0));

        ForeignKey fk = anotherTable.getForeignKey(0);

        assertEquals(null, CascadeActionEnum.NONE, CascadeActionEnum.NONE, someTable, 1, fk);
        assertEquals(anotherTable.getColumn(0), someTable.getColumn(0), fk.getReference(0));

        fk = anotherTable.getForeignKey(1);

        assertEquals(null, CascadeActionEnum.NONE, CascadeActionEnum.NONE, someTable, 1, fk);
        assertEquals(anotherTable.getColumn(1), someTable.getColumn(2), fk.getReference(0));

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"SomeTable\" description=\"Some table\">\n" +
            "    <column name=\"ID\" primaryKey=\"true\" required=\"true\" type=\"VARCHAR\" size=\"16\" autoIncrement=\"false\" description=\"The primary key\" />\n" +
            "    <column name=\"VALUE1\" primaryKey=\"false\" required=\"false\" type=\"INTEGER\" autoIncrement=\"false\" description=\"A value\" />\n" +
            "    <column name=\"VALUE2\" primaryKey=\"false\" required=\"false\" type=\"DOUBLE\" autoIncrement=\"false\" description=\"Another value\" />\n" +
            "  </table>\n" +
            "  <table name=\"AnotherTable\" description=\"And another table\">\n" +
            "    <column name=\"Some_ID\" primaryKey=\"false\" required=\"false\" type=\"VARCHAR\" size=\"16\" autoIncrement=\"false\" description=\"The foreign key\" />\n" +
            "    <column name=\"Some_Value\" primaryKey=\"false\" required=\"false\" type=\"DOUBLE\" autoIncrement=\"false\" />\n" +
            "    <foreign-key foreignTable=\"SomeTable\">\n" +
            "      <reference local=\"Some_ID\" foreign=\"ID\" />\n" +
            "    </foreign-key>\n" +
            "    <foreign-key foreignTable=\"SomeTable\">\n" +
            "      <reference local=\"Some_Value\" foreign=\"VALUE2\" />\n" +
            "    </foreign-key>\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests a database model containing two foreignkeys with the same name.
     */
    public void testTwoForeignkeysWithSameName() throws Exception
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='SomeTable'\n" +
                "         description='Some table'>\n" +
                "    <column name='ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            primaryKey='true'\n" +
                "            required='true'\n" +
                "            description='The primary key'/>\n" +
                "    <column name='VALUE1'\n" +
                "            type='INTEGER'\n" +
                "            required='false'\n" +
                "            description='A value'/>\n" +
                "    <column name='VALUE2'\n" +
                "            type='DOUBLE'\n" +
                "            required='false'\n" +
                "            description='Another value'/>\n" +
                "  </table>\n" +
                "  <table name='AnotherTable'\n" +
                "         description='And another table'>\n" +
                "    <column name='Some_ID'\n" +
                "            type='VARCHAR'\n" +
                "            size='16'\n" +
                "            description='The foreign key'/>\n" +
                "    <column name='Some_Value'\n" +
                "            type='DOUBLE'/>\n" +
                "    <foreign-key name='The foreignkey' foreignTable='SomeTable'>\n" +
                "       <reference local='Some_ID' foreign='ID'/>\n" +
                "    </foreign-key>\n" +
                "    <foreign-key name='The foreignkey' foreignTable='SomeTable'>\n" +
                "       <reference local='Some_Value' foreign='VALUE2'/>\n" +
                "    </foreign-key>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (ModelException ex)
        {}
    }

    /**
     * Tests a database model with an index.
     */
    public void testSingleIndex() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableWithIndex'>\n" +
            "    <column name='id'\n" +
            "            type='DOUBLE'\n" +
            "            primaryKey='true'\n" +
            "            required='true'/>\n" +
            "    <column name='value'\n" +
            "            type='SMALLINT'\n" +
            "            default='1'/>\n" +
            "    <index>\n" +
            "      <index-column name='value'/>\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(1, model.getTableCount());
        
        Table table = model.getTable(0);

        assertEquals("TableWithIndex", null, 2, 1, 0, 0, 1,
                     table);
        assertEquals("id", Types.DOUBLE, 0, 0, null, null, null, true, true, false,
                     table.getColumn(0));
        assertEquals("value", Types.SMALLINT, 0, 0, "1", null, null, false, false, false,
                     table.getColumn(1));

        Index index = table.getIndex(0);

        assertEquals(null, false, 1, index);
        assertEquals(table.getColumn(1), null, index.getColumn(0));

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"TableWithIndex\">\n" +
            "    <column name=\"id\" primaryKey=\"true\" required=\"true\" type=\"DOUBLE\" autoIncrement=\"false\" />\n" +
            "    <column name=\"value\" primaryKey=\"false\" required=\"false\" type=\"SMALLINT\" default=\"1\" autoIncrement=\"false\" />\n" +
            "    <index>\n" +
            "      <index-column name=\"value\" />\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests a database model with an index with two columns.
     */
    public void testIndexWithTwoColumns() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableWithIndex'>\n" +
            "    <column name='id'\n" +
            "            type='DOUBLE'\n" +
            "            primaryKey='true'\n" +
            "            required='true'/>\n" +
            "    <column name='when'\n" +
            "            type='TIMESTAMP'\n" +
            "            required='true'/>\n" +
            "    <column name='value'\n" +
            "            type='SMALLINT'\n" +
            "            default='1'/>\n" +
            "    <index>\n" +
            "      <index-column name='when'/>\n" +
            "      <index-column name='id'/>\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(1, model.getTableCount());
        
        Table table = model.getTable(0);

        assertEquals("TableWithIndex", null, 3, 1, 0, 0, 1,
                     table);
        assertEquals("id", Types.DOUBLE, 0, 0, null, null, null, true, true, false,
                     table.getColumn(0));
        assertEquals("when", Types.TIMESTAMP, 0, 0, null, null, null, false, true, false,
                     table.getColumn(1));
        assertEquals("value", Types.SMALLINT, 0, 0, "1", null, null, false, false, false,
                     table.getColumn(2));

        Index index = table.getIndex(0);

        assertEquals(null, false, 2, index);
        assertEquals(table.getColumn(1), null, index.getColumn(0));
        assertEquals(table.getColumn(0), null, index.getColumn(1));

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"TableWithIndex\">\n" +
            "    <column name=\"id\" primaryKey=\"true\" required=\"true\" type=\"DOUBLE\" autoIncrement=\"false\" />\n" +
            "    <column name=\"when\" primaryKey=\"false\" required=\"true\" type=\"TIMESTAMP\" autoIncrement=\"false\" />\n" +
            "    <column name=\"value\" primaryKey=\"false\" required=\"false\" type=\"SMALLINT\" default=\"1\" autoIncrement=\"false\" />\n" +
            "    <index>\n" +
            "      <index-column name=\"when\" />\n" +
            "      <index-column name=\"id\" />\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests a database model with an index with a name.
     */
    public void testIndexWithName() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableWithIndex'>\n" +
            "    <column name='id'\n" +
            "            type='DOUBLE'\n" +
            "            primaryKey='true'\n" +
            "            required='true'/>\n" +
            "    <column name='value'\n" +
            "            type='SMALLINT'\n" +
            "            default='1'/>\n" +
            "    <index name='The Index'>\n" +
            "      <index-column name='value'/>\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(1, model.getTableCount());
        
        Table table = model.getTable(0);

        assertEquals("TableWithIndex", null, 2, 1, 0, 0, 1,
                     table);
        assertEquals("id", Types.DOUBLE, 0, 0, null, null, null, true, true, false,
                     table.getColumn(0));
        assertEquals("value", Types.SMALLINT, 0, 0, "1", null, null, false, false, false,
                     table.getColumn(1));

        Index index = table.getIndex(0);

        assertEquals("The Index", false, 1, index);
        assertEquals(table.getColumn(1), null, index.getColumn(0));

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"TableWithIndex\">\n" +
            "    <column name=\"id\" primaryKey=\"true\" required=\"true\" type=\"DOUBLE\" autoIncrement=\"false\" />\n" +
            "    <column name=\"value\" primaryKey=\"false\" required=\"false\" type=\"SMALLINT\" default=\"1\" autoIncrement=\"false\" />\n" +
            "    <index name=\"The Index\">\n" +
            "      <index-column name=\"value\" />\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests a database model with an index without index columns.
     */
    public void testIndexWithoutColumns() throws Exception
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='TableWithIndex'>\n" +
                "    <column name='id'\n" +
                "            type='DOUBLE'\n" +
                "            primaryKey='true'\n" +
                "            required='true'/>\n" +
                "    <column name='value'\n" +
                "            type='SMALLINT'\n" +
                "            default='1'/>\n" +
                "    <index>\n" +
                "    </index>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (DdlUtilsXMLException ex)
        {}
    }

    /**
     * Tests a database model with an index with an index column that references an undefined column.
     */
    public void testIndexWithUndefinedColumns() throws Exception
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='TableWithIndex'>\n" +
                "    <column name='id'\n" +
                "            type='DOUBLE'\n" +
                "            primaryKey='true'\n" +
                "            required='true'/>\n" +
                "    <column name='value'\n" +
                "            type='SMALLINT'\n" +
                "            default='1'/>\n" +
                "    <index>\n" +
                "      <index-column name='theValue'/>\n" +
                "    </index>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (ModelException ex)
        {}
    }

    /**
     * Tests a database model with an index with an index column that has no name.
     */
    public void testIndexWithNoNameColumn() throws Exception
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='TableWithIndex'>\n" +
                "    <column name='id'\n" +
                "            type='DOUBLE'\n" +
                "            primaryKey='true'\n" +
                "            required='true'/>\n" +
                "    <column name='value'\n" +
                "            type='SMALLINT'\n" +
                "            default='1'/>\n" +
                "    <index>\n" +
                "      <index-column/>\n" +
                "    </index>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (DdlUtilsXMLException ex)
        {}
    }


    /**
     * Tests a database model with an unique index.
     */
    public void testSingleUniqueIndex() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableWithIndex'>\n" +
            "    <column name='id'\n" +
            "            type='DOUBLE'\n" +
            "            primaryKey='true'\n" +
            "            required='true'/>\n" +
            "    <column name='value'\n" +
            "            type='SMALLINT'\n" +
            "            default='1'/>\n" +
            "    <unique>\n" +
            "      <unique-column name='value'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(1, model.getTableCount());
        
        Table table = model.getTable(0);

        assertEquals("TableWithIndex", null, 2, 1, 0, 0, 1,
                     table);
        assertEquals("id", Types.DOUBLE, 0, 0, null, null, null, true, true, false,
                     table.getColumn(0));
        assertEquals("value", Types.SMALLINT, 0, 0, "1", null, null, false, false, false,
                     table.getColumn(1));

        Index index = table.getIndex(0);

        assertEquals(null, true, 1, index);
        assertEquals(table.getColumn(1), null, index.getColumn(0));

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"TableWithIndex\">\n" +
            "    <column name=\"id\" primaryKey=\"true\" required=\"true\" type=\"DOUBLE\" autoIncrement=\"false\" />\n" +
            "    <column name=\"value\" primaryKey=\"false\" required=\"false\" type=\"SMALLINT\" default=\"1\" autoIncrement=\"false\" />\n" +
            "    <unique>\n" +
            "      <unique-column name=\"value\" />\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests a database model with an unique index with two columns.
     */
    public void testUniqueIndexWithTwoColumns() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableWithIndex'>\n" +
            "    <column name='id'\n" +
            "            type='DOUBLE'\n" +
            "            primaryKey='true'\n" +
            "            required='true'/>\n" +
            "    <column name='when'\n" +
            "            type='TIMESTAMP'\n" +
            "            required='true'/>\n" +
            "    <column name='value'\n" +
            "            type='SMALLINT'\n" +
            "            default='1'/>\n" +
            "    <unique>\n" +
            "      <unique-column name='when'/>\n" +
            "      <unique-column name='id'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(1, model.getTableCount());
        
        Table table = model.getTable(0);

        assertEquals("TableWithIndex", null, 3, 1, 0, 0, 1,
                     table);
        assertEquals("id", Types.DOUBLE, 0, 0, null, null, null, true, true, false,
                     table.getColumn(0));
        assertEquals("when", Types.TIMESTAMP, 0, 0, null, null, null, false, true, false,
                     table.getColumn(1));
        assertEquals("value", Types.SMALLINT, 0, 0, "1", null, null, false, false, false,
                     table.getColumn(2));

        Index index = table.getIndex(0);

        assertEquals(null, true, 2, index);
        assertEquals(table.getColumn(1), null, index.getColumn(0));
        assertEquals(table.getColumn(0), null, index.getColumn(1));

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"TableWithIndex\">\n" +
            "    <column name=\"id\" primaryKey=\"true\" required=\"true\" type=\"DOUBLE\" autoIncrement=\"false\" />\n" +
            "    <column name=\"when\" primaryKey=\"false\" required=\"true\" type=\"TIMESTAMP\" autoIncrement=\"false\" />\n" +
            "    <column name=\"value\" primaryKey=\"false\" required=\"false\" type=\"SMALLINT\" default=\"1\" autoIncrement=\"false\" />\n" +
            "    <unique>\n" +
            "      <unique-column name=\"when\" />\n" +
            "      <unique-column name=\"id\" />\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests a database model with an unique index with a name.
     */
    public void testUniqueIndexWithName() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableWithIndex'>\n" +
            "    <column name='id'\n" +
            "            type='DOUBLE'\n" +
            "            primaryKey='true'\n" +
            "            required='true'/>\n" +
            "    <column name='value'\n" +
            "            type='SMALLINT'\n" +
            "            default='1'/>\n" +
            "    <unique name='The Index'>\n" +
            "      <unique-column name='value'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(1, model.getTableCount());
        
        Table table = model.getTable(0);

        assertEquals("TableWithIndex", null, 2, 1, 0, 0, 1,
                     table);
        assertEquals("id", Types.DOUBLE, 0, 0, null, null, null, true, true, false,
                     table.getColumn(0));
        assertEquals("value", Types.SMALLINT, 0, 0, "1", null, null, false, false, false,
                     table.getColumn(1));

        Index index = table.getIndex(0);

        assertEquals("The Index", true, 1, index);
        assertEquals(table.getColumn(1), null, index.getColumn(0));

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"TableWithIndex\">\n" +
            "    <column name=\"id\" primaryKey=\"true\" required=\"true\" type=\"DOUBLE\" autoIncrement=\"false\" />\n" +
            "    <column name=\"value\" primaryKey=\"false\" required=\"false\" type=\"SMALLINT\" default=\"1\" autoIncrement=\"false\" />\n" +
            "    <unique name=\"The Index\">\n" +
            "      <unique-column name=\"value\" />\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests a database model with an unique index without index columns.
     */
    public void testUniqueIndexWithoutColumns() throws Exception
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='TableWithIndex'>\n" +
                "    <column name='id'\n" +
                "            type='DOUBLE'\n" +
                "            primaryKey='true'\n" +
                "            required='true'/>\n" +
                "    <column name='value'\n" +
                "            type='SMALLINT'\n" +
                "            default='1'/>\n" +
                "    <unique>\n" +
                "    </unique>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (DdlUtilsXMLException ex)
        {}
    }

    /**
     * Tests a database model with an unique index with an index column that references an undefined column.
     */
    public void testUniqueIndexWithUndefinedColumns() throws Exception
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='TableWithIndex'>\n" +
                "    <column name='id'\n" +
                "            type='DOUBLE'\n" +
                "            primaryKey='true'\n" +
                "            required='true'/>\n" +
                "    <column name='value'\n" +
                "            type='SMALLINT'\n" +
                "            default='1'/>\n" +
                "    <unique>\n" +
                "      <unique-column name='theValue'/>\n" +
                "    </unique>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (ModelException ex)
        {}
    }

    /**
     * Tests a database model with an unique index with an index column that has no name.
     */
    public void testUniqueIndexWithNoNameColumn() throws Exception
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='TableWithIndex'>\n" +
                "    <column name='id'\n" +
                "            type='DOUBLE'\n" +
                "            primaryKey='true'\n" +
                "            required='true'/>\n" +
                "    <column name='value'\n" +
                "            type='SMALLINT'\n" +
                "            default='1'/>\n" +
                "    <unique>\n" +
                "      <unique-column/>\n" +
                "    </unique>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (DdlUtilsXMLException ex)
        {}
    }

    /**
     * Tests a database model with indices, both uniques and non-uniques.
     */
    public void testMixedIndexes() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='TableWithIndexes'>\n" +
            "    <column name='id'\n" +
            "            type='SMALLINT'\n" +
            "            primaryKey='false'\n" +
            "            required='true'\n" +
            "            autoIncrement='true'/>\n" +
            "    <column name='when'\n" +
            "            type='DATE'/>\n" +
            "    <unique name='important column'>\n" +
            "      <unique-column name='id'/>\n" +
            "    </unique>\n" +
            "    <index>\n" +
            "      <index-column name='when'/>\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(1, model.getTableCount());
        
        Table table = model.getTable(0);

        assertEquals("TableWithIndexes", null, 2, 0, 1, 0, 2,
                     table);
        assertEquals("id", Types.SMALLINT, 0, 0, null, null, null, false, true, true,
                     table.getColumn(0));
        assertEquals("when", Types.DATE, 0, 0, null, null, null, false, false, false,
                     table.getColumn(1));
        assertEquals(table.getColumn(0), table.getAutoIncrementColumns()[0]);

        Index index = table.getIndex(0);

        assertEquals("important column", true, 1, index);
        assertEquals(table.getColumn(0), null, index.getColumn(0));

        index = table.getIndex(1);

        assertEquals(null, false, 1, index);
        assertEquals(table.getColumn(1), null, index.getColumn(0));

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"TableWithIndexes\">\n" +
            "    <column name=\"id\" primaryKey=\"false\" required=\"true\" type=\"SMALLINT\" autoIncrement=\"true\" />\n" +
            "    <column name=\"when\" primaryKey=\"false\" required=\"false\" type=\"DATE\" autoIncrement=\"false\" />\n" +
            "    <unique name=\"important column\">\n" +
            "      <unique-column name=\"id\" />\n" +
            "    </unique>\n" +
            "    <index>\n" +
            "      <index-column name=\"when\" />\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests a complex database model with multiple tables, foreign keys, indices and uniques.
     */
    public void testComplex() throws Exception
    {
        // A = id:INTEGER, parentId:INTEGER, name:VARCHAR(32); fk 'parent' -> A (parentId -> id), unique(name)
        // B = id:TIMESTAMP, aid:INTEGER, cid:CHAR(32) fk -> A (aid -> id), fk -> C (cid -> id), index(aid,cid)
        // C = id:CHAR(32), text:LONGVARCHAR; index 'byText' (text)
        
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='A'\n" +
            "         description='Table A'>\n" +
            "    <column name='id'\n" +
            "            type='INTEGER'\n" +
            "            autoIncrement='true'\n" +
            "            primaryKey='true'\n" +
            "            required='true'\n" +
            "            description='The primary key of table A'/>\n" +
            "    <column name='parentId'\n" +
            "            type='INTEGER'\n" +
            "            description='The field for the foreign key parent'/>\n" +
            "    <column name='name'\n" +
            "            type='VARCHAR'\n" +
            "            size='32'\n" +
            "            required='true'\n" +
            "            description='The name'/>\n" +
            "    <foreign-key name='parent' foreignTable='A'>\n" +
            "       <reference local='parentId' foreign='id'/>\n" +
            "    </foreign-key>\n" +
            "    <unique>\n" +
            "      <unique-column name='name'/>\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "  <table name='B'\n" +
            "         description='Table B'>\n" +
            "    <column name='id'\n" +
            "            type='TIMESTAMP'\n" +
            "            primaryKey='true'\n" +
            "            required='true'\n" +
            "            description='The primary key of table B'/>\n" +
            "    <column name='aid'\n" +
            "            type='INTEGER'\n" +
            "            description='The field for the foreign key towards A'/>\n" +
            "    <column name='cid'\n" +
            "            type='CHAR'\n" +
            "            size='32'\n" +
            "            description='The field for the foreign key towards C'/>\n" +
            "    <foreign-key foreignTable='A'>\n" +
            "       <reference local='aid' foreign='id'/>\n" +
            "    </foreign-key>\n" +
            "    <foreign-key foreignTable='C'>\n" +
            "       <reference local='cid' foreign='id'/>\n" +
            "    </foreign-key>\n" +
            "    <index>\n" +
            "      <index-column name='aid'/>\n" +
            "      <index-column name='cid'/>\n" +
            "    </index>\n" +
            "  </table>\n" +
            "  <table name='C'\n" +
            "         description='Table C'>\n" +
            "    <column name='id'\n" +
            "            type='CHAR'\n" +
            "            size='32'\n" +
            "            primaryKey='true'\n" +
            "            required='true'\n" +
            "            description='The primary key of table C'/>\n" +
            "    <column name='text'\n" +
            "            type='LONGVARCHAR'\n" +
            "            description='The text'/>\n" +
            "    <index name='byText'>\n" +
            "      <index-column name='text'/>\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(3, model.getTableCount());

        // table A

        Table table = model.getTable(0);

        assertEquals("A", "Table A", 3, 1, 1, 1, 1,
                     table);
        assertEquals("id", Types.INTEGER, 0, 0, null, "The primary key of table A", null, true, true, true,
                     table.getColumn(0));
        assertEquals("parentId", Types.INTEGER, 0, 0, null, "The field for the foreign key parent", null, false, false, false,
                     table.getColumn(1));
        assertEquals("name", Types.VARCHAR, 32, 0, null, "The name", null, false, true, false,
                     table.getColumn(2));
        assertEquals(table.getColumn(0), table.getAutoIncrementColumns()[0]);

        ForeignKey fk = table.getForeignKey(0);

        assertEquals("parent", CascadeActionEnum.NONE, CascadeActionEnum.NONE, table, 1, fk);
        assertEquals(table.getColumn(1), table.getColumn(0), fk.getFirstReference());

        Index index = table.getIndex(0);

        assertEquals(null, true, 1, index);
        assertEquals(table.getColumn(2), null, index.getColumn(0));

        // table B
        
        table = model.getTable(1);

        assertEquals("B", "Table B", 3, 1, 0, 2, 1,
                     table);
        assertEquals("id", Types.TIMESTAMP, 0, 0, null, "The primary key of table B", null, true, true, false,
                     table.getColumn(0));
        assertEquals("aid", Types.INTEGER, 0, 0, null, "The field for the foreign key towards A", null, false, false, false,
                     table.getColumn(1));
        assertEquals("cid", Types.CHAR, 32, 0, null, "The field for the foreign key towards C", null, false, false, false,
                     table.getColumn(2));

        fk = table.getForeignKey(0);

        assertEquals(null, CascadeActionEnum.NONE, CascadeActionEnum.NONE, model.getTable(0), 1, fk);
        assertEquals(table.getColumn(1), model.getTable(0).getColumn(0), fk.getFirstReference());

        fk = table.getForeignKey(1);

        assertEquals(null, CascadeActionEnum.NONE, CascadeActionEnum.NONE, model.getTable(2), 1, fk);
        assertEquals(table.getColumn(2), model.getTable(2).getColumn(0), fk.getFirstReference());

        index = table.getIndex(0);

        assertEquals(null, false, 2, index);
        assertEquals(table.getColumn(1), null, index.getColumn(0));
        assertEquals(table.getColumn(2), null, index.getColumn(1));

        // table C

        table = model.getTable(2);

        assertEquals("C", "Table C", 2, 1, 0, 0, 1,
                     table);
        assertEquals("id", Types.CHAR, 32, 0, null, "The primary key of table C", null, true, true, false,
                     table.getColumn(0));
        assertEquals("text", Types.LONGVARCHAR, 0, 0, null, "The text", null, false, false, false,
                     table.getColumn(1));

        index = table.getIndex(0);

        assertEquals("byText", false, 1, index);
        assertEquals(table.getColumn(1), null, index.getColumn(0));

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"A\" description=\"Table A\">\n" +
            "    <column name=\"id\" primaryKey=\"true\" required=\"true\" type=\"INTEGER\" autoIncrement=\"true\" description=\"The primary key of table A\" />\n" +
            "    <column name=\"parentId\" primaryKey=\"false\" required=\"false\" type=\"INTEGER\" autoIncrement=\"false\" description=\"The field for the foreign key parent\" />\n" +
            "    <column name=\"name\" primaryKey=\"false\" required=\"true\" type=\"VARCHAR\" size=\"32\" autoIncrement=\"false\" description=\"The name\" />\n" +
            "    <foreign-key foreignTable=\"A\" name=\"parent\">\n" +
            "      <reference local=\"parentId\" foreign=\"id\" />\n" +
            "    </foreign-key>\n" +
            "    <unique>\n" +
            "      <unique-column name=\"name\" />\n" +
            "    </unique>\n" +
            "  </table>\n" +
            "  <table name=\"B\" description=\"Table B\">\n" +
            "    <column name=\"id\" primaryKey=\"true\" required=\"true\" type=\"TIMESTAMP\" autoIncrement=\"false\" description=\"The primary key of table B\" />\n" +
            "    <column name=\"aid\" primaryKey=\"false\" required=\"false\" type=\"INTEGER\" autoIncrement=\"false\" description=\"The field for the foreign key towards A\" />\n" +
            "    <column name=\"cid\" primaryKey=\"false\" required=\"false\" type=\"CHAR\" size=\"32\" autoIncrement=\"false\" description=\"The field for the foreign key towards C\" />\n" +
            "    <foreign-key foreignTable=\"A\">\n" +
            "      <reference local=\"aid\" foreign=\"id\" />\n" +
            "    </foreign-key>\n" +
            "    <foreign-key foreignTable=\"C\">\n" +
            "      <reference local=\"cid\" foreign=\"id\" />\n" +
            "    </foreign-key>\n" +
            "    <index>\n" +
            "      <index-column name=\"aid\" />\n" +
            "      <index-column name=\"cid\" />\n" +
            "    </index>\n" +
            "  </table>\n" +
            "  <table name=\"C\" description=\"Table C\">\n" +
            "    <column name=\"id\" primaryKey=\"true\" required=\"true\" type=\"CHAR\" size=\"32\" autoIncrement=\"false\" description=\"The primary key of table C\" />\n" +
            "    <column name=\"text\" primaryKey=\"false\" required=\"false\" type=\"LONGVARCHAR\" autoIncrement=\"false\" description=\"The text\" />\n" +
            "    <index name=\"byText\">\n" +
            "      <index-column name=\"text\" />\n" +
            "    </index>\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }

    /**
     * Tests that an exception is generated when an index references an undefined column.
     */
    public void testUndefinedIndexColumn()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='TableWidthIndex'>\n" +
                "    <column name='id'\n" +
                "            type='INTEGER'\n" +
                "            primaryKey='true'\n" +
                "            required='true'/>\n" +
                "    <index name='test index'>\n" +
                "      <index-column name='id'/>\n" +
                "      <index-column name='value'/>\n" +
                "    </index>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (ModelException ex)
        {}
    }

    /**
     * Tests that an exception is generated when two table elements have the same value in their name attributes.
     */
    public void testTwoTablesWithTheSameName()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='TestTable'>\n" +
                "    <column name='id1'\n" +
                "            type='INTEGER'\n" +
                "            primaryKey='true'\n" +
                "            required='true'/>\n" +
                "  </table>\n" +
                "  <table name='TestTable'>\n" +
                "    <column name='id2'\n" +
                "            type='INTEGER'\n" +
                "            primaryKey='true'\n" +
                "            required='true'/>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (ModelException ex)
        {}
    }

    /**
     * Tests that an exception is generated when two column elements within the same table
     * element have the same value in their name attributes.
     */
    public void testTwoColumnsWithTheSameName()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='TestTable'>\n" +
                "    <column name='id'\n" +
                "            type='INTEGER'\n" +
                "            primaryKey='true'\n" +
                "            required='true'/>\n" +
                "    <column name='id'\n" +
                "            type='VARCHAR'/>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (ModelException ex)
        {}
    }

    /**
     * Tests that an exception is generated when the a unique index references an undefined column.
     */
    public void testUndefinedUniqueColumn()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='TableWidthUnique'>\n" +
                "    <column name='id'\n" +
                "            type='INTEGER'\n" +
                "            primaryKey='true'\n" +
                "            required='true'/>\n" +
                "    <unique>\n" +
                "      <unique-column name='value'/>\n" +
                "    </unique>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (ModelException ex)
        {}
    }

    /**
     * Tests that an exception is generated when two indices have the same value in their name attributes.
     */
    public void testTwoIndicesWithTheSameName()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='TableWidthIndex'>\n" +
                "    <column name='id'\n" +
                "            type='INTEGER'\n" +
                "            primaryKey='true'\n" +
                "            required='true'/>\n" +
                "    <column name='value1'\n" +
                "            type='INTEGER'\n" +
                "            required='true'/>\n" +
                "    <column name='value2'\n" +
                "            type='INTEGER'\n" +
                "            required='true'/>\n" +
                "    <index name='the index'>\n" +
                "      <index-column name='value1'/>\n" +
                "    </index>\n" +
                "    <index name='the index'>\n" +
                "      <index-column name='value2'/>\n" +
                "    </index>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (ModelException ex)
        {}
    }

    /**
     * Tests that an exception is generated when two unique indices have the
     * same value in their name attributes.
     */
    public void testTwoUniqueIndicesWithTheSameName()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='TableWidthUnique'>\n" +
                "    <column name='id'\n" +
                "            type='INTEGER'\n" +
                "            primaryKey='true'\n" +
                "            required='true'/>\n" +
                "    <column name='value1'\n" +
                "            type='INTEGER'\n" +
                "            required='true'/>\n" +
                "    <column name='value2'\n" +
                "            type='INTEGER'\n" +
                "            required='true'/>\n" +
                "    <unique name='the unique'>\n" +
                "      <unique-column name='value1'/>\n" +
                "    </unique>\n" +
                "    <unique name='the unique'>\n" +
                "      <unique-column name='value2'/>\n" +
                "    </unique>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (ModelException ex)
        {}
    }

    /**
     * Tests that an exception is generated when a unique and a normal index
     * have the same value in their name attributes.
     */
    public void testUniqueAndNormalIndexWithTheSameName()
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                "  <table name='TableWidthUnique'>\n" +
                "    <column name='id'\n" +
                "            type='INTEGER'\n" +
                "            primaryKey='true'\n" +
                "            required='true'/>\n" +
                "    <column name='value1'\n" +
                "            type='INTEGER'\n" +
                "            required='true'/>\n" +
                "    <column name='value2'\n" +
                "            type='INTEGER'\n" +
                "            required='true'/>\n" +
                "    <index name='test'>\n" +
                "      <index-column name='value1'/>\n" +
                "    </index>\n" +
                "    <unique name='test'>\n" +
                "      <unique-column name='value2'/>\n" +
                "    </unique>\n" +
                "  </table>\n" +
                "</database>");

            fail();
        }
        catch (ModelException ex)
        {}
    }

    /**
     * Regression test ensuring that wrong XML is not read (regarding betwixt issue #37369).
     */
    public void testFaultReadOfTable() 
    {
        try
        {
            readModel(
                "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='db' >\n" +
                "  <index name='NotATable'/>\n" +
                "</database>");

            fail();
        }
        catch (DdlUtilsXMLException ex)
        {}
    }

    /**
     * Tests the Torque/Turbine extensions BOOLEANINT & BOOLEANCHAR.
     */
    public void testTurbineExtension() throws Exception
    {
        Database model = readModel(
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
            "  <table name='SomeTable'>\n" +
            "    <column name='intField'\n" +
            "            type='BOOLEANINT'/>\n" +
            "    <column name='charField'\n" +
            "            type='BOOLEANCHAR' />\n" +
            "  </table>\n" +
            "</database>");

        assertEquals("test", model.getName());
        assertEquals(1, model.getTableCount());
        
        Table table = model.getTable(0);

        assertEquals("SomeTable", null, 2, 0, 0, 0, 0,
                     table);
        assertEquals("intField", Types.TINYINT, 0, 0, null, null, null, false, false, false,
                     table.getColumn(0));
        assertEquals("charField", Types.CHAR, 0, 0, null, null, null, false, false, false,
                     table.getColumn(1));

        assertEquals(
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<database xmlns=\"" + DatabaseIO.DDLUTILS_NAMESPACE + "\" name=\"test\">\n" +
            "  <table name=\"SomeTable\">\n" +
            "    <column name=\"intField\" primaryKey=\"false\" required=\"false\" type=\"TINYINT\" autoIncrement=\"false\" />\n" +
            "    <column name=\"charField\" primaryKey=\"false\" required=\"false\" type=\"CHAR\" autoIncrement=\"false\" />\n" +
            "  </table>\n" +
            "</database>\n",
            model);
    }
}
