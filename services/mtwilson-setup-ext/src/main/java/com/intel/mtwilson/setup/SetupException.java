/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

/**
 * Base class for all setup-related exceptions in this project
 * 
 * @author jbuhacoff
 */
public class SetupException extends RuntimeException {
    public SetupException() {
        super();
    }
    public SetupException(Exception e) {
        super(e);
    }
    public SetupException(String message) {
        super(message);
    }
    public SetupException(String message, Exception e) {
        super(message, e);
    }
}
