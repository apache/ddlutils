/*
 * $Header: /home/cvs/jakarta-commons-sandbox/jelly/src/java/org/apache/commons/jelly/CompilableTag.java,v 1.5 2002/05/17 15:18:12 jstrachan Exp $
 * $Revision: 1.5 $
 * $Date: 2002/05/17 15:18:12 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
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

package org.apache.commons.sql.builder;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.ForeignKey;
import org.apache.commons.sql.model.Table;

/**
 * An SQL Builder for MS SQL
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.14 $
 */
public class MSSqlBuilder extends SqlBuilder {
    
    public MSSqlBuilder() {
        setForeignKeyConstraintsNamed(true);
    }
    
    public void dropTable(Table table) throws IOException { 
        // this method is one example that might be a bit simpler if implemented in Velocity...
        
        String tableName = table.getName();

        // drop the foreign key contraints
        int counter = 1;
        for (Iterator iter = table.getForeignKeys().iterator(); iter.hasNext(); ) {
            ForeignKey key = (ForeignKey) iter.next();
            
            String constraintName = tableName + "_FK_" + counter;
            println("IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='" 
                + constraintName + "'"
            );
            printIndent();
            print("ALTER TABLE " + tableName + " DROP CONSTRAINT " + constraintName );
            printEndOfStatement();
        }
        
        // now drop the table
        println( "IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = '" + tableName + "')" );
        println( "BEGIN" );
        println( "     DECLARE @reftable nvarchar(60), @constraintname nvarchar(60)" );
        println( "     DECLARE refcursor CURSOR FOR" );
        println( "     select reftables.name tablename, cons.name constraitname" );
        println( "      from sysobjects tables," );
        println( "           sysobjects reftables," );
        println( "           sysobjects cons," );
        println( "           sysreferences ref" );
        println( "       where tables.id = ref.rkeyid" );
        println( "         and cons.id = ref.constid" );
        println( "         and reftables.id = ref.fkeyid" );
        println( "         and tables.name = '" + tableName + "'" );
        println( "     OPEN refcursor" );
        println( "     FETCH NEXT from refcursor into @reftable, @constraintname" );
        println( "     while @@FETCH_STATUS = 0" );
        println( "     BEGIN" );
        println( "       exec ('alter table '+@reftable+' drop constraint '+@constraintname)" );
        println( "       FETCH NEXT from refcursor into @reftable, @constraintname" );
        println( "     END" );
        println( "     CLOSE refcursor" );
        println( "     DEALLOCATE refcursor" );
        println( "     DROP TABLE " + tableName );
        print( "END" );
        printEndOfStatement();
    }
    
    protected void printComment(String text) throws IOException {
        print("# ");
        println(text);
    }
    
    protected void printAutoIncrementColumn(Table table, Column column) throws IOException {
        print( "IDENTITY (1,1) " );
    }
}
