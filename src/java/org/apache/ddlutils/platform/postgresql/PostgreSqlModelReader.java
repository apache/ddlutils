package org.apache.ddlutils.platform.postgresql;

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

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.JdbcModelReader;

/**
 * Reads a database model from a PostgreSql database.
 *
 * @author Thomas Dudziak
 * @version $Revision: $
 */
public class PostgreSqlModelReader extends JdbcModelReader
{
    /**
     * Creates a new model reader for PostgreSql databases.
     * 
     * @param platformInfo The platform specific settings
     */
    public PostgreSqlModelReader(PlatformInfo platformInfo)
    {
        super(platformInfo);
        setDefaultCatalogPattern(null);
        setDefaultSchemaPattern(null);
        setDefaultTablePattern(null);
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isInternalForeignKeyIndex(Table table, ForeignKey fk, Index index)
    {
        // TODO Auto-generated method stub
        return super.isInternalForeignKeyIndex(table, fk, index);
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isInternalPrimaryKeyIndex(Table table, Index index)
    {
        // PostgreSql uses the form "<tablename>_pkey"
        return (table.getName() + "_pkey").equals(index.getName());
    }

}
