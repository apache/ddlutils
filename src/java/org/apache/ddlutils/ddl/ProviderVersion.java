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

/**
 * Configuration for a specific version of a database provider
 * 
 * @author <a href="mailto:tima@intalio.com">Tim Anderson</a>
 * @version 1.1
 */
public class ProviderVersion {

    private String name;
    private String createScript;
    private String dropScript;
    private String types;

    public ProviderVersion() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreateScript() {
        return createScript;
    }

    public void setCreateScript(String path) {
        createScript = path;
    }

    public String getDropScript() {
        return dropScript;
    }

    public void setDropScript(String path) {
        dropScript = path;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String path) {
        types = path;
    }

}
