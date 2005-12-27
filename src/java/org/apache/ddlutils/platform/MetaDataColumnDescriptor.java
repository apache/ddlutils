package org.apache.ddlutils.platform;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Describes a column in a metadata result set.
 * 
 * @author tomdz
 * @version $Revision: $
 */
public class MetaDataColumnDescriptor
{
    /** The name of the column. */
    private String _columnName;
    /** The jdbc type to read from the result set. */
    private int _jdbcType;
    /** The default value if the column is not present in the result set. */
    private Object _defaultValue;

    /**
     * Creates a new descriptor instance.
     * 
     * @param columnName The name of the column
     * @param jdbcType   The jdbc type for reading from the result set, one of
     *                   VARCHAR, INTEGER, TINYINT, BIT
     */
    public MetaDataColumnDescriptor(String columnName, int jdbcType)
    {
        this(columnName, jdbcType, null);
    }

    /**
     * Creates a new descriptor instance.
     * 
     * @param columnName   The name of the column
     * @param jdbcType   The jdbc type for reading from the result set, one of
     *                   VARCHAR, INTEGER, TINYINT, BIT
     * @param defaultValue The default value if the column is not present in the result set
     */
    public MetaDataColumnDescriptor(String columnName, int jdbcType, Object defaultValue)
    {
        _columnName   = columnName.toUpperCase();
        _jdbcType     = jdbcType;
        _defaultValue = defaultValue;
    }

    /**
     * Returns the name.
     *
     * @return The name
     */
    public String getName()
    {
        return _columnName;
    }

    /**
     * Returns the default value.
     *
     * @return The default value
     */
    public Object getDefaultValue()
    {
        return _defaultValue;
    }

    /**
     * Returns the jdbc type to read from the result set.
     *
     * @return The jdbc type
     */
    public int getJdbcType()
    {
        return _jdbcType;
    }

    /**
     * Reads the column from the result set.
     * 
     * @param resultSet The result set
     * @return The column value or the default value if the column is not present in the result set
     */
    public Object readColumn(ResultSet resultSet) throws SQLException
    {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int               foundIdx = -1;

        for (int idx = 1; (foundIdx < 0) && (idx <= metaData.getColumnCount()); idx++)
        {
            if (_columnName.equals(metaData.getColumnName(idx).toUpperCase()))
            {
                foundIdx = idx;
            }
        }
        if (foundIdx > 0)
        {
            switch (_jdbcType)
            {
                case Types.BIT:
                    return new Boolean(resultSet.getBoolean(foundIdx));
                case Types.INTEGER:
                    return new Integer(resultSet.getInt(foundIdx));
                case Types.TINYINT:
                    return new Short(resultSet.getShort(foundIdx));
                default:
                    return resultSet.getString(foundIdx);
            }
        }
        else
        {
            return _defaultValue;
        }
    }
}
