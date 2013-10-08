/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.impl.vendor;

import com.intel.mtwilson.as.data.MwAssetTagCertificate;
import com.intel.mtwilson.policy.impl.JpaPolicyReader;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.model.Bios;
import com.intel.mtwilson.model.Vmm;
import com.intel.mtwilson.policy.Rule;
import com.intel.mtwilson.policy.impl.VendorHostTrustPolicyFactory;
import java.util.HashSet;
import java.util.Set;

/**
 * Needs to create a policy to check AIK Certificate is signed by trusted Privacy CA
 * @author jbuhacoff
 */
public class IntelHostTrustPolicyFactory implements VendorHostTrustPolicyFactory {
    private JpaPolicyReader reader;
    public IntelHostTrustPolicyFactory(JpaPolicyReader util) {
        this.reader = util;
    }

    @Override
    public Set<Rule> loadTrustRulesForBios(Bios bios, TblHosts host) {
        return reader.loadPcrMatchesConstantRulesForBios(bios, host);
    }

    @Override
    public Set<Rule> loadTrustRulesForVmm(Vmm vmm, TblHosts host) {
        HashSet<Rule> rules = new HashSet<Rule>();
        // first, load the list of pcr's marked for this host's vmm mle 
        Set<Rule> pcrConstantRules = reader.loadPcrMatchesConstantRulesForVmm(vmm, host);
        rules.addAll(pcrConstantRules);

        // Next we need to add all the modules
        if( host.getVmmMleId().getRequiredManifestList().contains("19") ) {
            Set<Rule> pcrEventLogRules = reader.loadPcrEventLogIncludesRuleForVmm(vmm, host);
            rules.addAll(pcrEventLogRules);
        }
        return rules;    
    }

    // Since the open source tBoot does not support PCR 22, we will not support it here.
    @Override
    public Set<Rule> loadTrustRulesForLocation(String location, TblHosts host) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Rule> loadTrustRulesForAssetTag(MwAssetTagCertificate atagCert, TblHosts host) {
        return reader.loadPcrMatchesConstantRulesForAssetTag(atagCert, host);
    }

}
