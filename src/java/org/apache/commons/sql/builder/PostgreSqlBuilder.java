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

import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.Table;

import java.io.IOException;

/**
 * An SQL Builder for PostgresSqlL
 * 
 * @author <a href="mailto:john@zenplex.com">John Thorhauer</a>
 * @version $Revision: 1.7 $
 */
public class PostgreSqlBuilder extends SqlBuilder{

    public PostgreSqlBuilder() 
    {

    } 

    protected void printAutoIncrementColumn(Table table, Column column) throws IOException {
        print(" ");
        print("serial");
        print(" ");

    }

    /** 
     * Outputs the DDL to add a column to a table.
     */
    public void createColumn(Table table, Column column) throws IOException {
        print(column.getName());
        print(" ");
        if (column.isAutoIncrement()) {
            printAutoIncrementColumn(table, column);
        }
        else
        {

            print(getSqlType(column));
            print(" ");

            if (column.getDefaultValue() != null)
            {
              print("DEFAULT '" + column.getDefaultValue() + "' ");
            }
            if (column.isRequired()) {
                printNotNullable();
            }
            else {
                printNullable();
            }
            print(" ");
        }
    }

    /**
     * @return the full SQL type string including the size
     */
    protected String getSqlType(Column column) {

        if (column.getTypeCode() == java.sql.Types.VARBINARY)
        {
            return "OID";
        }
        else
        {
            return super.getSqlType(column);
        }
    }

}
