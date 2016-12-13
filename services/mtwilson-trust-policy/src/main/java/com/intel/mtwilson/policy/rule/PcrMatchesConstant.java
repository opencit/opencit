/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.rule;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.mtwilson.model.Pcr;
import com.intel.mtwilson.policy.BaseRule;
import com.intel.mtwilson.policy.BaseRule;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.RuleResult;
import com.intel.mtwilson.policy.RuleResult;
import com.intel.mtwilson.policy.fault.PcrManifestMissing;
import com.intel.mtwilson.policy.fault.PcrValueMismatch;
import com.intel.mtwilson.policy.fault.PcrValueMissing;

/**
 * The PcrMatchesConstant policy enforces that a specific PCR contains a specific 
 * pre-determined constant value. This is typical for values that are known in 
 * advance such as BIOS or trusted module measurements.
 * 
 * For example, "PCR {index} must equal {hex-value}"
 * 
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class PcrMatchesConstant extends BaseRule {
    private final Pcr expected;
    
    @JsonCreator
    public PcrMatchesConstant(@JsonProperty("expected_pcr") Pcr expected) {
        this.expected = expected;
    }
    
    public Pcr getExpectedPcr() { return expected; }
    
    @Override
    public RuleResult apply(HostReport hostReport) {
        RuleResult report = new RuleResult(this);
//        report.check(this);
//        report.check("%s: PCR %s is constant %s", getClass().getSimpleName(),expected.getIndex().toString(), expected.getValue().toString() );
        if( hostReport.pcrManifest == null ) {
            report.fault(new PcrManifestMissing());            
        }
        else {
            Pcr actual = hostReport.pcrManifest.getPcr(expected.getPcrBank(), expected.getIndex().toInteger());
            if( actual == null ) {
                report.fault(new PcrValueMissing(expected.getIndex()));
            }
            else {
                if( !expected.equals(actual) ) {
                    report.fault(PcrValueMismatch.newInstance(expected.getPcrBank(), expected.getIndex(), expected.getValue(), actual.getValue()) );
                }
            }
        }
        return report;
    }
    
    @Override
    public String toString() {
        return String.format("PCR %s, %s", expected.getIndex().toString(), expected.getValue().toString());
    }
}
