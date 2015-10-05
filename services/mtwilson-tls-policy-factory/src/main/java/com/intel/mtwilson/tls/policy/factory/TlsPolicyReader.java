/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.factory;

import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;

/**
 *
 * @author jbuhacoff
 */
public interface TlsPolicyReader {
    /**
     * 
     * @param contentType for example "application/java-keystore" or "application/json"
     * @return true if the reader can read that content type
     */
    boolean accept(String contentType);
    TlsPolicyDescriptor read(byte[] content);
}
