package org.apache.commons.sql.ddl;

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
