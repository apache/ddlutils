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
 * $Id$
 */
package org.apache.commons.sql.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Models a database.
 *
 * @version $Id$
 * @author John Marshall/Connectria
 * @author Matthew Hawthorne
 */
public class Database
{
    private String name;

    private String idMethod;

    /** Database version id */
    private String version;

    private List tables = new ArrayList();

    public Database()
    {
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name=name;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String ver)
    {
        version = ver;
    }


    public void setIdMethod(String idMethod)
    {
        this.idMethod=idMethod;
    }


    public void addTable(Table table)
    {
        tables.add(table);
    }

    public List getTables()
    {
        return tables;
    }

    // Helper methods

    /**
     * Finds the table with the specified name, using case insensitive matching.
     * Note that this method is not called getTable(String) to avoid introspection
     * problems.
     */
    public Table findTable(String name)
    {
        for (Iterator iter = tables.iterator(); iter.hasNext(); )
        {
            Table table = (Table) iter.next();

            // table names are typically case insensitive
            if (table.getName().equalsIgnoreCase( name ))
            {
                return table;
            }
        }
        return null;
    }


    // Additions for PropertyUtils

    public void setTable(int index, Table table)
    {
        addTable(table);
    }

    public Table getTable(int index)
    {
        return (Table) tables.get(index);
    }


    public String toString()
    {
        return super.toString() + "[name=" + name + ";tableCount=" + tables.size() + "]";
    }
}
