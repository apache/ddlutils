package org.apache.ddlutils.ddl;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Database provider configuration
 * 
 * @author <a href="mailto:tima@intalio.com">Tim Anderson</a>
 * @version 1.1
 */
public class Provider {

    private String name;
    List versions = new ArrayList();

    public Provider() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List getProviderVersions() {
        return versions;
    }

    public void addProviderVersion(ProviderVersion version) {
        versions.add(version);
    }

    public ProviderVersion getProviderVersion(String name) {
        ProviderVersion result = null;
        Iterator iterator = versions.iterator();
        while (iterator.hasNext()) {
            ProviderVersion version = (ProviderVersion) iterator.next();
            if ((name != null && name.equals(version.getName())) 
                || name == null && version.getName() == null) {
                result = version;
                break;
            }
        }
        return result;
    }

}
