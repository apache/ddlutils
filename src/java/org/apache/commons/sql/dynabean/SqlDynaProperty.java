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

import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.sql.model.Column;

/**
 * A DynaProperty which maps to a persistent Column in a database.
 * The Column describes additional relational metadata 
 * for the property such as whether the property is a primary key column, 
 * an autoIncrement column and the SQL type etc.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.14 $
 */
public class SqlDynaProperty extends DynaProperty {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog( SqlDynaProperty.class );

    private Column column;    

    /**
     * Construct a property that accepts any data type.
     *
     * @param name Name of the property being described
     * @param column the database Column this property maps to
     */
    public SqlDynaProperty(Column column) {
        super(column.getName());
        this.column = column;
    }

    /**
     * Construct a property that accepts any data type.
     *
     * @param name Name of the property being described
     * @param column the database Column this property maps to
     */
    public SqlDynaProperty(Column column, Class type) {
        super(column.getName(), type);
        this.column = column;
    }

    /**
     * @return the database Column this property maps to
     */
    public Column getColumn() {
        return column;
    }

    // Helper methods
    //-------------------------------------------------------------------------                
    
    /**
     * @return whether the property is part of the primary key
     */
    public boolean isPrimaryKey() {
        return getColumn().isPrimaryKey();
    }    
    
}
