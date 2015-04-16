/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.faults;

import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.validation.Faults;

/**
 *
 * @author jbuhacoff
 */
public class ConfigurationFault extends Fault {
    
    public ConfigurationFault(String description) {
        super(description);
    }
    
    public ConfigurationFault(String format, Object... args) {
        super(format, args);
    }
    
    public ConfigurationFault(Throwable e, String description) {
        super(e, description);
    }
    
    public ConfigurationFault(Throwable e, String format, Object... args) {
        super(e, format, args);
    }

    public ConfigurationFault(Faults related, String format, Object... args) {
        super(related, format, args);
    }
    
}
