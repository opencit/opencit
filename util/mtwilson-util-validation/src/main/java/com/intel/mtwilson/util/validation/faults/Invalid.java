/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.validation.faults;

import com.intel.dcsg.cpg.validation.Fault;

/**
 *
 * @author jbuhacoff
 */
public class Invalid extends Fault {
    private Object object;
    
    public Invalid(Object object) {
        super(object.getClass().getName());
        this.object = object;
    }
    public Invalid(Object object, String format, Object... args) {
        super(format, args);
        this.object = object;
    }

    public Object getInvalid() {
        return object;
    }
    
    
}
