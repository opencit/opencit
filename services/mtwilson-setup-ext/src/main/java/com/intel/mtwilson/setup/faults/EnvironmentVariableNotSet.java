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
public class EnvironmentVariableNotSet extends Fault {
    private String name;

    public EnvironmentVariableNotSet(String name) {
        super(name);
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    
}
