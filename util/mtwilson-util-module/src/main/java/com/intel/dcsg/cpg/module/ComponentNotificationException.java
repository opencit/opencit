/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

/**
 * @author jbuhacoff
 */
public class ComponentNotificationException extends ContainerException {
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
