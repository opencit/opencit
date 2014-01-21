package com.intel.mtwilson.model;

import com.intel.mtwilson.datatypes.OemData;
import com.intel.dcsg.cpg.validation.ObjectModel;

/**
 * Representation of a Bios record comprised of Name, Version, and OEM.
 * 
 * @since 0.5.1
 * @author jbuhacoff
 */
public class Bios extends ObjectModel {

    private String name = null;
    private String version = null;

    public String getOem() {
        return oem;
    }

    public final void setOem(String value) {
        this.oem = value;
    }
    private String oem = null;

    public Bios(String name, String version, String oemName) {
        setName(name);
        setVersion(version);
        setOem(oemName);
    }
    
    public Bios(String name, String version, OemData oem) {
        setName(name);
        setVersion(version);
        setOem(oem.getName());
    }

    public final void setName(String value) {
        name = value;
    }

    public final void setVersion(String value) {
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
        if(version == null || version.isEmpty()) {
            fault("BIOS Version is missing");
        }
        if (name == null || name.isEmpty()) {
            fault("BIOS Name is missing");
        }
        if (oem == null || oem.isEmpty()) {
            fault("BIOS Oem is missing");
        }
    }
}
