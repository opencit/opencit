/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.fs;

/**
 *
 * @author jbuhacoff
 */
public interface PlatformFilesystem {
    /**
     * Directory where applications should be installed.
     * Example values are /opt or /usr/share in Linux
     * and C:\ or C:\Program Files in Windows.
     * @return 
     */
    String getApplicationRoot();
}
