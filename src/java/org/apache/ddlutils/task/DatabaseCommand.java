package org.apache.ddlutils.task;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.PlatformUtils;
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
    /** Whether to stop execution upon an error */
    private boolean _failOnError = true;

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
     * Determines whether the command execution will be stopped upon an error.
     * Default value is <code>true</code>.
     *
     * @return <code>true</code> if the execution stops in case of an error
     */
    public boolean isFailOnError()
    {
        return _failOnError;
    }

    /**
     * Specifies whether the command execution will be stopped upon an error.
     *
     * @param failOnError <code>true</code> if the execution stops in case of an error
     */
    public void setFailOnError(boolean failOnError)
    {
        _failOnError = failOnError;
    }

    /**
     * Creates the platform for the configured database.
     * 
     * @return The platform
     */
    protected Platform getPlatform() throws BuildException
    {
        BasicDataSource dataSource = getDataSource();
        Platform        platform   = null;

        try
        {
            if (getDatabaseType() == null)
            {
                setDatabaseType(new PlatformUtils().determineDatabaseType(dataSource.getDriverClassName(), dataSource.getUrl()));
            }
            if (getDatabaseType() == null)
            {
                setDatabaseType(new PlatformUtils().determineDatabaseType(dataSource));
            }
            platform = PlatformFactory.createNewPlatformInstance(getDatabaseType());
        }
        catch (Exception ex)
        {
            throw new BuildException("Database type "+getDatabaseType()+" is not supported.", ex);
        }
        if (platform == null)
        {
            throw new BuildException("Database type "+getDatabaseType()+" is not supported.");
        }
        else
        {
            platform.setDataSource(getDataSource());
            return platform;
        }
    }
}
