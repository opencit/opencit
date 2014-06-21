/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.reader.impl;

import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyReader;

/**
 *
 * @author jbuhacoff
 */
public class InsecureDescriptor implements TlsPolicyReader {

    @Override
    public TlsPolicy createTlsPolicy(TlsPolicyDescriptor tlsPolicyDescriptor) {
        if( "INSECURE".equalsIgnoreCase(tlsPolicyDescriptor.getName()) ) {
            return new InsecureTlsPolicy();
        }
        return null;
    }
}
