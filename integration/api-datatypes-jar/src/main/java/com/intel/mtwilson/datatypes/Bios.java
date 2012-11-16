package com.intel.mtwilson.datatypes;

/**
 * Representation of a Bios record comprised of Name, Version, and OEM.
 * 
 * @since 0.5.1
 * @author jbuhacoff
 */
public class Bios {

    private String name = null;
    private String version = null;

    public String getOem() {
        return oem;
    }

    public final void setOem(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("BIOS Oem is missing");
        }
        this.oem = value;
    }
    private String oem = null;

    public Bios(String name, String version, String oemName) {
        setName(name);
        setVersion(version);
        setOem(oemName);
    }

    public final void setName(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("BIOS Name is missing");
        }
        name = value;
    }

    public final void setVersion(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("BIOS Version is missing");
        }
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
