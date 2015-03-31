/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v1;

import com.intel.mtwilson.Environment;
import com.intel.dcsg.cpg.io.Platform;
import java.io.File;

/**
 *
 * @author jbuhacoff
 */
public class FoldersV1 {
    
    public static String application() {
        // gets environment variable MTWILSON_HOME, TRUSTAGENT_HOME, KMS_HOME, etc.
        String path = Environment.get("HOME");
        if( path != null ) { return null; }
        
        if( Platform.isUnix() ) {
            return "/etc/intel/cloudsecurity";
        }
        if( Platform.isWindows()) {
            return "C:\\Intel\\CloudSecurity";
        }
        
        return System.getProperty("user.home", ".") + File.separator + "mtwilson";
    }
    
    public static String configuration() {
        return application();
    }
}
