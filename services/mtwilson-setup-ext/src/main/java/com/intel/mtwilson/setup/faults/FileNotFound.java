/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.faults;

import com.intel.dcsg.cpg.validation.Fault;

/**
 *
 * @author jbuhacoff
 */
public class FileNotFound extends Fault {
    private String path;

    public FileNotFound(String path) {
        super(path);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
    
    
}
