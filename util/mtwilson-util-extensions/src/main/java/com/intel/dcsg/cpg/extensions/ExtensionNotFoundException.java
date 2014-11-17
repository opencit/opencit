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
    /**
     * 
     * @param message typically the name of the extension, such as a fully qualified java class name
     */
    public ExtensionNotFoundException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message typically the name of the extension, such as a fully qualified java class name
     * @param cause 
     */
    public ExtensionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
