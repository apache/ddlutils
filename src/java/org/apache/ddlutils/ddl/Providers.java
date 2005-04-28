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
import java.util.HashMap;
import java.util.List;


/**
 * The set of known database providers
 * 
 * @author <a href="mailto:tima@intalio.com">Tim Anderson</a>
 * @version 1.1
 */
public class Providers {

    private HashMap providers = new HashMap();

    public Providers() {
    }

    public void addProvider(Provider provider) {
        providers.put(provider.getName().toLowerCase(), provider);
    }

    public List getProviders() {
        return new ArrayList(providers.values());
    }

    public Provider getProvider(String name) {
        return (Provider) providers.get(name.toLowerCase());
    }

}

