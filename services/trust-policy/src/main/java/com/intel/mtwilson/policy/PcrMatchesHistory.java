/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy;

import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.Pcr;
import com.intel.mtwilson.model.Sha1Digest;
import com.intel.mtwilson.policy.fault.PcrHistoryInvalid;
import com.intel.mtwilson.policy.fault.PcrManifestMissing;
import com.intel.mtwilson.policy.fault.PcrModuleManifestContainsUnexpectedEntries;
import com.intel.mtwilson.policy.fault.PcrModuleManifestMissingExpectedEntries;
import com.intel.mtwilson.policy.fault.PcrValueMismatch;
import com.intel.mtwilson.policy.fault.PcrValueMissing;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The PcrMatchesConstant policy enforces that a specific PCR contains a specific 
 * pre-determined constant value. This is typical for values that are known in 
 * advance such as BIOS or trusted module measurements.
 * 
 * In addition, this policy enforces that the value was calculated through a
 * specific sequence of extensions. This is a mechanism that can be used to
 * validate that the module manifest for a PCR is trusted.
 * 
 * @author jbuhacoff
 */
public class PcrMatchesHistory implements TrustPolicy {
    private Pcr expected;
    private List<Measurement> history;
    public PcrMatchesHistory(Pcr expected, List<Measurement> history) {
        this.expected = expected;
        this.history = history;
    }
    
    @Override
    public TrustReport apply(HostReport hostReport) {
        TrustReport report = new TrustReport();
        if( hostReport.pcrManifest == null ) {
            report.fault(new PcrManifestMissing());            
        }
        else {
            Pcr actual = hostReport.pcrManifest.getPcr(expected.getIndex().toInteger());
            if( actual == null ) {
                report.fault(new PcrValueMissing(expected.getIndex()));
            }
            else {
                Sha1Digest expectedValue = computeHistory(); // calculate expected' based on history
                // make sure them expected history matches up with the expected value  (a sort of internal integrity check for mistakes in the whitelist)
                if( !expected.getValue().equals(expectedValue)) {
                    report.fault(new PcrHistoryInvalid(expected));
                }
                // make sure the expected pcr value matches the actual pcr value
                if( !expected.equals(actual) ) {
                    report.fault(new PcrValueMismatch(expected.getIndex(), expected.getValue(), actual.getValue()) );
                }
                // compare the module manifest to the pcr history... flag any modules that are not included in the verified history, and any that are missing
                Set<Measurement> hostActualUnexpected = hostReport.pcrModuleManifest.get(expected.getIndex());
                hostActualUnexpected.removeAll(history); //  hostActualUnexpected = actual modules - expected modules = only extra modules that shouldn't be there
                if( !hostActualUnexpected.isEmpty() ) {
                    report.fault(new PcrModuleManifestContainsUnexpectedEntries(expected.getIndex(), hostActualUnexpected));
                }
                Set<Measurement> hostActualMissing = new HashSet<Measurement>(history);
                hostActualMissing.removeAll(hostReport.pcrModuleManifest.get(expected.getIndex()));  // hostActualMissing = expected modules - actual modules = only modules that should be there but aren't 
                if( !hostActualMissing.isEmpty() ) {
                    report.fault(new PcrModuleManifestMissingExpectedEntries(expected.getIndex(), hostActualMissing));
                }   
            }
        }
        return report;
    }
    
    private Sha1Digest computeHistory() {
        Iterator<Measurement> it = history.iterator();
        Sha1Digest result = it.next().getValue(); // first measurement
        while(it.hasNext()) {
            result = result.extend(it.next().getValue().toByteArray());
        }
        return result;
    }
}
