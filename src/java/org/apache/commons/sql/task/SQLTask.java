package org.apache.commons.sql.task;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and 
 *    "Apache Turbine" must not be used to endorse or promote products 
 *    derived from this software without prior written permission. For 
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without 
 *    prior written permission of the Apache Software Foundation.
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
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Iterator;
import org.apache.velocity.context.Context;
import org.apache.velocity.VelocityContext;

/**
 * An extended Texen task used for generating SQL source from
 * an XML schema describing a database structure.
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:jmcnally@collab.net>John McNally</a>
 * @version $Id: TorqueSQLTask.java,v 1.11 2002/04/10 21:08:43 dlr Exp $
 */
public class SQLTask 
    extends DataModelTask
{
    // if the database is set than all generated sql files
    // will be placed in the specified database, the database
    // will not be taken from the data model schema file.

    private String database;
    private String suffix = "";
    
    public void setDatabase(String database)
    {
        this.database = database;
    }
    
    public String getDatabase()
    {
        return database;
    }        

    public void setSuffix(String suffix)
    {
        this.suffix = suffix;
    }
    
    public String getSuffix()
    {
        return suffix;
    }        

    private void createSqlDbMap()
        throws Exception
    {
        if (getSqlDbMap() == null)
        {
            return;
        }        
        
        // Produce the sql -> database map
        Properties sqldbmap = new Properties();
        
        // Check to see if the sqldbmap has already been created.
        File file = new File(getSqlDbMap());
        
        if (file.exists())
        {
            FileInputStream fis = new FileInputStream(file);
            sqldbmap.load(fis);
            fis.close();
        }
        
        Iterator i = getDataModelDbMap().keySet().iterator();
        
        while (i.hasNext())
        {
            String dataModelName = (String) i.next();
            String sqlFile = dataModelName + suffix + ".sql";
            
            String databaseName;
            
            if (getDatabase() == null)
            {
                databaseName = (String) getDataModelDbMap().get(dataModelName);
            }
            else
            {   
                databaseName = getDatabase();
            }
            
            sqldbmap.setProperty(sqlFile,databaseName);
        }
        
        sqldbmap.store(new FileOutputStream(getSqlDbMap()),"Sqlfile -> Database map");
    }

    /**
     * Place our target database and target platform
     * values into the context for use in the
     * templates.
     */
    public Context initControlContext()
        throws Exception
    {   
        super.initControlContext();
        context.put("targetDatabase", getTargetDatabase());
        createSqlDbMap();
        return context;
    }
}
