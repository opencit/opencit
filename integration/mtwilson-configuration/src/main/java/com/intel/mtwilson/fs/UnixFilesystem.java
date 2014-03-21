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

    @Override
    public String getApplicationRoot() {
        return "/opt";
    }
}
