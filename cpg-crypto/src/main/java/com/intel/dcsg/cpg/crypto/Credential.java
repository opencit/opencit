/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

/**
 *
 * @since 0.5.2
 * @author jbuhacoff
 */
public interface Credential {
    byte[] identity();
    byte[] signature(byte[] document) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException;
    String algorithm();
}
