/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.patch;

/**
 *
 * @author jbuhacoff
 */
public class PatchException extends Exception {
    public PatchException() {
        super();
    }
    public PatchException(String message) {
        super(message);
    }
    public PatchException(String message, Throwable cause) {
        super(message, cause);
    }
    public PatchException(Throwable cause) {
        super(cause);
    }
}
