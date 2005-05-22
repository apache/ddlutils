package org.apache.ddlutils.io;

/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

import javax.sql.DataSource;

import org.apache.commons.beanutils.DynaBean;
import org.apache.ddlutils.builder.SqlBuilder;
import org.apache.ddlutils.dynabean.DynaSql;
import org.apache.ddlutils.model.Database;

/**
 * Data sink that directly inserts the beans into the database.
 */
public class DataToDatabaseSink implements DataSink
{
    /** Generates the sql and writes it to the database */
    private DynaSql _dynaSql;

    /**
     * Creates a new sink instance.
     * 
     * @param dataSource The database to write to
     * @param model      The database model
     * @param builder    The sql builder
     */
    public DataToDatabaseSink(DataSource dataSource, Database model, SqlBuilder builder)
    {
        _dynaSql = new DynaSql(builder, dataSource, model);
    }

    /* (non-Javadoc)
     * @see org.apache.ddlutils.io.DataSink#addBean(org.apache.commons.beanutils.DynaBean)
     */
    public void addBean(DynaBean bean) throws SQLException
    {
        _dynaSql.insert(bean);
    }

}
