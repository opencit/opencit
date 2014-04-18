/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.fs;

/**
 *
 * @author jbuhacoff
 */
public class WindowsFilesystem implements PlatformFilesystem {

    /**
     * Return the location where applications should be installed.
     * Even though the basic implementation of this is simply C:, 
     * another implementation could return C:\Program Files
     * or another location. So it is not an equivalent to the
     * filesystem root.
     * @return 
     */
    @Override
    public String getApplicationRoot() {
        return "C:"; // when paths are constructed the file separator will always be appended to this
    }    
}
