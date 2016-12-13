/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.rule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.dcsg.cpg.crypto.AbstractDigest;
import com.intel.dcsg.cpg.crypto.DigestAlgorithm;
import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.mtwilson.model.XmlMeasurementLog;
import com.intel.mtwilson.policy.BaseRule;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.RuleResult;
import com.intel.mtwilson.policy.fault.XmlMeasurementLogMissing;
import com.intel.mtwilson.policy.fault.XmlMeasurementValueMismatch;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This policy verifies the integrity of the measurement log provided by the host. It does
 * this integrity verification by calculating the expected final hash value by extending
 * all the modules measured in the exact same order and comparing it with the static
 * tbootxm module in the whitelist.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class XmlMeasurementLogIntegrity extends BaseRule {
    private Logger log = LoggerFactory.getLogger(getClass());

    private Sha1Digest expectedValue;
    private PcrIndex pcrIndex;
    
    protected XmlMeasurementLogIntegrity() { } // for desearializing jackson
    
    public XmlMeasurementLogIntegrity(Sha1Digest expectedValue, PcrIndex pcrIndex) {
        this.expectedValue = expectedValue;
        this.pcrIndex = pcrIndex;
    }
    
    public AbstractDigest getExpectedValue() { return expectedValue; }

    public PcrIndex getPcrIndex() {
        return pcrIndex;
    }
    

    @Override
    public RuleResult apply(HostReport hostReport) {
        log.debug("XmlMeasurementLogIntegrity: About to apply the XmlMeasurementLogIntegrity policy");
        RuleResult report = new RuleResult(this);
        if( hostReport.pcrManifest.getMeasurementXml() == null || hostReport.pcrManifest.getMeasurementXml().isEmpty()) {
            
            log.debug("XmlMeasurementLogIntegrity: XmlMeasurementLog missing fault is being raised.");
            report.fault(new XmlMeasurementLogMissing());
            
        } else {

            List<Measurement> measurements = new XmlMeasurementLog(this.pcrIndex, hostReport.pcrManifest.getMeasurementXml()).getMeasurements();
            log.debug("XmlMeasurementLogIntegrity: Retrieved #{} of measurements from the log.", measurements.size());
            if( measurements.size() > 0 ) {
                AbstractDigest actualValue = computeHistory(measurements); // calculate expected' based on history
                log.debug("XmlMeasurementLogIntegrity: About to verify the calclated final hash {} with expected hash {}", actualValue.toString(), expectedValue.toString());
                // make sure the expected pcr value matches the actual pcr value
                if( !expectedValue.equals(actualValue) ) {
                    log.info("XmlMeasurementLogIntegrity: Mismatch in the expected final hash value for the XML Measurement log.");
                    report.fault(XmlMeasurementValueMismatch.newInstance(DigestAlgorithm.SHA1, expectedValue, actualValue) );
                } else {
                    log.debug("Verified the integrity of the XML measurement log successfully.");
                }
            }
        }
        return report;
    }
    
    private Sha1Digest computeHistory(List<Measurement> list) {
        // start with a default value of zero...  that should be the initial value of every PCR ..  if a pcr is reset after boot the tpm usually sets its starting value at -1 so the end result is different , which we could then catch here when the hashes don't match        
            Sha1Digest result = Sha1Digest.ZERO;
            for (Measurement m : list) {
                //result = result.extend(m.getValue().toString().getBytes());
                log.debug("XmlMeasurementLogIntegrity-computeHistory: Current value of result is {}", result.toString());
                result = result.extend(Sha1Digest.valueOfHex(m.getValue().toString()));
                log.debug("XmlMeasurementLogIntegrity-computeHistory: Extended value of result is {}", result.toString());
            }
            return result;     
    }
}
