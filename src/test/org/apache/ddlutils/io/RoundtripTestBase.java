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
import java.sql.Types;
import java.util.List;

import org.apache.commons.beanutils.DynaBean;
import org.apache.ddlutils.TestDatabaseWriterBase;
import org.apache.ddlutils.dynabean.SqlDynaBean;
import org.apache.ddlutils.dynabean.SqlDynaClass;
import org.apache.ddlutils.dynabean.SqlDynaProperty;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
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
                    if (getPlatformInfo().isReturningDefaultValueForPrimaryKeys() &&
                        (column.getDefaultValue() == null) && column.isPrimaryKey())
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
     * Compares the two given database models, and if they are not equal, writes both of them
     * in XML form to <code>stderr</code>.
     * 
     * @param expected The expected model
     * @param actual   The actual model
     */
    protected void assertEquals(Database expected, Database actual) throws RuntimeException
    {
        try
        {
            assertEquals((Object)expected, (Object)actual);
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
                throw new RuntimeException(ex);
            }
        }
    }
}
