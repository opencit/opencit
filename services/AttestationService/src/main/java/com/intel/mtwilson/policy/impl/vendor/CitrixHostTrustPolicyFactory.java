/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.impl.vendor;

import com.intel.mtwilson.policy.impl.JpaPolicyReader;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.model.Bios;
import com.intel.mtwilson.model.Vmm;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.Rule;
import com.intel.mtwilson.policy.impl.VendorHostTrustPolicyFactory;
import java.util.List;
import java.util.Set;

/**
 * Needs to create a policy to check AIK Public Key
 * 
 * @author jbuhacoff
 */
public class CitrixHostTrustPolicyFactory implements VendorHostTrustPolicyFactory {
    private JpaPolicyReader util;
    public CitrixHostTrustPolicyFactory(JpaPolicyReader util) {
        this.util = util;
    }
    
    @Override
    public Set<Rule> generateTrustRulesForHost(TblHosts host, HostReport hostReport) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    @Override
    public Set<Rule> loadTrustRulesForBios(Bios bios, TblHosts host) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Rule> loadTrustRulesForVmm(Vmm vmm, TblHosts host) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Rule> loadTrustRulesForLocation(String location, TblHosts host) {
        return util.loadPcrMatchesConstantRulesForLocation(location, host);
    }
    
}
