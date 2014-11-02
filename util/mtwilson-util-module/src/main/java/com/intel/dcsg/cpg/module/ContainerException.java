/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

/**
 * @author jbuhacoff
 */
public class ContainerException extends Exception {
    public ContainerException() {
        super();
    }
    public ContainerException(Throwable cause) {
        super(cause);
    }
    public ContainerException(String message) {
        super(message);
    }
    public ContainerException(String message, Throwable cause) {
        super(message, cause);
    }
}
