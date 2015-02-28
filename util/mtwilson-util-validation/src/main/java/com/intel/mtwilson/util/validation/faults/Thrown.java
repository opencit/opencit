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
public class Thrown extends Fault {
    private Throwable cause;
    public Thrown(Throwable cause) {
        super(String.format("[%s: %s]", cause.getClass().getName(), cause.getMessage()));
        this.cause = cause;
        if( cause.getCause() != null ) {
            fault(cause.getCause());
        }
    }
    public Thrown(Throwable cause, String description) {
        super(description);
        this.cause = cause;
        if( cause.getCause() != null ) {
            fault(cause.getCause());
        }
    }
    public Thrown(Throwable cause, String format, Object... args) {
        super(format, args);
        this.cause = cause;
        if( cause.getCause() != null ) {
            fault(cause.getCause());
        }
    }

    public Throwable getCause() {
        return cause;
    }
    
}
