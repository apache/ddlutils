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

package org.apache.commons.sql.task;

import java.io.FileWriter;
import java.util.Hashtable;

import org.apache.commons.sql.io.DatabaseWriter;
import org.apache.commons.sql.model.Database;
import org.apache.tools.ant.BuildException;

/**
 * This class generates an XML schema of an existing database from
 * JDBC metadata.
 *
 * @author <a href="mailto:drfish@cox.net">J. Russell Smyth</a>
 * @version $Id: $
 */
public class JdbcToSchemaTask extends DatabaseTask
{
    /** Name of database schema file produced. */
    protected String outputFile;

    /** Hashtable of columns that have primary keys. */
    protected Hashtable primaryKeys;

    /** Hashtable to track what table a column belongs to. */
    protected Hashtable columnTableMap;
    
    protected boolean useTypeNames = false;

    
    public void setOutputFile (String v)
    {
        outputFile = v;
    }
    public void setUseTypeNames(boolean b)
    {
        useTypeNames = b;
    }
        

    /**
     * Default constructor.
     */
    public void execute() throws BuildException
    {
        System.err.println("Commons-Sql JdbcToSchema starting\n");
        printDbSettings();
        
        Database db = null;
        try{
            db = getDbFromConnection( getDataSource().getConnection() ); 
            DatabaseWriter w = new DatabaseWriter(new FileWriter(outputFile));
           // w.setWriteIDs(true);
            w.write(db);
            w.close();
        }catch(Exception e){
            System.out.println("exception during load:"+e.getMessage());
        }
    }

}