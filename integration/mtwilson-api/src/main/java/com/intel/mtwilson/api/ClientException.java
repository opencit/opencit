/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.api;

/**
 * @since 0.5.4
 * @author jbuhacoff
 */
public class ClientException extends Exception {
    public ClientException() {
        super();
    }
    public ClientException(Throwable cause) {
        super(cause);
    }
    public ClientException(String message) {
        super(message);
    }
    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
