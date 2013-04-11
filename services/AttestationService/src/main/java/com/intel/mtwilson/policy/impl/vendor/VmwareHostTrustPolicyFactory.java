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
import com.intel.mtwilson.policy.rule.PcrMatchesConstant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Needs to create a policy to check custom vmware flags in the report to indicate if the pcr and event measurements are valid
 * @author jbuhacoff
 */
public class VmwareHostTrustPolicyFactory implements VendorHostTrustPolicyFactory {
    private JpaPolicyReader util;
    public VmwareHostTrustPolicyFactory(JpaPolicyReader util) {
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

    @Override
    public Set<Rule> loadTrustRulesForVmm(Vmm vmm, TblHosts host) {
        HashSet<Rule> rules = new HashSet<Rule>();
        // first, load the list of pcr's marked for this host's vmm mle 
        Set<Rule> pcrConstantRules = util.loadPcrMatchesConstantRulesForVmm(vmm, host);
        // second, if that list includes pcr 19, then we remove it -- later we will add module rules instead
        boolean checkPcr19 = false;
        for(Rule rule : pcrConstantRules) {
            if( rule instanceof PcrMatchesConstant ) {
                PcrMatchesConstant pcrRule = (PcrMatchesConstant)rule;
                if( pcrRule.getExpectedPcr().getIndex().toInteger() == 19 ) {
                    pcrConstantRules.remove(rule);
                    checkPcr19 = true;
                }
            }
        }
        rules.addAll(pcrConstantRules);
        // third, if the list included pcr 19, at this point the pcr matches constant was rmoved and
        // we will replace it with module/eventlog checks. 
        if( checkPcr19 ) {
            Set<Rule> pcrEventLogRules = util.loadPcrEventLogIncludesRuleForVmm(vmm, host);
            rules.addAll(pcrEventLogRules);
        }
        return rules;
    }

    @Override
    public Set<Rule> loadTrustRulesForLocation(String location, TblHosts host) {
        return util.loadPcrMatchesConstantRulesForLocation(location, host);
    }
    
}
