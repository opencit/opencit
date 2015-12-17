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
import java.util.Arrays;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The PcrMatchesConstant policy enforces that a specific PCR contains a specific 
 * pre-determined constant value. This is typical for values that are known in 
 * advance such as BIOS or trusted module measurements.
 * 
 * For example, "PCR {index} must equal {hex-value}"
 * 
 * @author hxia5
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class AssetTagMatches extends BaseRule {
    private Logger log = LoggerFactory.getLogger(getClass());
    private final byte[] expected;
    
    @JsonCreator
    public AssetTagMatches(@JsonProperty("expected_tag") byte[] expected) {
        this.expected = expected;
    }
    
    public byte[] getExpectedTag() { return expected; }
    
    @Override
    public RuleResult apply(HostReport hostReport) {
        RuleResult report = new RuleResult(this);
//        report.check(this);
//        report.check("%s: PCR %s is constant %s", getClass().getSimpleName(),expected.getIndex().toString(), expected.getValue().toString() );
        if( hostReport.assetTagReported == null) {
            log.debug("hostReport.assetTagReported is null");
            report.fault("AssetTag Reported is null");            
        } else if (expected == null) {
            log.debug("Expected Assettag is null");
            report.fault("AssetTag is not in provisionded by the management");            
        }
        else {
            log.debug("assetTagReported is {}, expected is {}", Hex.encodeHexString(hostReport.assetTagReported), Hex.encodeHexString(expected));
            if(!Arrays.equals(expected, hostReport.assetTagReported)) {
               log.debug("assetTagReported: {}, NOT equal to expected: {}", Hex.encodeHexString(hostReport.assetTagReported), Hex.encodeHexString(expected));
               report.fault("Asset tag provisioned does not match asset tag reported");
            }
        }
        return report;
    }
    
    @Override
    public String toString() {
        return String.format("Expected tag is: %s", expected.toString());
    }
}
