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
public class MissingEnvironmentVariable extends Fault {
    private String name;

    public MissingEnvironmentVariable(String name) {
        super(name);
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    
}
