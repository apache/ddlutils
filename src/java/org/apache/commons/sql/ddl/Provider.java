package org.apache.commons.sql.ddl;

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
