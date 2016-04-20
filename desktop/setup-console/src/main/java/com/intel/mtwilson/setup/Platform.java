/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

/**
 *
 * @author jbuhacoff
 */
public class Platform {
    
    private static boolean isWindows = false;
    private static boolean isUnix = false;
    
    static {
        String os = System.getProperty("os.name").toLowerCase();
        if( os.indexOf( "win" ) >= 0 ) {
            isWindows = true; 
            isUnix = false;
        }
        else if( os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0 ) {
            isWindows = false;
            isUnix = true;
        }
    }
    
    public static boolean isWindows() { return isWindows; }
    public static boolean isUnix() { return isUnix; }
    
}
