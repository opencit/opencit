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
public class ValidationFault extends Fault {
    public ValidationFault(String description) {
        super(description);
    }
    public ValidationFault(String format, Object... args) {
        super(format, args);
    }
    
    public ValidationFault(Throwable e, String description) {
        super(e, description);
    }
    
    public ValidationFault(Throwable e, String format, Object... args) {
        super(e, format, args);
    }

    public ValidationFault(Faults related, String format, Object... args) {
        super(related, format, args);
    }
    
}
