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

package org.apache.commons.sql.builder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.Table;

/**
 * An SQL Builder for MySQL
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author John Marshall/Connectria
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 1.14 $
 */
public class MySqlBuilder extends SqlBuilder
{
    private HashMap _specialTypes = new HashMap();

    public MySqlBuilder() {
        setForeignKeysEmbedded(true);
        _specialTypes.put("binary",        "BLOB");
        _specialTypes.put("blob",          "LONGBLOB");
        _specialTypes.put("boolean",       "BIT");
        _specialTypes.put("clob",          "LONGTEXT");
        _specialTypes.put("float",         "FLOAT");
        _specialTypes.put("longvarbinary", "LONGBLOB");
        _specialTypes.put("longvarchar",   "MEDIUMTEXT");
        _specialTypes.put("real",          "FLOAT");
        _specialTypes.put("varbinary",     "MEDIUMBLOB");
    }

    /* (non-Javadoc)
     * @see org.apache.commons.sql.builder.SqlBuilder#getCommentPrefix()
     */
    protected String getCommentPrefix()
    {
        return "#";
    }

    /* (non-Javadoc)
     * @see org.apache.commons.sql.builder.SqlBuilder#dropTable(Table)
     */
    public void dropTable(Table table) throws IOException
    { 
        print("drop table if exists ");
        print(table.getName());
        printEndOfStatement();
    }

    /* (non-Javadoc)
     * @see org.apache.commons.sql.builder.SqlBuilder#printAutoIncrementColumn(Table,Column)
     */
    protected void printAutoIncrementColumn(Table table, Column column) throws IOException
    {
        print("AUTO_INCREMENT");
    }

    /* (non-Javadoc)
     * @see org.apache.commons.sql.builder.SqlBuilder#shouldGeneratePrimaryKeys(List)
     */
    protected boolean shouldGeneratePrimaryKeys(List primaryKeyColumns)
    {
        /*
         * mySQL requires primary key indication for autoincrement key columns
         * I'm not sure why the default skips the pk statement if all are identity
         */
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.sql.builder.SqlBuilder#getNativeType(Column)
     */
    protected String getNativeType(Column column){
        String type        = column.getType();
        String specialType = (String)_specialTypes.get(type.toLowerCase());

        return (specialType == null ? type : specialType);
    }
}
