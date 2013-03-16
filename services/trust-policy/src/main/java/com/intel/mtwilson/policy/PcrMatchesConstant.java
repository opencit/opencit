/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy;

import com.intel.mtwilson.model.Pcr;
import com.intel.mtwilson.policy.fault.PcrManifestMissing;
import com.intel.mtwilson.policy.fault.PcrValueMismatch;
import com.intel.mtwilson.policy.fault.PcrValueMissing;

/**
 * The PcrMatchesConstant policy enforces that a specific PCR contains a specific 
 * pre-determined constant value. This is typical for values that are known in 
 * advance such as BIOS or trusted module measurements.
 * 
 * @author jbuhacoff
 */
public class PcrMatchesConstant implements TrustPolicy {
    private Pcr expected;
    public PcrMatchesConstant(Pcr expected) {
        this.expected = expected;
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
                if( !expected.equals(actual) ) {
                    report.fault(new PcrValueMismatch(expected.getIndex(), expected.getValue(), actual.getValue()) );
                }
            }
        }
        return report;
    }
    
}
