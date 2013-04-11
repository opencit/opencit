/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.rule;

import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.PcrEventLog;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.policy.BaseRule;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.RuleResult;
import com.intel.mtwilson.policy.fault.PcrEventLogContainsUnexpectedEntries;
import com.intel.mtwilson.policy.fault.PcrEventLogMissing;
import com.intel.mtwilson.policy.fault.PcrEventLogMissingExpectedEntries;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A TrustPolicy implementation that checks whether the HostReport contains
 * a ModuleManifest for a given PCR that includes the expected ModuleManifest.
 * The expected ModuleManifest in this case is considered a subset manifest -
 * the host may have the same modules, or additional modules, and pass;  but
 * if the host is missing any modules from the manifest it will trigger a fault.
 * 
 * @author jbuhacoff
 */
public class PcrEventLogIncludes extends BaseRule {
    private PcrIndex pcrIndex;
    private Set<Measurement> expected;
    public PcrEventLogIncludes(PcrIndex pcrIndex, Set<Measurement> expected) {
        this.pcrIndex = pcrIndex;
        this.expected = expected;
    }
    
    public PcrIndex getPcrIndex() { return pcrIndex; }
    public Set<Measurement> getMeasurements() { return expected; }
    
    
    @Override
    public RuleResult apply(HostReport hostReport) {
        RuleResult report = new RuleResult(this);
//        report.check(this);
//        report.check(getClass().getSimpleName()); // the minimum... show that the host was evaluated by this policy
        if( hostReport.pcrManifest == null ) {
            report.fault(new PcrEventLogMissing());
        }
        else {
            List<Measurement> moduleManifest = hostReport.pcrManifest.getPcrEventLog(pcrIndex).getEventLog();
            if( moduleManifest == null ) {
                report.fault(new PcrEventLogMissing(pcrIndex));
            }
            else {
                HashSet<Measurement> hostActualMissing = new HashSet<Measurement>(expected);
                hostActualMissing.removeAll(moduleManifest); // hostActualMissing = expected modules - actual modules = only modules that should be there but aren't 
                if( !hostActualMissing.isEmpty() ) {
                    report.fault(new PcrEventLogMissingExpectedEntries(pcrIndex, hostActualMissing));
                }   
            }
        }
        return report;
    }
    
}
