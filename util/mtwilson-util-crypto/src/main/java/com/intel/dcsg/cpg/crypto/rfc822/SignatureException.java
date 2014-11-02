/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.rfc822;

/**
 *
 * @author jbuhacoff
 */
public class SignatureException extends RuntimeException {
    public SignatureException() {
        super();
    }
    public SignatureException(String message) {
        super(message);
    }
    public SignatureException(Throwable cause) {
        super(cause);
    }
    public SignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}
