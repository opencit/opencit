/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.rule;

import com.intel.mtwilson.policy.BaseRule;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.RuleResult;
import com.intel.mtwilson.policy.fault.TpmQuoteMissing;

/**
 * A TrustPolicy implementation that checks whether the HostReport contains
 * a TpmQuote, and if it does that the TpmQuote signature is valid and that
 * its contents match what is reported in the PcrManifest
 * 
 * XXX maybe rename to ValidTpmQuotePolicy
 * 
 * @author jbuhacoff
 */
public class TpmQuoteRequired extends BaseRule {

    @Override
    public RuleResult apply(HostReport hostReport) {
        RuleResult report = new RuleResult(this);
//        report.check(this);
//        report.check(getClass().getSimpleName()); // the minimum... show that the host was evaluated by this policy
        if( hostReport.tpmQuote == null ) {
            report.fault(new TpmQuoteMissing());
        }
        else {
            // XXX TODO need to check the validity of the tpm quote signature ... does that mean TpmQuoteRequired implies AikRequired ?  maybe there doesn't need to be a separate AikRequired policy ??
            // XXX TODO need to check that the tpm quote matches the provided pcr manifest  (build a PcrManifest from the quote information and use equals() to compare to the hostReport's PcrManifest)
        }
        return report;
    }
    
}
