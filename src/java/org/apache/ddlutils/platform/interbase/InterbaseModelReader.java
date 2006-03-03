package org.apache.ddlutils.platform.interbase;

/*
 * Copyright 2006 The Apache Software Foundation.
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.DatabaseMetaDataWrapper;
import org.apache.ddlutils.platform.JdbcModelReader;

/**
 * The Jdbc Model Reader for Interbase.
 *
 * @author Thomas Dudziak
 * @version $Revision: $
 */
public class InterbaseModelReader extends JdbcModelReader
{
    /**
     * Creates a new model reader for Interbase databases.
     * 
     * @param platformInfo The platform specific settings
     */
    public InterbaseModelReader(PlatformInfo platformInfo)
    {
        super(platformInfo);
        setDefaultCatalogPattern(null);
        setDefaultSchemaPattern(null);
        setDefaultTablePattern("%");
        setDefaultColumnPattern("%");
    }

    /**
     * {@inheritDoc}
     */
    protected Collection readColumns(DatabaseMetaDataWrapper metaData, String tableName) throws SQLException
    {
        ResultSet columnData = null;

        try
        {
            List columns = new ArrayList();

            if (getPlatformInfo().isUseDelimitedIdentifiers())
            {
                // Jaybird has a problem when delimited identifiers are used as
                // it is not able to find the columns for the table
                // So we have to filter manually below
                columnData = metaData.getColumns(getDefaultTablePattern(), getDefaultColumnPattern());

                while (columnData.next())
                {
                    Map values = readColumns(columnData, getColumnsForColumn());

                    if (tableName.equals(values.get("TABLE_NAME")))
                    {
                        columns.add(readColumn(metaData, values));
                    }
                }
            }
            else
            {
                columnData = metaData.getColumns(tableName, getDefaultColumnPattern());

                while (columnData.next())
                {
                    Map values = readColumns(columnData, getColumnsForColumn());

                    columns.add(readColumn(metaData, values));
                }
            }

            return columns;
        }
        finally
        {
            if (columnData != null)
            {
                columnData.close();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected Column readColumn(DatabaseMetaDataWrapper metaData, Map values) throws SQLException
    {
        Column column = super.readColumn(metaData, values);

        if (column.getTypeCode() == Types.FLOAT)
        {
            column.setTypeCode(Types.REAL);
        }
        return column;
    }

    /**
     * {@inheritDoc}
     */
    protected Collection readPrimaryKeyNames(DatabaseMetaDataWrapper metaData, String tableName) throws SQLException
    {
        List      pks   = new ArrayList();
        ResultSet pkData = null;

        try
        {
            if (getPlatformInfo().isUseDelimitedIdentifiers())
            {
                // Jaybird has a problem when delimited identifiers are used as
                // it is not able to find the primary key info for the table
                // So we have to filter manually below
                pkData = metaData.getPrimaryKeys(getDefaultTablePattern());
                while (pkData.next())
                {
                    Map values = readColumns(pkData, getColumnsForPK());
    
                    if (tableName.equals(values.get("TABLE_NAME")))
                    {
                        pks.add(readPrimaryKeyName(metaData, values));
                    }
                }
            }
            else
            {
                pkData = metaData.getPrimaryKeys(tableName);
                while (pkData.next())
                {
                    Map values = readColumns(pkData, getColumnsForPK());
    
                    pks.add(readPrimaryKeyName(metaData, values));
                }
            }
        }
        finally
        {
            if (pkData != null)
            {
                pkData.close();
            }
        }
        return pks;
    }
    
    /**
     * {@inheritDoc}
     */
    protected boolean isInternalPrimaryKeyIndex(Table table, Index index)
    {
        // Interbase generates an unique index for the pks of the form "RDB$PRIMARY825"
        return index.getName().startsWith("RDB$PRIMARY");
    }
}
