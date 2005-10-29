package org.apache.ddlutils.task;

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
