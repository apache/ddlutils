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

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.commons.sql.builder.SqlBuilder;
import org.apache.commons.sql.builder.SqlBuilderFactory;
import org.apache.commons.sql.io.DatabaseReader;
import org.apache.commons.sql.model.Database;
import org.apache.commons.sql.util.DDLExecutor;
import org.apache.tools.ant.BuildException;

/**
 * A base task which generates the SQL DDL to create a database
 * to a given output file from an XML schema representing
 * a data model contains tables for a <strong>single</strong>
 * database.  This task can optionally generate DDL to upgrade an existing
 * database to the current schema definition.  The results of either
 * generation can be executed against an existing database.
 * <p>
 * Here is a ant/maven excerpt for using this:
 * <pre>
  &lt;taskdef
    name="ddl"
    classname="org.apache.commons.sql.task.DDLTask"&gt;
      &lt;classpath refid="maven.dependency.classpath"/&gt;
  &lt;/taskdef&gt;
        
  &lt;target name="custom-ddl" description="Creates ddl"&gt;
    &lt;ddl
      xmlFile="schema/schema.xml" 
      targetDatabase="mysql"
      output="target/schema.sql"
      dbUrl="jdbc:mysql://localhost:3306/test"
      dbUser="user"
      dbPassword="pass"
      dbDriver="com.mysql.jdbc.Driver"
      alterDb="true"
      executeSql="true"
      modifyColumns="true"
      doDrops="true"
    /&gt;
  &lt;/target&gt;
 * </pre>
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:jvanzyl@zenplex.com">Jason van Zyl</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author John Marshall/Connectria
 */
public class DDLTask extends DatabaseTask
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
     * Flag for executing the sql or not.
     */
    private boolean executeSql;

    /**
     * Flag for whether to alter the database or recreate from scratch
     */
    private boolean alterDb;
    
    /**
     * Flag for whether to modify column definitions in an existing database
     */
    private boolean modifyColumns;
    
    /**
     * Flag for whether drops should be made when updating an existing database
     */
    private boolean doDrops;
    
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
     * Check if the database should be altered to match the schema or
     * recreated from scratch
     * @return alter flag
     */
    public boolean getAlterDb() {
        return alterDb;
    }

    /**
     * Set whether the database should be altered to match the schema or
     * recreated from scratch
     * @param alterDb alter flag
     */
    public void setAlterDb(boolean alterDb) {
        this.alterDb = alterDb;
    }

    /**
     * Check if the generated ddl should be executed against the databsase
     * @return true if sql is to be executed
     */
    public boolean getExecuteSql() {
        return executeSql;
    }

    /**
     * Set whether the generated ddl should be executed against the databsase
     * @param executeSql the execute flag
     */
    public void setExecuteSql(boolean executeSql) {
        this.executeSql = executeSql;
    }

    /**
     * Check if tables/columns/indexes should be dropped when updating a database
     * @return true if drops should be made
     */
    public boolean getDoDrops() {
        return doDrops;
    }

    /**
     * Set whether tables/columns/indexes should be dropped when updating a database
     * @param doDrops the new drop flag
     */
    public void setDoDrops(boolean doDrops) {
        this.doDrops = doDrops;
    }

    /**
     * Modify column definitions in an existing database
     * @return true if columns should be modified
     */
    public boolean getModifyColumns() {
        return modifyColumns;
    }

    /**
     * Modify column definitions in an existing database
     * @param modifyColumns the new flag
     */
    public void setModifyColumns(boolean modifyColumns) {
        this.modifyColumns = modifyColumns;
    }


    /**
     * Checks that settings exist and in valid combinations
     * 
     * @throws BuildException if parameters are incorrect
     */
    private void assertValidSettings() throws BuildException {
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
        if (getDbUrl() == null && ( alterDb || executeSql ))
        {
            throw new BuildException( "Connection url is required if altering database or executing sql" );
        }
    }
    
    /**
     * Create the SQL DDL for the given database.
     * 
     * @throws BuildException
     */
    public void execute() throws BuildException
    {
        assertValidSettings();

        Database database = null;
        try 
        {
            database = loadDatabase();
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            throw new BuildException( "Failed to parse file: " + getXmlFile(), e );                
        }
        
        DataSource dataSource = null;
        if (getDbUrl() != null)
        {
            try
            { 
                dataSource = getDataSource();
            }
            catch ( Exception e )
            {
                e.printStackTrace();
                throw new BuildException( "Could not get connection: " + dbUrl, e );
            }
        }
        
        StringWriter writer = new StringWriter();
        SqlBuilder builder = null;
        try
        {
            builder = newSqlBuilder(writer);
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            throw new BuildException( "Failed to create SqlBuilder for database: " + getTargetDatabase(), e );                
        }
        if ( builder == null)
        {
            throw new BuildException( "Unknown database type: " + getTargetDatabase() );                
        }
        
        // OK we're ready now, lets try create the DDL
        Connection con = null;
        try 
        {
            if ( alterDb )
            {
                con = dataSource.getConnection();
                builder.alterDatabase(database, con, doDrops, modifyColumns);
            }
            else
            {
                builder.createDatabase(database);
            }
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            throw new BuildException( "Error occurred while creating ddl", e );
        } 
        finally
        {
            try
            {
                if ( con != null )
                {
                    con.close();
                }
            }
            catch (Exception e)
            {
                //ignore
            }
        }

        String sql = writer.toString();
        if ( executeSql )
        {
            try
            {
                DDLExecutor exec = new DDLExecutor( dataSource );
                exec.evaluateBatch(sql);
                
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new BuildException( "Failed to create evaluate sql", e );                
            }
        }

        //write it out
        FileWriter out = null;
        try 
        {
            out = new FileWriter( getOutput() );
            out.write( sql );
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            throw new BuildException( "Failed to create file: " + getOutput(), e );                
        }
        finally
        {
            try
            {
                out.close();
            }
            catch (Exception e)
            {
                //ignore
            }
        }

    }
    
    // Implementation methods
    //-------------------------------------------------------------------------                
    
    /**
     * Loads the XML schema from the XML file and returns the database model bean
     * 
     * @return Database schema
     * @throws Exception
     */
    protected Database loadDatabase() throws Exception
    {
        DatabaseReader reader = new DatabaseReader();
        Database db = (Database) reader.parse( getXmlFile() );

//org.apache.commons.sql.io.DatabaseWriter writer = new org.apache.commons.sql.io.DatabaseWriter(System.err);
//writer.write(db);
        
        return db;
    }
    
    /**
     * Gets an SqlBuilder for the given writer
     * 
     * @param writer Destination writer
     * @return SqlBuilder
     * 
     * @throws Exception
     */
    protected SqlBuilder newSqlBuilder(Writer writer) throws Exception     
    {   
        SqlBuilder builder = SqlBuilderFactory.newSqlBuilder(getTargetDatabase());
        builder.setWriter(writer);
        return builder;
    }
}
