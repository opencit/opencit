/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.rule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.PcrEventLog;
import com.intel.mtwilson.policy.BaseRule;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.RuleResult;
import com.intel.mtwilson.policy.fault.PcrEventLogContainsUnexpectedEntries;
import com.intel.mtwilson.policy.fault.PcrEventLogMissing;
import com.intel.mtwilson.policy.fault.PcrEventLogMissingExpectedEntries;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A TrustPolicy implementation that checks whether the HostReport contains
 * a ModuleManifest for a given PCR that equals the expected ModuleManifest.
 * The expected ModuleManifest in this case is a complete manifest and any
 * change (less modules, more modules, different modules) in the actual
 * ModuleManifest will trigger a fault.
 * 
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class PcrEventLogEquals extends BaseRule {
    private Logger log = LoggerFactory.getLogger(getClass());
    private PcrEventLog expected;

    protected PcrEventLogEquals() { } // for desearializing jackson
    
    public PcrEventLogEquals(PcrEventLog expected) {
        this.expected = expected;
    }
    
    public PcrEventLog getPcrModuleManifest() { return expected; }
    
    @Override
    public RuleResult apply(HostReport hostReport) {
        RuleResult report = new RuleResult(this);
//        report.check(this);        
//        report.check(getClass().getSimpleName()); // the minimum... show that the host was evaluated by this policy
        if( hostReport.pcrManifest == null ) {
            log.debug("PcrManifest null fault is being raised.");
            report.fault(new PcrEventLogMissing());
        }
        else {
            PcrEventLog pcrEventLog = getPcrEventLog(hostReport); 
            if( pcrEventLog == null ) {
                log.debug("PcrEventLog missing fault is being raised.");
                report.fault(new PcrEventLogMissing(expected.getPcrIndex()));
            }
            else {
                List<Measurement> moduleManifest = pcrEventLog.getEventLog();
                log.debug("About to apply the PcrEventLogEquals policy for {} entries.", moduleManifest.size());
                if( moduleManifest == null || moduleManifest.isEmpty() ) {
                    report.fault(new PcrEventLogMissing(expected.getPcrIndex()));
                }
                else {                    
                    // we check that for the PCR defined in the policy, the HostReport's PcrModuleManifest contains the exact set of expected modules
                    ArrayList<Measurement> hostActualUnexpected = new ArrayList<Measurement>(moduleManifest);
                    hostActualUnexpected.removeAll(expected.getEventLog()); //  hostActualUnexpected = actual modules - expected modules = only extra modules that shouldn't be there;  comparison is done BY HASH VALUE,  not by name or any "other info"
                    if( !hostActualUnexpected.isEmpty() ) {
                        log.debug("PcrEventLogEquals : Host is having #{} additional modules compared to the white list.", hostActualUnexpected.size());
                        report.fault(new PcrEventLogContainsUnexpectedEntries(expected.getPcrIndex(), hostActualUnexpected));
                    }
                    ArrayList<Measurement> hostActualMissing = new ArrayList<Measurement>(expected.getEventLog());
                    hostActualMissing.removeAll(moduleManifest); // hostActualMissing = expected modules - actual modules = only modules that should be there but aren't 
                    if( !hostActualMissing.isEmpty() ) {
                        log.debug("PcrEventLogEquals : Host is missing #{} modules compared to the white list.", hostActualMissing.size());
                        report.fault(new PcrEventLogMissingExpectedEntries(expected.getPcrIndex(), new HashSet<Measurement>(hostActualMissing)));
                    }   
                }
            }
        }
        return report;
    }
    
    protected PcrEventLog getPcrEventLog(HostReport hostReport) {
        return hostReport.pcrManifest.getPcrEventLog(expected.getPcrIndex());        
    }
    
}
