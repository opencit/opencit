/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import com.intel.mtwilson.fs.ApplicationFilesystem;
import com.intel.dcsg.cpg.io.Platform;
import com.intel.mtwilson.fs.*;

/**
 *
 * @author jbuhacoff
 */
public class MyFilesystem {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MyFilesystem.class);

    public static ApplicationFilesystem getApplicationFilesystem() {
        /*
        if (Platform.isUnix()) {
            return new UnixFilesystem();
        }
        if (Platform.isWindows()) {
            return new WindowsFilesystem();
        }
        return new RelativeFilesystem();
        */
        try {
            return new ConfigurableFilesystem(My.configuration().getConfiguration());
        }
        catch(Exception e) {
        if (Platform.isUnix()) {
            return new UnixFilesystem();
        }
        if (Platform.isWindows()) {
            return new WindowsFilesystem();
        }
        return new RelativeFilesystem();
        
    }
    }







}
