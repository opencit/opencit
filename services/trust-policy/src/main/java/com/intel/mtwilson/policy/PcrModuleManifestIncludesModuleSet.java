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
 * a ModuleManifest for a given PCR that includes the expected ModuleManifest.
 * The expected ModuleManifest in this case is considered a subset manifest -
 * the host may have the same modules, or additional modules, and pass;  but
 * if the host is missing any modules from the manifest it will trigger a fault.
 * 
 * @author jbuhacoff
 */
public class PcrModuleManifestIncludesModuleSet implements TrustPolicy {
    private PcrModuleManifest expected;
    public PcrModuleManifestIncludesModuleSet(PcrModuleManifest expected) {
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
