/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.extensions.cache;

import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author jbuhacoff
 */
public class AppJarsFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
        return pathname.getName().contains("mtwilson");
    }
    
}
