/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.fs;

/**
 *
 * @author jbuhacoff
 */
public class UnixFilesystem extends AbstractFilesystem {

    @Override
    protected String getDefaultConfigurationPath() {
//        return "/etc/mtwilson"; // installer can symlink this to /opt/mtwilson/configuration
        return "/opt/mtwilson/configuration";
    }

    @Override
    protected String getDefaultApplicationPath() {
        return "/opt/mtwilson";
    }
}
