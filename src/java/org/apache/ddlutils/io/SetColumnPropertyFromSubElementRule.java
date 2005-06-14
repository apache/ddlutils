package org.apache.ddlutils.io;

/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.digester.Rule;
import org.apache.ddlutils.io.converters.SqlTypeConverter;
import org.apache.ddlutils.model.Column;

/**
 * A digester rule for setting a bean property that corresponds to a column
 * with the value derived from a sub element. 
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision:$
 */
public class SetColumnPropertyFromSubElementRule extends Rule
{
    /** The column that this rule shall set */
    private Column _column;
    /** The converter for generating the property value from a string */
    private SqlTypeConverter _converter;

    /**
     * Creates a new creation rule that sets the property corresponding to the given column.
     * 
     * @param column    The column that this rule shall set
     * @param converter The converter to be used for this column
     */
    public SetColumnPropertyFromSubElementRule(Column column, SqlTypeConverter converter)
    {
        _column    = column;
        _converter = converter;
    }

    
    /* (non-Javadoc)
     * @see org.apache.commons.digester.Rule#body(java.lang.String)
     */
    public void body(String text) throws Exception
    {
        String attrValue = text.trim();
        Object propValue = (_converter != null ? _converter.convertFromString(attrValue, _column.getTypeCode()) : attrValue);

        if (digester.getLogger().isDebugEnabled())
        {
            digester.getLogger().debug("[SetColumnPropertyFromSubElementRule]{" + digester.getMatch() +
                                       "} Setting property '" + _column.getName() + "' to '" + propValue + "'");
        }

        PropertyUtils.setProperty(digester.peek(), _column.getName(), propValue);
    }
}
