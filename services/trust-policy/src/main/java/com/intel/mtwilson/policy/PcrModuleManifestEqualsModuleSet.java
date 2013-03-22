/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy;

import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.PcrModuleManifest;
import com.intel.mtwilson.policy.fault.PcrModuleManifestContainsUnexpectedEntries;
import com.intel.mtwilson.policy.fault.PcrModuleManifestMissing;
import com.intel.mtwilson.policy.fault.PcrModuleManifestMissingExpectedEntries;
import java.util.HashSet;
import java.util.Set;

/**
 * A TrustPolicy implementation that checks whether the HostReport contains
 * a ModuleManifest for a given PCR that equals the expected ModuleManifest.
 * The expected ModuleManifest in this case is a complete manifest and any
 * change (less modules, more modules, different modules) in the actual
 * ModuleManifest will trigger a fault.
 * 
 * @author jbuhacoff
 */
public class PcrModuleManifestEqualsModuleSet implements TrustPolicy {
    private PcrModuleManifest expected;
    public PcrModuleManifestEqualsModuleSet(PcrModuleManifest expected) {
        this.expected = expected;
    }
    
    @Override
    public TrustReport apply(HostReport hostReport) {
        TrustReport report = new TrustReport();
        if( hostReport.pcrModuleManifest == null ) {
            report.fault(new PcrModuleManifestMissing());
        }
        else {
            Set<Measurement> moduleManifest = hostReport.pcrModuleManifest.get(expected.getPcrIndex());
            if( moduleManifest == null ) {
                report.fault(new PcrModuleManifestMissing(expected.getPcrIndex()));
            }
            else {
                // we check that for the PCR defined in the policy, the HostReport's PcrModuleManifest contains the exact set of expected modules
                HashSet<Measurement> hostActualUnexpected = new HashSet<Measurement>(moduleManifest);
                hostActualUnexpected.removeAll(expected.getModuleManifest()); //  hostActualUnexpected = actual modules - expected modules = only extra modules that shouldn't be there
                if( !hostActualUnexpected.isEmpty() ) {
                    report.fault(new PcrModuleManifestContainsUnexpectedEntries(expected.getPcrIndex(), hostActualUnexpected));
                }
                HashSet<Measurement> hostActualMissing = new HashSet<Measurement>(expected.getModuleManifest());
                hostActualMissing.removeAll(moduleManifest); // hostActualMissing = expected modules - actual modules = only modules that should be there but aren't 
                if( !hostActualMissing.isEmpty() ) {
                    report.fault(new PcrModuleManifestMissingExpectedEntries(expected.getPcrIndex(), hostActualMissing));
                }   
            }
        }
        return report;
    }
    
}
