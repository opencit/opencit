/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

/**
 * TODO :  use cpg-i18n message instead of string.
 * @author jbuhacoff
 */
public class ComponentActivationException extends Exception {
    public ComponentActivationException() {
        super();
    }
    public ComponentActivationException(Throwable cause) {
        super(cause);
    }
    public ComponentActivationException(String message) {
        super(message);
    }
    public ComponentActivationException(String message, Throwable cause) {
        super(message, cause);
    }
}
