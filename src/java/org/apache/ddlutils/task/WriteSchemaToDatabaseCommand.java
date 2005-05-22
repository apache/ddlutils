package org.apache.ddlutils.task;

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

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.ddlutils.builder.SqlBuilder;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.util.DDLExecutor;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Command for writing a database schema into the database.
 */
public class WriteSchemaToDatabaseCommand extends DatabaseCommand
{
    /* (non-Javadoc)
     * @see org.apache.ddlutils.task.Command#execute(org.apache.tools.ant.Task, org.apache.ddlutils.model.Database)
     */
    public void execute(Task task, Database model) throws BuildException
    {
        if (getDataSource() == null)
        {
            throw new BuildException("No database specified.");
        }

        SqlBuilder   builder    = getSqlBuilder();
        StringWriter writer     = new StringWriter();
        Connection   connection = null;

        builder.setWriter(writer);
        try
        {
            connection = getDataSource().getConnection();
            if (isAlterDatabase())
            {
                builder.alterDatabase(model, connection, true, true);
            }
            else
            {
                builder.createDatabase(model);
            }
            
            DDLExecutor executor = new DDLExecutor(getDataSource());

            executor.evaluateBatch(writer.toString());
            task.log("Created database", Project.MSG_INFO);
        }
        catch (Exception ex)
        {
            throw new BuildException(ex);
        }
        finally
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException ex)
                {}
            }
        }
    }
}
