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
package org.apache.commons.sql.dynabean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.Table;

/**
 * SqlDynaClass is a DynaClass which is associated with a persistent 
 * Table in a Database.
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.14 $
 */
public class SqlDynaClass extends BasicDynaClass {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog( SqlDynaClass.class );
    
    private Table table;
    private SqlDynaProperty[] primaryKeys;
    private SqlDynaProperty[] nonPrimaryKeyProperties;
    
    /**
     * Creates a new SqlDynaClass instance from a Table model.
     */
    public static SqlDynaClass newInstance(Table table) {
        List properties = new ArrayList();
        for (Iterator iter = table.getColumns().iterator(); iter.hasNext(); ) {
            Column column = (Column) iter.next();
            properties.add( new SqlDynaProperty(column));
        }
        SqlDynaProperty[] array = new SqlDynaProperty[properties.size()];
        properties.toArray(array);
        return new SqlDynaClass(table, array);
    }
    
    public SqlDynaClass(Table table) {
        super(table.getName(), SqlDynaBean.class);
        this.table = table;
    }

    public SqlDynaClass(Table table, SqlDynaProperty[] properties) {
        super(table.getName(), SqlDynaBean.class, properties);
        this.table = table;
    }

    /**
     * @return the database Table this DynaClass maps to
     */
    public Table getTable() {
        return table;
    }

    // Helper methods
    //-------------------------------------------------------------------------                
    
    /**
     * @return the name of the table
     */
    public String getTableName() {
        return getTable().getName();
    }    
    
    /**
     * @return the SqlDynaProperty objects of this class
     */
    public SqlDynaProperty[] getSqlDynaProperties() {
        return (SqlDynaProperty[]) getDynaProperties();
    }
    
    /**
     * @return an array of the primary key DynaProperty objects
     */
    public SqlDynaProperty[] getPrimaryKeyProperties() {
        if ( primaryKeys == null ) {
            initPrimaryKeys();
        }
        return primaryKeys;
    }

    /**
     * @return an array of the non-primary key DynaProperty objects
     */
    public SqlDynaProperty[] getNonPrimaryKeyProperties() {
        if ( nonPrimaryKeyProperties == null ) {
            initPrimaryKeys();
        }
        return nonPrimaryKeyProperties;
    }
    
    // Implementation methods    
    //-------------------------------------------------------------------------                

    /**
     * Creates the primary key and non primary key property arrays, laziliy.
     */
    protected void initPrimaryKeys() {
        List primaryKeyList = new ArrayList();
        List otherList = new ArrayList();
        
        DynaProperty[] properties = getDynaProperties();
        for (int i = 0, size = properties.length; i < size; i++ ) {
            DynaProperty property = properties[i];
            if (property instanceof SqlDynaProperty) {
                SqlDynaProperty sqlProperty = (SqlDynaProperty) property;
                if ( sqlProperty.isPrimaryKey() ) {
                    primaryKeyList.add( sqlProperty );
                }
                else {
                    otherList.add( sqlProperty );
                }
            }
        }
        this.primaryKeys = new SqlDynaProperty[primaryKeyList.size()];
        primaryKeyList.toArray(this.primaryKeys);
        
        this.nonPrimaryKeyProperties = new SqlDynaProperty[otherList.size()];
        otherList.toArray(this.nonPrimaryKeyProperties);
    }
}
