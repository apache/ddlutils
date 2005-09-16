package org.apache.ddlutils.task;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.ddlutils.PlatformFactory;

/**
 * A parameter which consists of a name-value pair and an optional list of platforms
 * for which the parameter is applicable.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 231306 $
 */
public class Parameter
{
    /** The name */
    private String _name;
    /** The value */
    private String _value;
    /** The platforms for which this parameter is applicable */
    private Set _platforms = new HashSet();

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

    /**
     * Sets the platforms - a comma-separated list of platform names - for which this parameter shall be used.
     * 
     * @param platforms The platforms
     */
    public void setPlatforms(String platforms)
    {
        _platforms.clear();
        if (platforms != null)
        {
            StringTokenizer tokenizer = new StringTokenizer(platforms, ",");

            while (tokenizer.hasMoreTokens())
            {
                String platform = tokenizer.nextToken().trim();

                if (PlatformFactory.isPlatformSupported(platform))
                {
                    _platforms.add(platform.toLowerCase());
                }
                else
                {
                    throw new IllegalArgumentException("Platform "+platform+" is not supported");
                }
            }
        }
    }

    /**
     * Determines whether this parameter is applicable for the indicated platform.
     * 
     * @param platformName The platform name
     * @return <code>true</code> if this parameter is defined for the platform
     */
    public boolean isForPlatform(String platformName)
    {
        return _platforms.isEmpty() || _platforms.contains(platformName.toLowerCase());
    }
}
