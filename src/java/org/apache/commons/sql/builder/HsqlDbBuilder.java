package org.apache.commons.sql.builder;

/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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

import java.io.IOException;

import org.apache.commons.sql.model.Table;


/**
 * An SQL Builder for the <a href="http://hsqldb.sourceforge.net/">HsqlDb</a> JDBC database.
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 1.14 $
 */
public class HsqlDbBuilder extends SqlBuilder
{
    /* (non-Javadoc)
     * @see org.apache.commons.sql.builder.SqlBuilder#getDatabaseName()
     */
    public String getDatabaseName()
    {
        return "HsqlDb";
    }

    /* (non-Javadoc)
     * @see org.apache.commons.sql.builder.SqlBuilder#dropTable(Table)
     */
    public void dropTable(Table table) throws IOException
    { 
        print("DROP TABLE ");
        print(table.getName());
        print(" IF EXISTS");
        printEndOfStatement();
    }
}
