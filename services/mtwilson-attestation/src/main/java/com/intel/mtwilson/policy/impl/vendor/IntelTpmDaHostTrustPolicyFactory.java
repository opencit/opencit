/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.policy.impl.vendor;

import com.intel.dcsg.cpg.crypto.DigestAlgorithm;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.model.Bios;
import com.intel.mtwilson.model.Vmm;
import com.intel.mtwilson.policy.Rule;
import com.intel.mtwilson.policy.impl.JpaPolicyReader;
import com.intel.mtwilson.policy.impl.TrustMarker;
import com.intel.mtwilson.policy.rule.AikCertificateTrusted;
import com.intel.mtwilson.policy.rule.PcrMatchesConstant;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author dczech
 */
public class IntelTpmDaHostTrustPolicyFactory extends IntelHostTrustPolicyFactory {
    
    public IntelTpmDaHostTrustPolicyFactory(JpaPolicyReader util) {
        super(util);
    }
    
    @Override
    public Set<Rule> loadTrustRulesForBios(Bios bios, TblHosts host) {
        if (cacerts == null) {
            cacerts = loadTrustedAikCertificateAuthorities();
        }
        HashSet<Rule> rules = new HashSet<>();
        AikCertificateTrusted aikcert = new AikCertificateTrusted(cacerts);
        aikcert.setMarkers(TrustMarker.BIOS.name());
        rules.add(aikcert);
        // first add all the constant rules. EventLog dynamic PCRs will be blank in the whitelist db, and won't be added to the Set
        Set<Rule> pcrConstantRules = reader.loadPcrMatchesConstantRulesForBios(bios, host);
        
        for(Iterator<Rule> it = pcrConstantRules.iterator(); it.hasNext();) {
            PcrMatchesConstant r = (PcrMatchesConstant)it.next();
            if(r.getExpectedPcr().getPcrBank() != DigestAlgorithm.valueOf(host.getPcrBank())) {
                it.remove();
            }            
        }
        
        rules.addAll(pcrConstantRules);
        
        if(host.getBiosMleId().getRequiredManifestList().contains("17")) {
            // 17 is a host specific PCR TPM DA Mode
            Set<Rule> pcrEventRules = reader.loadPcrEventLogIncludesRuleForBiosDaMode(bios, host);
            rules.addAll(pcrEventRules);
        }
        return rules;
    }
    
    @Override
    public Set<Rule> loadComparisonRulesForVmm(Vmm vmm, TblHosts host) {
        HashSet<Rule> rules = new HashSet<>();
        
        Set<Rule> pcrConstantRules = reader.loadPcrMatchesConstantRulesForVmm(vmm, host);
        
        for (Iterator<Rule> it = pcrConstantRules.iterator(); it.hasNext();) {
            PcrMatchesConstant r = (PcrMatchesConstant) it.next();
            if (r.getExpectedPcr().getPcrBank() != DigestAlgorithm.valueOf(host.getPcrBank())) {
                it.remove();
            }
        }
        
        rules.addAll(pcrConstantRules);
        
        if(host.getVmmMleId().getRequiredManifestList().contains("17")) {
            Set<Rule> pcrEventLogRules = reader.loadPcrEventLogIncludesRuleForVmmDaMode(vmm,host);
            rules.addAll(pcrEventLogRules);
        }
        
        return rules;
    }
    
    @Override
    public Set<Rule> loadTrustRulesForVmm(Vmm vmm, TblHosts host) {
        return loadComparisonRulesForVmm(vmm, host);
    }
}
