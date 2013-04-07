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
 * Needs to create a policy to check AIK Public Key
 * 
 * @author jbuhacoff
 */
public class CitrixHostTrustPolicyFactory implements VendorHostTrustPolicyFactory {

    @Override
    public List<TrustPolicy> createTrustPolicyWhitelistFromHost(TblHosts host, HostReport hostReport) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
