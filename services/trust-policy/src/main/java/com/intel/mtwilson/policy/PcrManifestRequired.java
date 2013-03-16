/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy;

import com.intel.mtwilson.model.Pcr;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.model.PcrManifest;
import com.intel.mtwilson.policy.fault.PcrManifestMissing;
import com.intel.mtwilson.policy.fault.PcrValueMismatch;

/**
 * A TrustPolicy implementation that checks whether the HostReport contains
 * PCR values that match a given expected PcrManifest. The expected PcrManifest
 * need only include those PCRs whose values are relevant - only those PCRs
 * present in the PcrManifest will be checked in the HostReport. Other PCR
 * values are not considered.
 * 
 * XXX maybe rename to PcrManifestPolicy
 * 
 * @author jbuhacoff
 */
public class PcrManifestRequired implements TrustPolicy {
    private PcrManifest expected;
    public PcrManifestRequired(PcrManifest expected) {
        this.expected = expected;
    }
    
    @Override
    public TrustReport apply(HostReport hostReport) {
        TrustReport report = new TrustReport();
        if( hostReport.pcrManifest == null ) {
            report.fault(new PcrManifestMissing());
        }
        else {
            // we check that for every PCR defined in the policy, the HostReport's PcrManifest contains a PCR with the same index & value
            for(int i=PcrIndex.MIN_VALUE; i<=PcrIndex.MAX_VALUE; i++) {
                if( expected.contains(i) ) {
                    Pcr expectedPcr = expected.getPcr(i);
                    Pcr actualPcr = hostReport.pcrManifest.getPcr(i);
                    if( !expectedPcr.equals(actualPcr) ) {
                        report.fault(new PcrValueMismatch(new PcrIndex(i), expectedPcr.getValue(), actualPcr.getValue()));
                    }
                }
            }
        }
        return report;
    }
    
}
