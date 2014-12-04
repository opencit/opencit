/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.filesystem;

import java.io.File;

/**
 *
 * @author jbuhacoff
 */
public class Home extends ApplicationFolder {
    private final String app;
    private final String property;
    private final String environment;
    public Home() {
        super();
        app = getApplicationName(); // like "mtwilson" or "MtWilson"
        property = app.toLowerCase()+".home"; // like "mtwilson.home"
        environment = app.toUpperCase()+"_HOME"; // like "MTWILSON_HOME"
    }
    
    @Override
    public String getPropertyName() {
        return property;
    }
    
    @Override
    public String getEnvironmentName() {
        return environment;
    }
    
    @Override
    public String getDefaultPath() {
        // if the home or current directory is /opt/mtwilson or /opt/mtwilson-3.0
        // or /etc/mtwilson
        // or /home/mtwilson on linux, or c:/mtwilson or c:/program files/mtwilson
        // on windows, then use it directly
        String home = System.getProperty("user.home");
        if( home != null && home.toLowerCase().contains(File.separator+app) ) {
            return home;
        }
        if( Platform.isUnix() ) {
            return "/opt/"+app; // like /opt/mtwilson
        }
        if( Platform.isWindows() ) {
            return "C:"+File.separator+app; // like C:/mtwilson 
        }
        // anywhere else, like /root or /home/someone we use a "dot" subfolder
        if( home != null ) {
            return home + File.separator + "." + app; // like ~/.mtwilson
        }
        // if there's no home directory defined, use a "dot" subfolder in the
        // current folder
        return System.getProperty("user.dir", ".") + File.separator + "." + app;
    }
    
}
