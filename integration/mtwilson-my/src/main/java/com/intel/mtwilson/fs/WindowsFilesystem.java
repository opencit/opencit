/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.fs;

/**
 *
 * @author jbuhacoff
 */
public class WindowsFilesystem extends AbstractFilesystem {

    @Override
    protected String getDefaultConfigurationPath() {
        return "C:\\mtwilson\\configuration";
    }

    @Override
    protected String getDefaultApplicationPath() {
        return "C:\\mtwilson";
    }
}
