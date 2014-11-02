/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

/**
 *
 * @author jbuhacoff
 */
public class VersionRange {
    private Version min;
    private Version max;
    private End minEnd = End.CLOSED;
    private End maxEnd = End.OPEN;
    
    public static enum End { OPEN, CLOSED; }
    
    public VersionRange(Version min, Version max) {
        this.min = min;
        this.max = max;
    }

    public VersionRange(Version min, End minEnd, Version max, End maxEnd) {
        this.min = min;
        this.max = max;
        if( minEnd != null ) { this.minEnd = minEnd; }
        if( maxEnd != null ) { this.maxEnd = maxEnd; }
    }
    
    public boolean contains(Version version) {
        return 
                (( minEnd.equals(End.CLOSED) && min.le(version) ) || ( minEnd.equals(End.CLOSED) && min.lt(version) ))
                &&
                (( maxEnd.equals(End.CLOSED) && min.ge(version) ) || ( minEnd.equals(End.CLOSED) && max.gt(version) ));
    }
    
    @Override
    public String toString() {
        String startSymbol = null, endSymbol = null;
        if( minEnd.equals(End.CLOSED )) { startSymbol = "["; }
        if( minEnd.equals(End.OPEN )) { startSymbol = "("; }
        if( maxEnd.equals(End.CLOSED )) { endSymbol = "]"; }
        if( maxEnd.equals(End.OPEN )) { endSymbol = ")"; }
        return startSymbol + min.toString() + "," + max.toString() + endSymbol;
    }
    
    // input like [1.1,2.0)   or just [1.1]  as a shortcut to [1.1,1.1]
    public static VersionRange valueOf(String text) {
        Version min = null, max = null;
        End minEnd = null, maxEnd = null;
        String[] parts = text.split(",");
        if( parts.length > 2 ) { throw new IllegalArgumentException("Invalid version range format"); } // or just return null ? 
        if( parts.length == 2 ) {
            min = Version.valueOf(parts[0].substring(1));
            max = Version.valueOf(parts[1].substring(0,parts[1].length()-1));
        }
        if( parts.length == 1 ) {
            min = Version.valueOf(parts[0].substring(1));
            max = Version.valueOf(parts[0].substring(0,parts[0].length()-1));
        }
        if( text.startsWith("[") ) { minEnd = End.CLOSED; }
        if( text.startsWith("(")) { minEnd = End.OPEN; }
        if( text.endsWith("]")) { maxEnd = End.CLOSED; }
        if( text.endsWith(")")) { maxEnd = End.OPEN; }
        return new VersionRange(min,minEnd,max,maxEnd);
    }
}
