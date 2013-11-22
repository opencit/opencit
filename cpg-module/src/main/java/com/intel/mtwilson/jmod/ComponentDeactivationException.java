/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jmod;

/**
 * TODO :  use cpg-i18n message instead of string.
 * @author jbuhacoff
 */
public class ComponentDeactivationException extends Exception {
    public ComponentDeactivationException() {
        super();
    }
    public ComponentDeactivationException(Throwable cause) {
        super(cause);
    }
    public ComponentDeactivationException(String message) {
        super(message);
    }
    public ComponentDeactivationException(String message, Throwable cause) {
        super(message, cause);
    }
}
