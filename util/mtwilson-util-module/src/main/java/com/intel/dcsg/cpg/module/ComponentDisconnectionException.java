/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

/**
 * @author jbuhacoff
 */
public class ComponentDisconnectionException extends ContainerException {
    public ComponentDisconnectionException() {
        super();
    }
    public ComponentDisconnectionException(Throwable cause) {
        super(cause);
    }
    public ComponentDisconnectionException(String message) {
        super(message);
    }
    public ComponentDisconnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
