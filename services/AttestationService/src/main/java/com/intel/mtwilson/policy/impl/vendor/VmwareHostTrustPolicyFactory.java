/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.impl.vendor;

import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.Rule;
import com.intel.mtwilson.policy.impl.VendorHostTrustPolicyFactory;
import java.util.List;

/**
 * Needs to create a policy to check custom vmware flags in the report to indicate if the pcr and event measurements are valid
 * @author jbuhacoff
 */
public class VmwareHostTrustPolicyFactory implements VendorHostTrustPolicyFactory {

    @Override
    public List<Rule> createTrustPolicyWhitelistFromHost(TblHosts host, HostReport hostReport) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
