package org.apache.ddlutils.task;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.ddlutils.model.Database;
import org.apache.tools.ant.BuildException;

/**
 * This is the container for sub tasks that operate in the direction database -> file, eg.
 * that create/drop a schema in the database, insert data into the database, etc. They also
 * create DTDs for these data files, and dump the SQL for creating a schema in the database
 * to a file.
 * 
 * @version $Revision: 289996 $
 * @ant.task name="databaseToDdl"
 */
public class DatabaseToDdlTask extends DatabaseTaskBase
{
    /** The specific schema to use. */
    private String _schema;
    /** The specific catalog to use. */
    private String _catalog;
    /** The table types to recognize when reading the model from the database. */
    private String _tableTypes;
    /** The name of the model read from the database. */
    private String _modelName;

    /**
     * Specifies the table schema(s) to access. This is only necessary for some databases. The
     * pattern is that of
     * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/sql/DatabaseMetaData.html#getTables(java.lang.String,%20java.lang.String,%20java.lang.String,%20java.lang.String[])">java.sql.DatabaseMetaData#getTables</a>.
     * The special pattern <code>'%'</code> indicates that every table schema shall be used.
     * 
     * @param schema The schema
     * @ant.not-required No schema is used by default.
     */
    public void setSchema(String schema)
    {
        _schema = schema;
    }

    /**
     * Specifies the catalog(s) to access. This is only necessary for some databases. The pattern
     * is that of
     * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/sql/DatabaseMetaData.html#getTables(java.lang.String,%20java.lang.String,%20java.lang.String,%20java.lang.String[])">java.sql.DatabaseMetaData#getTables</a>.
     * The special pattern <code>'%'</code> indicates that every catalog shall be used.
     * 
     * @param catalog The catalog
     * @ant.not-required No catalog is used by default.
     */
    public void setCatalog(String catalog)
    {
        _catalog = catalog;
    }

    /**
     * Specifies the table types to be processed. For details and typical table types see
     * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/sql/DatabaseMetaData.html#getTables(java.lang.String,%20java.lang.String,%20java.lang.String,%20java.lang.String[])">java.sql.DatabaseMetaData#getTables</a>.
     * 
     * @param tableTypes The table types as a comma-separated list
     * @ant.not-required By default, only tables of type <code>TABLE</code> are read.
     */
    public void setTableTypes(String tableTypes)
    {
        _tableTypes = tableTypes;
    }

    /**
     * Specifies the name of the model, e.g. the value of the name attribute in the XML if
     * the <code>writeSchemaToFile</code> sub-task is used.
     * 
     * @param modelName The model name. Use <code>null</code> or an empty string for the default name
     * @ant.not-required By default, DldUtils uses the schema name returned from the database
     *                   or <code>default</code> if none was returned.
     */
    public void setModelName(String modelName)
    {
        _modelName = modelName;
    }

    /**
     * Adds the "create dtd"-command.
     * 
     * @param command The command
     */
    public void addWriteDtdToFile(WriteDtdToFileCommand command)
    {
        addCommand(command);
    }

    /**
     * Adds the "write schema to file"-command.
     * 
     * @param command The command
     */
    public void addWriteSchemaToFile(WriteSchemaToFileCommand command)
    {
        addCommand(command);
    }

    /**
     * Adds the "write schema sql to file"-command.
     * 
     * @param command The command
     */
    public void addWriteSchemaSqlToFile(WriteSchemaSqlToFileCommand command)
    {
        addCommand(command);
    }

    /**
     * Adds the "write data into database"-command.
     * 
     * @param command The command
     */
    public void addWriteDataToDatabase(WriteDataToDatabaseCommand command)
    {
        addCommand(command);
    }

    /**
     * Adds the "write data into file"-command.
     * 
     * @param command The command
     */
    public void addWriteDataToFile(WriteDataToFileCommand command)
    {
        addCommand(command);
    }

    /**
     * Returns the table types to recognize.
     * 
     * @return The table types
     */
    private String[] getTableTypes()
    {
        if ((_tableTypes == null) || (_tableTypes.length() == 0))
        {
            return new String[0];
        }

        StringTokenizer tokenizer = new StringTokenizer(_tableTypes, ",");
        ArrayList       result    = new ArrayList();

        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken().trim();

            if (token.length() > 0)
            {
                result.add(token);
            }
        }
        return (String[])result.toArray(new String[result.size()]);
    }

    /**
     * {@inheritDoc}
     */
    protected Database readModel()
    {
        if (getDataSource() == null)
        {
            throw new BuildException("No database specified.");
        }

        try
        {
            return getPlatform().readModelFromDatabase(_modelName, _catalog, _schema, getTableTypes());
        }
        catch (Exception ex)
        {
            throw new BuildException("Could not read the schema from the specified database: "+ex.getLocalizedMessage(), ex);
        }
    }
}
