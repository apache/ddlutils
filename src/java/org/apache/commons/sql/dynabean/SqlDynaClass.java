/*
 * $Header: /home/cvs/jakarta-commons-sandbox/jelly/src/java/org/apache/commons/jelly/CompilableTag.java,v 1.5 2002/05/17 15:18:12 jstrachan Exp $
 * $Revision: 1.5 $
 * $Date: 2002/05/17 15:18:12 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 * 
 * $Id: CompilableTag.java,v 1.5 2002/05/17 15:18:12 jstrachan Exp $
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
