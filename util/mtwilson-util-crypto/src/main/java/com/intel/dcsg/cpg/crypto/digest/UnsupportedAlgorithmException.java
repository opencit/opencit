/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.digest;

/**
 *
 * @author jbuhacoff
 */
public class UnsupportedAlgorithmException extends UnsupportedOperationException {

    public UnsupportedAlgorithmException(String algorithm) {
        super(algorithm);
    }

    public UnsupportedAlgorithmException(String algorithm, Throwable cause) {
        super(algorithm, cause);
    }
    
}
