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

import java.io.OutputStream;
import java.io.Writer;

import org.apache.commons.betwixt.io.BeanWriter;

/**
 * This class outputs a fully populated Database bean as XML.
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author Matthew Hawthorne
 * @version $Revision$
 */
public class DatabaseWriter extends BeanWriter {
    
    public DatabaseWriter(OutputStream out) {
        super(out);
        init();
    }
    
    public DatabaseWriter(Writer writer) {
        super(writer);
        init();
    }
    
    /**
     * Common initialization code
     */
    private void init() {
        setXMLIntrospector( DatabaseReader.newXMLIntrospector() );
        enablePrettyPrint();
        
        getBindingConfiguration().setMapIDs(false);
    }
}
