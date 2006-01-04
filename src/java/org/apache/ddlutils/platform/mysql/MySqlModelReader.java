package org.apache.ddlutils.platform.mysql;

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

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.DatabaseMetaDataWrapper;
import org.apache.ddlutils.platform.JdbcModelReader;

/**
 * Reads a database model from a MySql database.
 *
 * @author Martin van den Bemt
 * @version $Revision: $
 */
public class MySqlModelReader extends JdbcModelReader
{
    /**
     * Creates a new model reader for PostgreSql databases.
     * 
     * @param platformInfo The platform specific settings
     */
    public MySqlModelReader(PlatformInfo platformInfo)
    {
        super(platformInfo);
        setDefaultCatalogPattern(null);
        setDefaultSchemaPattern(null);
        setDefaultTablePattern(null);
    }

    /**
     * {@inheritDoc}
     * @todo This needs some more work, since table names can be case sensitive or lowercase
     *       depending on the platform (really cute).
     *       See http://dev.mysql.com/doc/refman/4.1/en/name-case-sensitivity.html for more info.
     */
    protected Table readTable(DatabaseMetaDataWrapper metaData, Map values) throws SQLException
    {
        Table table =  super.readTable(metaData, values);
//        List indexes = new ArrayList();
//        // this could be optimized, we need to check if an index can be named PRIMARY without
//        // being a primary key. Taking the safe path for now.
//        for (int i = 0; i < table.getIndexCount(); i++)
//        {
//            Index index = table.getIndex(i);
//            if ("PRIMARY".equals(index.getName()))
//            {
//                for (int c = 0; c < index.getColumnCount(); c++)
//                {
//                    String columnName = index.getColumn(c).getName();
//                    Column column  = table.findColumn(columnName);
//                    if (column.isPrimaryKey())
//                    {
//                        indexes.add(index);
//                    }
//                }
//            }
//            else
//            {
//                for (int f = 0; f < table.getForeignKeyCount(); f++)
//                {
//                    ForeignKey fk = table.getForeignKey(f);
//                    if (fk.getName().equals(index.getName()))
//                    {
//                        indexes.add(index);
//                    }
//                }
//            }
//        }
//        for (int i = 0; i < indexes.size(); i++)
//        {
//            table.removeIndex((Index) indexes.get(i));
//        }
        return table;
    }

    /**
     * {@inheritDoc}
     */
    protected Column readColumn(DatabaseMetaDataWrapper metaData, Map values) throws SQLException
    {
        Column column = super.readColumn(metaData, values);

        if (column.getTypeCode() == Types.BIT)
        {
            // MySql
        }
//        if ("".equals(column.getDescription()))
//        {
//            column.setDescription(null);
//        }
//        if ("".equals(column.getParsedDefaultValue()))
//        {
//            column.setDefaultValue(null);
//        }
//        if ("auto_increment".equals(column.getDescription()))
//        {
//            column.setAutoIncrement(true);
//        }
//        switch (column.getTypeCode())
//        {
//            case Types.INTEGER:
//                if ("0".equals(column.getDefaultValue()))
//                {
//                    column.setDefaultValue(null);
//                }
//            case Types.DOUBLE:
//                column.setSize(null);
//                break;
//        }
        return column;
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isInternalPrimaryKeyIndex(Table table, Index index)
    {
        // MySql defines a unique index "PRIMARY" for primary keys
        return "PRIMARY".equals(index.getName());
    }


}
