package org.apache.ddlutils.platform;

import java.sql.Types;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.ddlutils.model.TypeMap;
import org.apache.ddlutils.util.Jdbc3Utils;

/**
 * Helper class for dealing with default values, e.g. converting them to other types.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 289996 $
 */
public class DefaultValueHelper
{
    /**
     * Converts the given default value from the specified original to the target
     * jdbc type.
     * 
     * @param defaultValue     The default value
     * @param originalTypeCode The original type code
     * @param targetTypeCode   The target type code
     * @return The converted default value 
     */
    public String convert(String defaultValue, int originalTypeCode, int targetTypeCode)
    {
        String result = defaultValue;

        if (defaultValue != null)
        {
            switch (originalTypeCode)
            {
                case Types.BIT:
                    result = convertBoolean(defaultValue, targetTypeCode).toString();
                    break;
                default:
                    if (Jdbc3Utils.supportsJava14JdbcTypes() &&
                        (originalTypeCode == Jdbc3Utils.determineBooleanTypeCode()))
                    {
                        result = convertBoolean(defaultValue, targetTypeCode).toString();
                    }
                    break;
            }
        }
        return result;
    }

    /**
     * Converts a boolean default value to the given target type.
     * 
     * @param defaultValue   The default value
     * @param targetTypeCode The target type code
     * @return
     */
    private Object convertBoolean(String defaultValue, int targetTypeCode)
    {
        Boolean value  = null;
        Object  result = null;

        try
        {
            value = (Boolean)ConvertUtils.convert(defaultValue, Boolean.class);
        }
        catch (ConversionException ex)
        {
            return defaultValue;
        }
        
        if ((targetTypeCode == Types.BIT) ||
            (Jdbc3Utils.supportsJava14JdbcTypes() && (targetTypeCode == Jdbc3Utils.determineBooleanTypeCode())))
        {
            result = value;
        }
        else if (TypeMap.isNumericType(targetTypeCode))
        {
            result = (value.booleanValue() ? new Integer(1) : new Integer(0));
        }
        else
        {
            result = value.toString();
        }
        return result;
    }
}
