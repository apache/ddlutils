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

import java.util.Iterator;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.ddlutils.dynabean.DynaSql;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

/**
 * Reads data XML into dyna beans according to a specified database model.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision:$
 */
public class DataReader extends Digester
{
    /** The object to receive the read beans */
    private DataSink _sink;

    /**
     * Creates a new data reader that reads data fitting the specified database model.
     * 
     * @param model The database model
     */
    public DataReader(Database model, DataSink sink)
    {
        setValidating(false);

        DynaSql dynaSql = new DynaSql(null);
        Rule    rule    = new DynaSqlCreateRule(dynaSql, sink);

        dynaSql.setDatabase(model);
        for (Iterator tableIt = model.getTables().iterator(); tableIt.hasNext();)
        {
            // TODO: For now we hardcode the root as 'data' but ultimately we should wildcard it ('?')
            Table  table = (Table)tableIt.next();
            String path  = "data/"+table.getName();

            // we're registering the same rule for every table
            addRule(path, rule);
            addSetProperties(path);
            for (Iterator columnIt = table.getColumns().iterator(); columnIt.hasNext();)
            {
                Column column = (Column)columnIt.next();

                addBeanPropertySetter("data/"+table.getName()+"/"+column.getName(), column.getName());
            }
        }
    }
}
