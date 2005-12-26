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

import java.io.StringWriter;
import java.util.List;

import org.apache.commons.beanutils.DynaBean;
import org.apache.ddlutils.TestDatabaseWriterBase;
import org.apache.ddlutils.dynabean.SqlDynaBean;
import org.apache.ddlutils.dynabean.SqlDynaClass;
import org.apache.ddlutils.dynabean.SqlDynaProperty;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.TypeMap;
import org.apache.ddlutils.model.UniqueIndex;
import org.apache.ddlutils.platform.DefaultValueHelper;

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
    /** Test model with a simple TINYINT column. */
    protected static final String TEST_TINYINT_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='TINYINT'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a TINYINT column with a default value. */
    protected static final String TEST_TINYINT_MODEL_WITH_DEFAULT = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='TINYINT' required='true' default='-200'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple SMALLINT column. */
    protected static final String TEST_SMALLINT_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='SMALLINT'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a SMALLINT column with a default value. */
    protected static final String TEST_SMALLINT_MODEL_WITH_DEFAULT = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='SMALLINT' required='true' default='-32768'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple INTEGER column. */
    protected static final String TEST_INTEGER_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='INTEGER'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a INTEGER column with a default value. */
    protected static final String TEST_INTEGER_MODEL_WITH_DEFAULT = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='INTEGER' required='true' default='2147483647'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple BIGINT column. */
    protected static final String TEST_BIGINT_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='BIGINT'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a BIGINT column with a default value. */
    protected static final String TEST_BIGINT_MODEL_WITH_DEFAULT = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='BIGINT' required='true' default='-9223372036854775808'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple REAL column. */
    protected static final String TEST_REAL_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='REAL'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a REAL column with a default value. */
    protected static final String TEST_REAL_MODEL_WITH_DEFAULT = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='REAL' required='true' default='-1.0123456'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple FLOAT column. */
    protected static final String TEST_FLOAT_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='FLOAT'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a FLOAT column with a default value. */
    protected static final String TEST_FLOAT_MODEL_WITH_DEFAULT = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='FLOAT' required='true' default='1234567890.012345678901234'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple DOUBLE column. */
    protected static final String TEST_DOUBLE_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='DOUBLE'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a DOUBLE column with a default value. */
    protected static final String TEST_DOUBLE_MODEL_WITH_DEFAULT = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='DOUBLE' required='true' default='-9876543210.987654321098765'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple DECIMAL column. */
    protected static final String TEST_DECIMAL_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='DECIMAL' size='15'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a DECIMAL column with a default value. */
    protected static final String TEST_DECIMAL_MODEL_WITH_DEFAULT = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='DECIMAL' size='15' required='true' default='123456789012345'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple DECIMAL column with a scale. */
    protected static final String TEST_DECIMAL_MODEL_WITH_SCALE = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='DECIMAL' size='15,7'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a DECIMAL column with a scale and default value. */
    protected static final String TEST_DECIMAL_MODEL_WITH_SCALE_AND_DEFAULT = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='DECIMAL' size='15,7' required='true' default='12345678.7654321'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple NUMERIC column. */
    protected static final String TEST_NUMERIC_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='NUMERIC' size='15'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a NUMERIC column with a default value. */
    protected static final String TEST_NUMERIC_MODEL_WITH_DEFAULT = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='NUMERIC' size='15' required='true' default='-123456789012345'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple NUMERIC column with a scale. */
    protected static final String TEST_NUMERIC_MODEL_WITH_SCALE = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='NUMERIC' size='15,8'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a NUMERIC column with a scale and default value. */
    protected static final String TEST_NUMERIC_MODEL_WITH_SCALE_AND_DEFAULT = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='NUMERIC' size='15,8' required='true' default='-1234567.87654321'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple CHAR column. */
    protected static final String TEST_CHAR_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='CHAR' size='10'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a CHAR column with a default value. */
    protected static final String TEST_CHAR_MODEL_WITH_DEFAULT = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='CHAR' size='15' required='true' default='some value'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple VARCHAR column. */
    protected static final String TEST_VARCHAR_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='VARCHAR' size='20'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a VARCHAR column with a default value. */
    protected static final String TEST_VARCHAR_MODEL_WITH_DEFAULT = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='VARCHAR' required='true' default='some value'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple LONGVARCHAR column. */
    protected static final String TEST_LONGVARCHAR_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='LONGVARCHAR'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a LONGVARCHAR column with a default value. */
    protected static final String TEST_LONGVARCHAR_MODEL_WITH_DEFAULT = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='LONGVARCHAR' required='true' default='some value'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple DATE column. */
    protected static final String TEST_DATE_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='DATE'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a DATE column with a default value. */
    protected static final String TEST_DATE_MODEL_WITH_DEFAULT = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='DATE' required='true' default='2000-01-01'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple TIME column. */
    protected static final String TEST_TIME_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='TIME'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a TIME column with a default value. */
    protected static final String TEST_TIME_MODEL_WITH_DEFAULT = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='TIME' required='true' default='11:27:03'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple TIMESTAMP column. */
    protected static final String TEST_TIMESTAMP_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='TIMESTAMP'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a TIMESTAMP column with a default value. */
    protected static final String TEST_TIMESTAMP_MODEL_WITH_DEFAULT = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='TIMESTAMP' required='true' default='1985-06-17 16:17:18.0'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple BINARY column. */
    protected static final String TEST_BINARY_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='BINARY'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple VARBINARY column. */
    protected static final String TEST_VARBINARY_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='VARBINARY'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple LONGVARBINARY column. */
    protected static final String TEST_LONGVARBINARY_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='LONGVARBINARY'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple BLOB column. */
    protected static final String TEST_BLOB_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='BLOB'/>\n"+
        "  </table>\n"+
        "</database>";
    /** Test model with a simple CLOB column. */
    protected static final String TEST_CLOB_MODEL = 
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
        "<database name='roundtriptest'>\n"+
        "  <table name='ROUNDTRIP'>\n"+
        "    <column name='PK' type='INTEGER' primaryKey='true' required='true'/>\n"+
        "    <column name='VALUE' type='CLOB'/>\n"+
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
     * Specifies whether the platform has unique indices for pks in the model
     * read back from the database.
     * 
     * @return <code>true</code> if there will be unique indices for pks in read-back models
     */
    protected abstract boolean hasPkUniqueIndices();
    
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
                }
            }
            if (hasPkUniqueIndices())
            {
                addPrimaryKeyUniqueIndicesToModel();
            }
            return model;
        }
        catch (CloneNotSupportedException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Compares the attribute value of the given bean with the expected object.
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

    protected void assertEquals(Database expected, Database actual)
    {
        StringWriter writer = new StringWriter();
        DatabaseIO   dbIo   = new DatabaseIO();

        dbIo.write(expected, writer);

        String expectedXml = writer.toString();
        
        writer = new StringWriter();
        dbIo.write(actual, writer);

        String actualXml = writer.toString();

        assertEquals((Object)expected, (Object)actual);
    }

    // special columns (java_object, array, distinct, ...)

    // auto-increment
    // default values
    // null/not null
    // pk (incl. pk with auto-increment)
    // index/unique (incl. for pks)
    // fks (incl. multiple columns, circular references)
}
