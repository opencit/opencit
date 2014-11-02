/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

/**
 *
 * @author jbuhacoff
 */
public class ExtensionNotFoundException extends UnsupportedOperationException {
    public ExtensionNotFoundException(String message) {
        super(message);
    }
    public ExtensionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
