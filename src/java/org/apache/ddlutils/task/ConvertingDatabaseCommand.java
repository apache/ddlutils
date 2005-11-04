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

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.ddlutils.io.ConverterConfiguration;
import org.apache.ddlutils.io.DataConverterRegistration;
import org.apache.tools.ant.BuildException;

/**
 * Base type for database commands that use converters.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public abstract class ConvertingDatabaseCommand extends DatabaseCommand
{
    /** The converters. */
    private ArrayList _converters = new ArrayList();

    /**
     * Registers a converter.
     * 
     * @param converterRegistration The registration info
     */
    public void addConfiguredConverter(DataConverterRegistration converterRegistration)
    {
        _converters.add(converterRegistration);
    }

    /**
     * Registers the converters at the given configuration.
     * 
     * @param converterConf The converter configuration
     */
    protected void registerConverters(ConverterConfiguration converterConf) throws BuildException
    {
        for (Iterator it = _converters.iterator(); it.hasNext();)
        {
            DataConverterRegistration registrationInfo = (DataConverterRegistration)it.next();

            if (registrationInfo.getTypeCode() != Integer.MIN_VALUE)
            {
                converterConf.registerConverter(registrationInfo.getTypeCode(),
                                                registrationInfo.getConverter());
            }
            else
            {
                if ((registrationInfo.getTable() == null) || (registrationInfo.getColumn() == null)) 
                {
                    throw new BuildException("Please specify either the jdbc type or a table/column pair for which the converter shall be defined");
                }
                converterConf.registerConverter(registrationInfo.getTable(),
                                                registrationInfo.getColumn(),
                                                registrationInfo.getConverter());
            }
        }
    }
}
