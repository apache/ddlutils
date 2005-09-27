package org.apache.ddlutils.task;

/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.ddlutils.PlatformFactory;

/**
 * A parameter which consists of a name-value pair and an optional list of platforms
 * for which the parameter is applicable.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 231306 $
 */
public class Parameter
{
    /** The name. */
    private String _name;
    /** The value. */
    private String _value;
    /** The platforms for which this parameter is applicable. */
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
