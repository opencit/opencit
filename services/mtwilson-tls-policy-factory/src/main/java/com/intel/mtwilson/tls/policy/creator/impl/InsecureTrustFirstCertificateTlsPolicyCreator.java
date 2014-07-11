/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.creator.impl;

import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.dcsg.cpg.tls.policy.TrustDelegate;
import com.intel.dcsg.cpg.tls.policy.impl.FirstPublicKeyTrustDelegate;
import com.intel.dcsg.cpg.x509.repository.HashSetMutablePublicKeyRepository;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;

/**
 *
 * @author jbuhacoff
 */
public class InsecureTrustFirstCertificateTlsPolicyCreator implements TlsPolicyCreator {

    @Override
    public TlsPolicy createTlsPolicy(TlsPolicyDescriptor tlsPolicyDescriptor) {
        if( "TRUST_FIRST_CERTIFICATE".equalsIgnoreCase(tlsPolicyDescriptor.getPolicyType()) ) {
            HashSetMutablePublicKeyRepository repository = new HashSetMutablePublicKeyRepository();
            TrustDelegate delegate = new FirstPublicKeyTrustDelegate(repository);
            return TlsPolicyBuilder.factory().strict(repository).trustDelegate(delegate).skipHostnameVerification().build();
            // XXX TODO... need to tie the mutable public key repository back to the host record so it will be saved when it's written to....
//            return new InsecureTlsPolicy();
        }
        return null;
    }
}
