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
package org.apache.commons.sql.type;

import java.io.OutputStream;
import java.io.Writer;

import org.apache.commons.betwixt.io.BeanWriter;


/**
 * This class outputs a fully populated Types bean as XML.
 * 
 * @author <a href="mailto:tima@intalio.com">Tim Anderson</a>
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author Matthew Hawthorne
 * @version 1.1
 */
public class TypesWriter extends BeanWriter {
    
    public TypesWriter(OutputStream out) {
        super(out);
        init();
    }
    
    public TypesWriter(Writer writer) {
        super(writer);
        init();
    }
    
    /**
     * Common initialization code
     */
    private void init() {
        setXMLIntrospector(TypesReader.newXMLIntrospector());
        enablePrettyPrint();
        // TODO: Remove deprecated call once Betwixt is updated on ibiblio
        setWriteIDs(false);
        //getBindingConfiguration().setMapIDs(false);
    }
}
