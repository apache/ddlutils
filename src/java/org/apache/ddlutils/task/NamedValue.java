package org.apache.ddlutils.task;

/**
 * A name-value pair.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 231306 $
 */
public class NamedValue
{
    /** The name */
    private String _name;
    /** The value */
    private String _value;

    /**
     * Returns the name.
     *
     * @return The name
     */
    public String getName()
    {
        return _name;
    }

    /**
     * Sets the name.
     *
     * @param name The name
     */
    public void setName(String name)
    {
        _name = name;
    }

    /**
     * Returns the value.
     *
     * @return The value
     */
    public String getValue()
    {
        return _value;
    }

    /**
     * Sets the value.
     *
     * @param value The value
     */
    public void setValue(String value)
    {
        _value = value;
    }
}
