/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.impl.vendor;

import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.TrustPolicy;
import com.intel.mtwilson.policy.impl.VendorHostTrustPolicyFactory;
import java.util.List;

/**
 * Needs to create a policy to check AIK Certificate is signed by trusted Privacy CA
 * @author jbuhacoff
 */
public class IntelHostTrustPolicyFactory implements VendorHostTrustPolicyFactory {

    @Override
    public List<TrustPolicy> createTrustPolicyWhitelistFromHost(TblHosts host, HostReport hostReport) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
