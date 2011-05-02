package org.apache.ddlutils.io;

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

import java.util.Iterator;
import java.util.List;

/**
 * Base class providing helper functions for writing model elements to XML.  
 */
public abstract class ModelXmlWriter
{
    protected void writeText(DataWriter writer, String value, boolean isBase64Encoded)
    {
        if (isBase64Encoded)
        {
            writer.writeAttribute(null, "base64", "true");
            writer.writeCharacters(value);
        }
        else
        {
            List cutPoints = XMLUtils.findCDataCutPoints(value);
    
            // if the content contains special characters, we have to apply base64 encoding to it
            if (cutPoints.isEmpty())
            {
                writer.writeCharacters(value);
            }
            else
            {
                int lastPos = 0;
    
                for (Iterator cutPointIt = cutPoints.iterator(); cutPointIt.hasNext();)
                {
                    int curPos = ((Integer)cutPointIt.next()).intValue();
    
                    writer.writeCData(value.substring(lastPos, curPos));
                    lastPos = curPos;
                }
                if (lastPos < value.length())
                {
                    writer.writeCData(value.substring(lastPos));
                }
            }
        }
    }
}
