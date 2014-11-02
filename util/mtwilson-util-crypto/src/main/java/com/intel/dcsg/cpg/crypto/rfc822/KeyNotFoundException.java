/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.rfc822;

import java.security.KeyManagementException;

/**
 *
 * @author jbuhacoff
 */
public class KeyNotFoundException extends KeyManagementException {
    private String keyId = null;;
    public KeyNotFoundException() {
        super();
    }
    public KeyNotFoundException(Throwable cause) {
        super(cause);
    }
    public KeyNotFoundException(String keyId) {
        super(String.format("Key not found: "+keyId));
        this.keyId = keyId;
    }
    public KeyNotFoundException(String keyId, Throwable cause) {
        super(String.format("Key not found: "+keyId), cause);
        this.keyId = keyId;
    }    
    public String getKeyId() { return keyId; }
}
