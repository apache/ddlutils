package org.apache.ddlutils.platform;

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

/**
 * The platform implementation for MaxDB. It is currently identical to the SapDB
 * implementation as there is no difference in the functionality we're using.
 * Note that DdlUtils is currently not able to distinguish them based on the
 * jdbc driver or subprotocol as they are identical.   
 * 
 * @author Thomas Dudziak
 * @version $Revision: 231306 $
 */
public class MaxDbPlatform extends SapDbPlatform
{
    /** Database name of this platform. */
    public static final String DATABASENAME = "MaxDB";

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return DATABASENAME;
    }
}
