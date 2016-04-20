/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.creator.impl;

import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;

/**
 *
 * @author jbuhacoff
 */
public class InsecureTlsPolicyCreator implements TlsPolicyCreator {

    @Override
    public InsecureTlsPolicy createTlsPolicy(TlsPolicyDescriptor tlsPolicyDescriptor) {
        if( "INSECURE".equalsIgnoreCase(tlsPolicyDescriptor.getPolicyType()) ) {
            return new InsecureTlsPolicy();
        }
        return null;
    }
}
