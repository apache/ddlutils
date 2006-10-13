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
 * Ant task for working with a database, e.g. retrieving the schema from a
 * database, dumping data, etc.
 * 
 * @version $Revision: 289996 $
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
     * Sets the database schema to access.
     * 
     * @param schema The schema
     */
    public void setSchema(String schema)
    {
        _schema = schema;
    }

    /**
     * Sets the database catalog to access.
     * 
     * @param catalog The catalog
     */
    public void setCatalog(String catalog)
    {
        _catalog = catalog;
    }

    /**
     * Sets the table types ro recognize.
     * 
     * @param tableTypes The table types as a comma-separated list
     */
    public void setTableTypes(String tableTypes)
    {
        _tableTypes = tableTypes;
    }

    /**
     * Sets the name that the model read from the database shall have.
     * Use <code>null</code> or an empty string for the default name.
     * 
     * @param modelName The model name
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
