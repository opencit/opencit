/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

/**
 * @author jbuhacoff
 */
public class ComponentActivationException extends ContainerException {
    public ComponentActivationException() {
        super();
    }
    public ComponentActivationException(Throwable cause) {
        super(cause);
    }
    public ComponentActivationException(String message) {
        super(message);
    }
    public ComponentActivationException(String message, Throwable cause) {
        super(message, cause);
    }
}
