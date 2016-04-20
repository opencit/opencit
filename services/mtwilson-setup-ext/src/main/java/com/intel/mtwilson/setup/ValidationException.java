/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

/**
 * Thrown when a setup task cannot validate an item because of some external
 * problem such as unable to read from disk or network error.
 * Configuration issues do not cause this exception; they get logged as 
 * configuration faults by the setup task.
 * 
 * @author jbuhacoff
 */
public class ValidationException extends SetupException {
    public ValidationException() {
        super();
    }
    public ValidationException(Throwable e) {
        super(e);
    }
    public ValidationException(String message) {
        super(message);
    }
    public ValidationException(String message, Throwable e) {
        super(message, e);
    }
    
}
