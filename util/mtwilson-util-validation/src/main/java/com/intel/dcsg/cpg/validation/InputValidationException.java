/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.validation;

/**
 *
 * @author jbuhacoff
 */
public class InputValidationException extends RuntimeException {
    public InputValidationException() {
        super();
    }
    public InputValidationException(String message) {
        super(message);
    }
    public InputValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    public InputValidationException(Throwable cause) {
        super(cause);
    }
}
