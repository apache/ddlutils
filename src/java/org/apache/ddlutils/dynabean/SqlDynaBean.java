package org.apache.ddlutils.dynabean;

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

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SqlDynaBean is a DynaBean which can be persisted as a single row in 
 * a Database Table.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision$
 */
public class SqlDynaBean extends BasicDynaBean {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog( SqlDynaBean.class );
    
    public SqlDynaBean(DynaClass dynaClass) {
        super(dynaClass);
    }

    
}
