/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

/**
 * @author jbuhacoff
 */
public class ModuleDeactivationException extends ContainerException {
    public ModuleDeactivationException() {
        super();
    }
    public ModuleDeactivationException(Throwable cause) {
        super(cause);
    }
    public ModuleDeactivationException(String message) {
        super(message);
    }
    public ModuleDeactivationException(String message, Throwable cause) {
        super(message, cause);
    }
}
