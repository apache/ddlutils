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

/**
 * An SQL Builder for the <a href="http://axion.tigris.org/">Axion</a> JDBC database.
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.14 $
 */
public class AxionBuilder extends SqlBuilder {
    
    public AxionBuilder() {
        setForeignKeysEmbedded(true);
    }    


    protected String getSqlType(Column column) {
        // Axion doesn't support text width specification 
        return getNativeType(column);
    }
    
    protected void writePrimaryKeys(Table table) throws IOException {
        // disable primary key constraints
    }
    
    protected void writeForeignKeys(Table table) throws IOException {
        // disable foreign key constraints
    }
    
    protected void printAutoIncrementColumn(Table table, Column column) throws IOException {
        //print( "IDENTITY" );
    }

    protected void printNotNullable() throws IOException {
        //print("NOT NULL");
    }

    protected void printNullable() throws IOException {
        //print("NULL");
    }

    protected String getNativeType(Column column){
        if(column.getTypeCode() == java.sql.Types.DECIMAL){
            return "FLOAT";
	}else{
            return super.getNativeType(column);
	}
    }

    /** 
     * Outputs the DDL to add a column to a table. Axion
     * does not support default values so we are removing
     * default from the Axion column builder.
     */
    public void createColumn(Table table, Column column) throws IOException {
        print(column.getName());
        print(" ");
        print(getSqlType(column));
        print(" ");

        if (column.isRequired()) {
            printNotNullable();
        }
        else {
            printNullable();
        }
        print(" ");
        if (column.isAutoIncrement()) {
            printAutoIncrementColumn(table, column);
        }
    }
}
