/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

/**
 * @author jbuhacoff
 */
public class ComponentExportException extends ContainerException {
    public ComponentExportException() {
        super();
    }
    public ComponentExportException(Throwable cause) {
        super(cause);
    }
    public ComponentExportException(String message) {
        super(message);
    }
    public ComponentExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
