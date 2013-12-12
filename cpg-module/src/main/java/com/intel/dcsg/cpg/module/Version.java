/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

/**
 *
 * @author jbuhacoff
 */
public class Version {
    private final Integer major;
    private final Integer minor;
    private final Integer patch;
    private final String classifier;
    public Version(Integer major, Integer minor, Integer patch, String classifier) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.classifier = classifier;
    }
    
    public Version(Integer major, Integer minor, String classifier) {
        this.major = major;
        this.minor = minor;
        this.patch = null;
        this.classifier = classifier;
    }
    
    public Version(Integer major, Integer minor, Integer patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.classifier = null;
    }
    
    public Version(Integer major, Integer minor) {
        this.major = major;
        this.minor = minor;
        this.patch = null;
        this.classifier = null;
    }
    
    public boolean lesserThan(Version other) {
        return major < other.major || ( major == other.major && minor < other.minor ) ||
                ( major == other.major && minor == other.minor && patch < other.patch );
    }
    public boolean lt(Version other) { return lesserThan(other); }

    public boolean lesserThanOrEqualTo(Version other) {
        return major <= other.major || ( major == other.major && minor <= other.minor ) ||
                ( major == other.major && minor == other.minor && patch <= other.patch );
    }
    public boolean le(Version other) { return lesserThanOrEqualTo(other); }
    
    public boolean greaterThan(Version other) {
        return major > other.major || ( major == other.major && minor > other.minor ) ||
                ( major == other.major && minor == other.minor && patch > other.patch );
    }
    public boolean gt(Version other) { return greaterThan(other); }

    public boolean greaterThanOrEqualTo(Version other) {
        return major >= other.major || ( major == other.major && minor >= other.minor ) ||
                ( major == other.major && minor == other.minor && patch >= other.patch );
    }
    public boolean ge(Version other) { return greaterThanOrEqualTo(other); }
    
    @Override
    public String toString() {
        return major+"."+minor+(patch==null?"":"."+patch)+(classifier==null?"":"-"+classifier);
    }
    
    // input like  0.1  or 1.2-SNAPSHOT  or 1.2.3  or 2.3.4-RELEASE    
    public static Version valueOf(String text) {
        Integer major=null, minor=null, patch=null;
        String classifier=null;
        String[] versionAndClassifier = text.split("-");        
        if( versionAndClassifier.length == 2 ) {
            classifier = versionAndClassifier[1];
        }
        String[] versionParts = versionAndClassifier[0].split("\\.");
        if( versionParts.length >= 3 ) {
            patch = Integer.valueOf(versionParts[2]);
        }
        if( versionParts.length >= 2 ) {
            minor = Integer.valueOf(versionParts[1]);
        }
        if( versionParts.length >= 1 ) {
            major = Integer.valueOf(versionParts[0]);
        }
        return new Version(major,minor,patch,classifier);
    }

    // XXX TODO   hashCode and equals   by comparing major=major, minor=minor, patch=patch, classifier=classifier
    
}
