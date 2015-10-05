/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.factory;

import com.intel.mtwilson.tls.policy.TlsPolicyChoice;

/**
 *
 * @author jbuhacoff
 */
public interface TlsPolicyProvider {
    /**
     * 
     * @return a TlsPolicyChoice instance with one of tlsPolicyId, tlsPolicyName, or tlsPolicyDescriptor set; or null if no choice is defined
     */
    TlsPolicyChoice getTlsPolicyChoice();
}
