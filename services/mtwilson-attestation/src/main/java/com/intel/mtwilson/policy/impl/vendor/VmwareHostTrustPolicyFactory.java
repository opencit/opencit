/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.impl.vendor;

import com.intel.dcsg.cpg.crypto.DigestAlgorithm;
import com.intel.mtwilson.as.data.MwAssetTagCertificate;
import com.intel.mtwilson.policy.impl.JpaPolicyReader;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.model.Bios;
import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.Pcr;
import com.intel.mtwilson.model.PcrEventLog;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.model.Vmm;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.Rule;
import com.intel.mtwilson.policy.impl.TrustMarker;
import com.intel.mtwilson.policy.impl.VendorHostTrustPolicyFactory;
import com.intel.mtwilson.policy.rule.PcrEventLogEquals;
import com.intel.mtwilson.policy.rule.PcrEventLogIntegrity;
import com.intel.mtwilson.policy.rule.PcrMatchesConstant;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Needs to create a policy to check custom vmware flags in the report to indicate if the pcr and event measurements are valid
 * @author jbuhacoff
 */
public class VmwareHostTrustPolicyFactory implements VendorHostTrustPolicyFactory {
    private Logger log = LoggerFactory.getLogger(getClass());
    private JpaPolicyReader reader;
    public VmwareHostTrustPolicyFactory(JpaPolicyReader util) {
        this.reader = util;
    }

    /**
     * HOW TO USE THIS METHOD:  You call generateTrustRulesForHost(HostReport) and get a set of Rules back.
     * Then, based on what checkboxes the customer selected in the UI, you use only the applicable rules.
     * 
     * This function is not being used in MW 1.2 Release. Currently the JPAPolicyReader already includes all the modules ( PCR, Module Manifest and Host Specific ones).
     * 
     * VMWARE RULES:
     * BIOS:  PCR 0
     * VMM: PCR 17, 18, 19, 20
     * PCRS 0, 17, 18, 20 are constant across reboots and hosts with the same software
     * PCR 19 is constant across reboots, has many modules that are constant across hosts with the same software, but has 1 host-specific module
     * @param host
     * @param hostReport
     * @return 
     */
//    @Override
    public Set<Rule> createHostSpecificRules(HostReport hostReport) {
        // constant pcrs, modules that are consistent across reboots, and 
        // even modules that change across reboots *IF* there is a way to calculate
        // expected value, like if there are variables that go into it that we can
        // query from the host
        HashSet<Rule> rules = new HashSet<Rule>();
        // first do all the pcrs that are constants (across hosts with same software and across reboots)
        int[] constantPcrs = new int[] { 0, 17, 18, 20 };
        for(int i=0; i<constantPcrs.length; i++) {
            PcrIndex pcrIndex = PcrIndex.valueOf(constantPcrs[i]);
            Pcr pcr = hostReport.pcrManifest.getPcr(pcrIndex);
            PcrMatchesConstant rulePcr = new PcrMatchesConstant(pcr);
            if( constantPcrs[i] == 0 ) { rulePcr.setMarkers(TrustMarker.BIOS.name()); } // pcr 0 measures the bios for esxi
            else { rulePcr.setMarkers(TrustMarker.VMM.name()); } // pcrs 17,18,20 measure the vmm for esxi
            rules.add(rulePcr);
        }
        // second do all the modules in pcr 19;  if they were all constant across hosts with same VMM, we could just do new  PcrEventLogEquals(eventLog) ... but there is ONE module that is host-specific so we need to treat it differently
        PcrEventLog eventLog = hostReport.pcrManifest.getPcrEventLog(PcrIndex.PCR19);
        /*
        List<Measurement> ms = eventLog.getEventLog();
        ArrayList<Measurement> constants = new ArrayList<Measurement>();
        for(Measurement m : ms) {
//             one of the modules is host-specific so we need to check ?? no, because in THIS METHOD we are doing ALL HOST SPECIFIC ... so just record all of them the same way since they are consistent across reboots. 
            if( m.getInfo().containsKey("EventType") && "HostTpmCommandEvent".equals(m.getInfo().get("EventType"))) {
                // HOST SPECIFIC RULE ... actually ths is only important when WRITING TO DATABASE and READING FROM DATABASE !!! 
            }
        }*/
        PcrEventLogEquals ruleEventLog = new PcrEventLogEquals(eventLog);
        ruleEventLog.setMarkers(TrustMarker.VMM.name()); // pcr 19 measures vmm for esxi
        rules.add(ruleEventLog);
        // now add an integrity check for pcr 19, which is important in order to validate the event log used in previous rule
        PcrEventLogIntegrity ruleEventLogIntegrity = new PcrEventLogIntegrity(DigestAlgorithm.SHA1, PcrIndex.PCR19);
        ruleEventLogIntegrity.setMarkers(TrustMarker.VMM.name()); // this event log integrity rule applies to pcr 19 which is for the vmm
        rules.add(ruleEventLogIntegrity);
        return rules;
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
        // next block removed because the vmware pcr 19 in the database has no value, so the PcrMatchesConstant rule cannot even be created for it.... so there's nothing to find and remove, since it won't be in the list
        /*
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
        */ 
        rules.addAll(pcrConstantRules);
        // third, if the list included pcr 19, at this point the pcr matches constant was rmoved and
        // we will replace it with module/eventlog checks. 
        if( host.getVmmMleId().getRequiredManifestList().contains("19") ) {
            //Set<Rule> pcrEventLogRules = reader.loadPcrEventLogIncludesRuleForVmm(vmm, host  /*  NEW FLAG to exclude dynamic and host specific modules , default false here  and true in the new function getComparisonRulesForVmm */);
            Set<Rule> pcrEventLogRules = reader.loadPcrEventLogEqualExcludingVmm(vmm, host, false  /*  NEW FLAG to exclude dynamic and host specific modules , default false here  and true in the new function getComparisonRulesForVmm */);            
            //rules.addAll(pcrEventLogRules);
            rules.addAll(pcrEventLogRules);
        }
        return rules;
    }
    @Override
    public Set<Rule> loadTrustRulesForLocation(String location, TblHosts host) {
        return reader.loadPcrMatchesConstantRulesForLocation(location, host);
    }

    @Override
    public Set<Rule> loadComparisonRulesForVmm(Vmm vmm, TblHosts host) {
        HashSet<Rule> rules = new HashSet<Rule>();
        // first, load the list of pcr's marked for this host's vmm mle 
        Set<Rule> pcrConstantRules = reader.loadPcrMatchesConstantRulesForVmm(vmm, host);
        rules.addAll(pcrConstantRules);
        if( host.getVmmMleId().getRequiredManifestList().contains("19") ) {
            Set<Rule> pcrEventLogRules = reader.loadPcrEventLogEqualExcludingVmm(vmm, host, true);
            rules.addAll(pcrEventLogRules);
        }
        return rules;
    }
    @Override
    public Set<Rule> loadTrustRulesForAssetTag(MwAssetTagCertificate atagCert, TblHosts host) {
        return reader.loadPcrMatchesConstantRulesForAssetTag(atagCert, host);
    }
    
}
