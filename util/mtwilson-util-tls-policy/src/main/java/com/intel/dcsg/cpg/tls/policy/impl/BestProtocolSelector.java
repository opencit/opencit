/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import com.intel.dcsg.cpg.tls.policy.ProtocolSelector;

/**
 * TODO: this implementation must be updated as better protocols become available.
 * 
 * @author jbuhacoff
 */
public class BestProtocolSelector implements ProtocolSelector {

    @Override
    public boolean accept(String protocolName) {
        return "TLSv1.2".equals(protocolName);
    }

    @Override
    public String preferred() {
        return "TLSv1.2";
    }
    
}
