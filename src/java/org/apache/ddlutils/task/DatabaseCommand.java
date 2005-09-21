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
    /** Whether to use delimited SQL identifiers */
    private boolean _useDelimitedSqlIdentifiers = true;

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
     * Returns the data source to use for accessing the database.
     * 
     * @return The data source
     */
    protected BasicDataSource getDataSource()
    {
        return _dataSource;
    }

    /**
     * Sets the database info.
     * 
     * @param dataSource The data source pointing to the database
     * @param type       The database type
     */
    protected void setDatabaseInfo(BasicDataSource dataSource, String type)
    {
        _dataSource   = dataSource;
        _databaseType = type;
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
     * Determines whether delimited SQL identifiers shall be used (the default).
     *
     * @return <code>true</code> if delimited SQL identifiers shall be used
     */
    public boolean isUseDelimitedSqlIdentifiers()
    {
        return _useDelimitedSqlIdentifiers;
    }

    /**
     * Specifies whether delimited SQL identifiers shall be used.
     *
     * @param useDelimitedSqlIdentifiers <code>true</code> if delimited SQL identifiers shall be used
     */
    public void setUseDelimitedSqlIdentifiers(boolean useDelimitedSqlIdentifiers)
    {
        _useDelimitedSqlIdentifiers = useDelimitedSqlIdentifiers;
    }

    /**
     * Creates the platform for the configured database.
     * 
     * @return The platform
     */
    protected Platform getPlatform() throws BuildException
    {
        Platform platform = null;

        if (_databaseType == null)
        {
            if (_dataSource == null)
            {
                throw new BuildException("No database specified.");
            }
            if (_databaseType == null)
            {
                _databaseType = new PlatformUtils().determineDatabaseType(_dataSource.getDriverClassName(),
                                                                          _dataSource.getUrl());
            }
            if (_databaseType == null)
            {
                _databaseType = new PlatformUtils().determineDatabaseType(_dataSource);
            }
        }
        try
        {
            platform = PlatformFactory.createNewPlatformInstance(_databaseType);
        }
        catch (Exception ex)
        {
            throw new BuildException("Database type "+_databaseType+" is not supported.", ex);
        }
        if (platform == null)
        {
            throw new BuildException("Database type "+_databaseType+" is not supported.");
        }
        platform.setDataSource(_dataSource);
        platform.getPlatformInfo().setUseDelimitedIdentifiers(isUseDelimitedSqlIdentifiers());

        return platform;
    }
}
