/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import com.intel.dcsg.cpg.tls.policy.ProtocolSelector;

/**
 *
 * @author jbuhacoff
 */
public class AnyProtocolSelector implements ProtocolSelector {

    @Override
    public boolean accept(String protocolName) {
        return true;
    }

    @Override
    public String preferred() {
        return "TLS";
    }
    
}
