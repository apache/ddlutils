/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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

package org.apache.commons.sql.builder;


/**
 * An SQL Builder for the <a href="http://hsqldb.sourceforge.net/">HsqlDb</a> JDBC database.
 * This builder was primarily written to be used as another test case. If you want an open source,
 * non-GPL pure Java JDBC implementation we highly recommend you try
 * <a href="http://axion.tigris.org/">Axion</a> instead.
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.14 $
 */
public class HsqlDbBuilder extends SqlBuilder {
    
    public HsqlDbBuilder() {
        setForeignKeysEmbedded(true);
    }
}
