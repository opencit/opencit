/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

/**
 *
 * @author jbuhacoff
 */
public class SetupException extends Exception {
    public SetupException() {
        super();
    }
    public SetupException(Throwable cause) {
        super(cause);
    }
    public SetupException(String message) {
        super(message);
    }
    public SetupException(String message, Throwable cause) {
        super(message, cause);
    }
}
