package com.intel.mtwilson.model;

import com.intel.mtwilson.datatypes.OsData;
import com.intel.dcsg.cpg.validation.ObjectModel;
import org.apache.commons.lang3.Validate;

/**
 * Representation of a Vmm comprised of Name, Version, OS Name, and OS Version.
 * 
 * @since 0.5.1
 * @author jbuhacoff
 */
public final class Vmm extends ObjectModel {

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
        setName(name);
        setVersion(version);
        setOsName(osName);
        setOsVersion(osVersion);
    }
    
    public Vmm(String name, String version, OsData os) {
        setName(name);
        setVersion(version);
        setOsName(os.getName());
        setOsVersion(os.getVersion());
    }

    public final void setName(String value) {
        //Validate.notNull(value);
        name = value;
    }

    public final void setVersion(String value) {
        //Validate.notNull(value);
        version = value;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", name, version);
    }
    
    @Override
    protected void validate() {
        if (name == null || name.isEmpty() ) {
            fault("VMM name is missing");
        }
        if ( version == null || version.isEmpty()) {
            fault("VMM version is missing");
        }
        if (osName == null || osName.isEmpty()) {
            fault("VMM OS name is missing");
        }        
        if ( osVersion == null || osVersion.isEmpty()) {
            fault("VMM OS version is missing");
        }        
    }
}
