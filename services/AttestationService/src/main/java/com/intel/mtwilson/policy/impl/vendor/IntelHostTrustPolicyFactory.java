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
 * Needs to create a policy to check AIK Certificate is signed by trusted Privacy CA
 * @author jbuhacoff
 */
public class IntelHostTrustPolicyFactory implements VendorHostTrustPolicyFactory {
    private JpaPolicyReader util;
    public IntelHostTrustPolicyFactory(JpaPolicyReader util) {
        this.util = util;
    }

    @Override
    public Set<Rule> generateTrustRulesForHost(TblHosts host, HostReport hostReport) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Rule> loadTrustRulesForBios(Bios bios, TblHosts host) {
        return util.loadPcrMatchesConstantRulesForBios(bios, host);
    }

    /**
     * XXX TODO  the linux hosts have modules extended into pcr 19... need to add support for that.
     * @param vmm
     * @param host
     * @return 
     */
    @Override
    public Set<Rule> loadTrustRulesForVmm(Vmm vmm, TblHosts host) {
        return util.loadPcrMatchesConstantRulesForVmm(vmm, host);
    }

    @Override
    public Set<Rule> loadTrustRulesForLocation(String location, TblHosts host) {
        return util.loadPcrMatchesConstantRulesForLocation(location, host);
    }

}
