/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.provider;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.My;
import com.intel.mtwilson.tls.policy.TlsPolicyChoice;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyProvider;

/**
 * Loads the global TLS Policy for the server, configured as the value of 
 * mtwilson.global.tls.policy.id in mtwilson.properties or in the cluster
 * configuration from database
 * 
 * @author jbuhacoff
 */
public class GlobalTlsPolicyProvider implements TlsPolicyProvider {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalTlsPolicyProvider.class);
    
    @Override
    public TlsPolicyChoice getTlsPolicyChoice() {
        String globalTlsPolicyId = My.configuration().getGlobalTlsPolicyId();
        log.debug("GlobalTlsPolicy: {}", globalTlsPolicyId);
        if( globalTlsPolicyId == null || globalTlsPolicyId.isEmpty()) {
            return null;
        }
        if( globalTlsPolicyId.equals("INSECURE") || globalTlsPolicyId.equals("TRUST_FIRST_CERTIFICATE") ) {
            TlsPolicyChoice tlsPolicyChoice = new TlsPolicyChoice();
            tlsPolicyChoice.setTlsPolicyDescriptor(new TlsPolicyDescriptor());
            tlsPolicyChoice.getTlsPolicyDescriptor().setPolicyType(globalTlsPolicyId);
            return tlsPolicyChoice;
        }
        if( UUID.isValid(globalTlsPolicyId)) {
            TlsPolicyChoice tlsPolicyChoice = new TlsPolicyChoice();
            tlsPolicyChoice.setTlsPolicyId(globalTlsPolicyId);
            return tlsPolicyChoice;
        }
        log.debug("GlobalTlsPolicy not found");
        return null;
    }
}
