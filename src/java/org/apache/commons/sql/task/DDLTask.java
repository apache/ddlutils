/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import org.apache.commons.sql.builder.SqlBuilder;
import org.apache.commons.sql.builder.SqlBuilderFactory;
import org.apache.commons.sql.io.DatabaseReader;
import org.apache.commons.sql.model.Database;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * A base task which generates the SQL DDL to create a database
 * to a given output file from an XML schema representing
 * a data model contains tables for a <strong>single</strong>
 * database.
 *
 * @version $Id$
 */
public class DDLTask extends Task
{
    /**
     * XML that describes the database model, this is transformed
     * into the application model object.
     */
    private File xmlFile;
    
    /**
     * The output file that this task should output
     */
    private File output;

    /**
     * The target database we are generating SQL
     * for.
     */
    private String targetDatabase;

    /**
     * Flag indicates whether SQL drop statements should be generated.
     */
    private boolean dropTables = true;

    /**
     * Get the xml schema describing the application model.
     *
     * @return  String xml schema file.
     */
    public File getXmlFile()
    {
        return xmlFile;
    }

    /**
     * Set the xml schema describing the application model.
     *
     * @param xmlFile The new XmlFile value
     */
    public void setXmlFile(File xmlFile)
    {
        //this.xmlFile = project.resolveFile(xmlFile).toString();
        this.xmlFile = xmlFile;
    }

    /**
     * Get the current target package.
     *
     * @return String target database(s)
     */
    public String getTargetDatabase()
    {
        return targetDatabase;
    }

    /**
     * Sets the target database we are generating SQL
     * for.
     *
     * @param targetDatabase target database
     */
    public void setTargetDatabase(String targetDatabase)
    {
        this.targetDatabase = targetDatabase;
    }
    
    /**
     * @return the output file
     */
    public File getOutput() 
    {
        return output;
    }
    
    /**
     * Sets the output file
     */
    public void setOutput(File output) 
    {
        this.output = output;
    }
    
    /**
     * @return Returns the dropTables.
     */
    public boolean isDropTables() {
        return dropTables;
    }

    /**
     * @param dropTables The dropTables to set.
     */
    public void setDropTables(boolean dropTables) {
        this.dropTables = dropTables;
    }
    
    /**
     * Create the SQL DDL for the given database.
     */
    public void execute() throws BuildException
    {
        if (targetDatabase == null) 
        {
            throw new BuildException( "Must specify a targetDatabase attribute" );
        }
        if (xmlFile == null) 
        {
            throw new BuildException( "Must specify an xmlFile attribute" );
        }
        if (output == null) 
        {
            throw new BuildException( "Must specify an output attribute" );
        }
        
        Database database = null;
        try 
        {
            database = loadDatabase();
        }
        catch (Exception e) 
        {
            throw new BuildException( "Failed to parse file: " + getXmlFile(), e );                
        }
        
        FileWriter writer = null;
        try 
        {
            writer = new FileWriter( getOutput() );
        }
        catch (Exception e) 
        {
            throw new BuildException( "Failed to create file: " + getOutput(), e );                
        }
        
        SqlBuilder builder = null;
        try
        {        
            builder = newSqlBuilder(writer);
        }
        catch (Exception e) 
        {
            throw new BuildException( "Failed to create SqlBuilder for database: " + getTargetDatabase(), e );                
        }
        if ( builder == null)
        {
            throw new BuildException( "Unknown database type: " + getTargetDatabase() );                
        }
        
        // OK we're ready now, lets try create the DDL
        try 
        {
            builder.createDatabase(database, dropTables);
            writer.close();
        }
        catch (Exception e) 
        {
            throw new BuildException( "Error occurred while writing to file: " + getOutput(), e );                
        }
    }
    
    // Implementation methods
    //-------------------------------------------------------------------------                
    
    /**
     * Loads the XML schema from the XML file and returns the database model bean
     */
    protected Database loadDatabase() throws Exception
    {
        DatabaseReader reader = new DatabaseReader();
        return (Database) reader.parse( getXmlFile() );
    }
    
    protected SqlBuilder newSqlBuilder(Writer writer) throws Exception     
    {   
        SqlBuilder builder = SqlBuilderFactory.newSqlBuilder(getTargetDatabase());
        builder.setWriter(writer);
        return builder;
    }
    

}
