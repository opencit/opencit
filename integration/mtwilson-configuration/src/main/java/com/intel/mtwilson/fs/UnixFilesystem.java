/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.fs;

/**
 *
 * @author jbuhacoff
 */
public class UnixFilesystem implements PlatformFilesystem {

    /**
     * Return the location where applications should be installed.
     * Even though the basic implementation of this is /opt, 
     * another implementation could return /usr/share or /usr/local.
     * So it is not an equivalent to the
     * filesystem root.
     * @return 
     */
    @Override
    public String getApplicationRoot() {
        return "/opt";
    }
}
