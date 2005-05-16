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

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.digester.Rule;
import org.apache.ddlutils.dynabean.DynaSql;
import org.xml.sax.Attributes;

/**
 * A digester rule for creating dyna beans via the {@link org.apache.ddlutils.dynabean.DynaSql} class.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision:$
 */
public class DynaSqlCreateRule extends Rule
{
    /** The dyna sql instance to use for creating the dyna beans */
    private DynaSql _dynaSql;
    /** The object that will receive the read beans */
    private DataSink _receiver;

    /**
     * Creates a new creation rule that creates dyna bean instances.
     * 
     * @param dynaSql  The dyna sql instance to use for creating the dyna beans
     * @param receiver The object that will receive the read beans
     */
    public DynaSqlCreateRule(DynaSql dynaSql, DataSink receiver)
    {
        _dynaSql  = dynaSql;
        _receiver = receiver;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.digester.Rule#begin(java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void begin(String namespace, String name, Attributes attributes) throws Exception
    {
        Object instance = _dynaSql.newInstance(name);

        if (digester.getLogger().isDebugEnabled())
        {
            digester.getLogger().debug("[DynaSqlCreateRule]{" + digester.getMatch() + "} New dyna bean '" + name + "'");
        }
        digester.push(instance);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.digester.Rule#end(java.lang.String, java.lang.String)
     */
    public void end(String namespace, String name) throws Exception
    {
        DynaBean top = (DynaBean)digester.pop();

        if (digester.getLogger().isDebugEnabled())
        {
            digester.getLogger().debug("[DynaSqlCreateRule]{" + digester.getMatch() + "} Pop " + top.getDynaClass().getName());
        }
        _receiver.addBean(top);
    }

}
