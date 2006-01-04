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
import java.util.Map;

import org.apache.ddlutils.PlatformInfo;
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

        determineAutoIncrementFromResultSetMetaData(table, table.getPrimaryKeyColumns());
        
        return table;
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isInternalPrimaryKeyIndex(Table table, Index index)
    {
        // MySql defines a unique index "PRIMARY" for primary keys
        return "PRIMARY".equals(index.getName());
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isInternalForeignKeyIndex(Table table, ForeignKey fk, Index index)
    {
        // MySql defines a non-unique index of the same name as the fk
        return new MySqlBuilder(getPlatformInfo()).getForeignKeyName(table, fk).equals(index.getName());
    }
}
