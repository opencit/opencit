/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jmod;

/**
 * TODO :  use cpg-i18n message instead of string.
 * @author jbuhacoff
 */
public class ComponentNotificationException extends Exception {
    public ComponentNotificationException() {
        super();
    }
    public ComponentNotificationException(Throwable cause) {
        super(cause);
    }
    public ComponentNotificationException(String message) {
        super(message);
    }
    public ComponentNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
