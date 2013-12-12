/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

/**
 * TODO :  use cpg-i18n message instead of string.
 * @author jbuhacoff
 */
public class ComponentDisconnectionException extends Exception {
    public ComponentDisconnectionException() {
        super();
    }
    public ComponentDisconnectionException(Throwable cause) {
        super(cause);
    }
    public ComponentDisconnectionException(String message) {
        super(message);
    }
    public ComponentDisconnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
