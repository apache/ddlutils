package org.apache.ddlutils.task;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.builder.SqlBuilder;
import org.apache.ddlutils.builder.SqlBuilderFactory;
import org.apache.tools.ant.BuildException;

/**
 * Base type for commands that have the database info embedded.
 */
public abstract class DatabaseCommand implements Command
{
    /** The type of the database */
    private String _databaseType;
    /** The data source to use for accessing the database */
    private BasicDataSource _dataSource;
    /** Whether to alter or re-set the database if it already exists */
    private boolean _alterDb = true;

    /**
     * Returns the database type.
     * 
     * @return The database type
     */
    protected String getDatabaseType()
    {
        return _databaseType;
    }

    /**
     * Sets the database type.
     * 
     * @param type The database type
     */
    public void setDatabaseType(String type)
    {
        _databaseType = type;
    }

    /**
     * Returns the data source to use for accessing the database.
     * 
     * @return The data source
     */
    protected BasicDataSource getDataSource()
    {
        return _dataSource;
    }

    /**
     * Adds the data source to use for accessing the database.
     * 
     * @param dataSource The data source
     */
    public void addConfiguredDatabase(BasicDataSource dataSource)
    {
        _dataSource = dataSource;
    }

    /**
     * Determines whether to alter the database if it already exists, or re-set it.
     * 
     * @return <code>true</code> if to alter the database
     */
    protected boolean isAlterDatabase()
    {
        return _alterDb;
    }

    /**
     * Specifies whether to alter the database if it already exists, or re-set it.
     * 
     * @param alterTheDb <code>true</code> if to alter the database
     */
    public void setAlterDatabase(boolean alterTheDb)
    {
        _alterDb = alterTheDb;
    }

    /**
     * Creates the sql builder for the configured database.
     * 
     * @return The sql builder
     */
    protected SqlBuilder getSqlBuilder() throws BuildException
    {
        // TODO: This should largely be deducable from the jdbc connection url
        if (getDatabaseType() == null)
        {
            throw new BuildException("The database type needs to be defined.");
        }

        SqlBuilder builder = null;

        try
        {
            builder = SqlBuilderFactory.newSqlBuilder(getDatabaseType());
        }
        catch (Exception ex)
        {
            throw new BuildException("Database type "+getDatabaseType()+" is not supported.");
        }
        if (builder == null)
        {
            throw new BuildException("Database type "+getDatabaseType()+" is not supported.");
        }
        else
        {
            return builder;
        }
    }
}
