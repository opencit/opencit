package com.intel.mtwilson.datatypes;

import org.apache.commons.lang3.Validate;

/**
 * Representation of a Vmm comprised of Name, Version, OS Name, and OS Version.
 * 
 * @since 0.5.1
 * @author jbuhacoff
 */
public final class Vmm {

    private String name = null;
    private String version = null;
    private String osName = null;
    private String osVersion = null;

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public Vmm(String name, String version,String osName, String osVersion ) {
        if (name == null || name.isEmpty() || version == null || version.isEmpty()) {
            throw new IllegalArgumentException("VMM name or version is missing");
        }
        if (osName == null || osName.isEmpty() || osVersion == null || osVersion.isEmpty()) {
            throw new IllegalArgumentException("VMM OS name or OS version is missing");
        }
        setName(name);
        setVersion(version);
        setOsName(osName);
        setOsVersion(osVersion);
    }

    public final void setName(String value) {
        Validate.notNull(value);
        name = value;
    }

    public final void setVersion(String value) {
        Validate.notNull(value);
        version = value;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String toString() {
        return String.format("%s:%s", name, version);
    }
}
