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
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:jvanzyl@zenplex.com">Jason van Zyl</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
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
            builder.createDatabase(database);
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
