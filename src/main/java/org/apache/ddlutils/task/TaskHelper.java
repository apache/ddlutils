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

/**
 * Contains some utility functions for the Ant tasks.
 * 
 * @version $Revision: $
 */
public class TaskHelper
{
    /**
     * Parses the given comma-separated string list. A comma within a string needs to be
     * escaped as '\,'. Also, the individual strings are not trimmed but returned as-is.
     * 
     * @param stringList The comma-separated list of strings
     * @return The strings as an array
     */
    public String[] parseCommaSeparatedStringList(String stringList)
    {
        String[]  tokens = stringList.split(",");
        ArrayList values = new ArrayList();
        String    last   = null;

        for (int idx = 0; idx < tokens.length; idx++)
        {
            String  str         = tokens[idx];
            int     strLen      = str.length();
            boolean endsInSlash = (strLen > 0) && (str.charAt(strLen - 1) == '\\') &&
                                  ((strLen == 1) || (str.charAt(strLen - 2) != '\\'));

            if (last != null)
            {
                last += "," + str;
                if (!endsInSlash)
                {
                    values.add(last);
                    last = null;
                }
            }
            else if (endsInSlash)
            {
                last = str.substring(0, strLen - 1);
            }
            else
            {
                values.add(str);
            }
        }
        if (last != null)
        {
            values.add(last + ",");
        }
        return (String[])values.toArray(new String[values.size()]);
    }
}
