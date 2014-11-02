/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.authz.token;

/**
 *
 * @since 0.1
 * @author jbuhacoff
 */
public class UnsupportedTokenVersionException extends Exception {
    private byte version;
    public UnsupportedTokenVersionException(byte version) {
        super(String.format("Unsupported token version %x", version));
        this.version = version;
    }
    public UnsupportedTokenVersionException(byte version, Throwable e) {
        super(String.format("Unsupported token version %x", version), e);
        this.version = version;
    }
    public byte getVersion() { return version; }
}
