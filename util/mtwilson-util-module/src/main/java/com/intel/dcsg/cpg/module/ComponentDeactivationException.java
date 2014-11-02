/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

/**
 * @author jbuhacoff
 */
public class ComponentDeactivationException extends ContainerException {
    public ComponentDeactivationException() {
        super();
    }
    public ComponentDeactivationException(Throwable cause) {
        super(cause);
    }
    public ComponentDeactivationException(String message) {
        super(message);
    }
    public ComponentDeactivationException(String message, Throwable cause) {
        super(message, cause);
    }
}
