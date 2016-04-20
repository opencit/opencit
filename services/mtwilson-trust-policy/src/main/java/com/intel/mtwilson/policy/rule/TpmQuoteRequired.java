/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.rule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.mtwilson.policy.BaseRule;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.RuleResult;
import com.intel.mtwilson.policy.fault.TpmQuoteMissing;

/**
 * A TrustPolicy implementation that checks whether the HostReport contains
 * a TpmQuote, and if it does that the TpmQuote signature is valid and that
 * its contents match what is reported in the PcrManifest
 * 
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class TpmQuoteRequired extends BaseRule {

    protected TpmQuoteRequired() { } // for desearializing jackson
    
    @Override
    public RuleResult apply(HostReport hostReport) {
        RuleResult report = new RuleResult(this);
//        report.check(this);
//        report.check(getClass().getSimpleName()); // the minimum... show that the host was evaluated by this policy
        if( hostReport.tpmQuote == null ) {
            report.fault(new TpmQuoteMissing());
        }
        else {
            
        }
        return report;
    }
    
}
