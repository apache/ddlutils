package org.apache.commons.sql.ddl;

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

