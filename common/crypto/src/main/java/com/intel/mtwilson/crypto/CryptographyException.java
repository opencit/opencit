/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.crypto;

/**
 *
 * @author jbuhacoff
 */
public class CryptographyException extends Exception {
    public CryptographyException() {
        super();
    }
    public CryptographyException(Throwable cause) {
        super(cause);
    }
    public CryptographyException(String message) {
        super(message);
    }
    public CryptographyException(String message, Throwable cause) {
        super(message, cause);
    }
}
