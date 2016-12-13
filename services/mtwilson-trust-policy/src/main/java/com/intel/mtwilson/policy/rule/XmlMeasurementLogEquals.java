/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.rule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.MeasurementSha1;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.model.XmlMeasurementLog;
import com.intel.mtwilson.policy.BaseRule;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.RuleResult;
import com.intel.mtwilson.policy.fault.XmlMeasurementLogContainsUnexpectedEntries;
import com.intel.mtwilson.policy.fault.XmlMeasurementLogMissing;
import com.intel.mtwilson.policy.fault.XmlMeasurementLogMissingExpectedEntries;
import com.intel.mtwilson.policy.fault.XmlMeasurementLogValueMismatchEntries;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The functionality of this policy is to verify the whitelist measurement log against what is provided by the host during host attestation.
 * Need to ensure that there are no additional modules or any modules missing. Also the digest value of all the modules are matching.
 * 
 * Sample format of the log would like:
 * <Measurements xmlns="mtwilson:trustdirector:measurements:1.1" DigestAlg="sha1">
 *     <Dir Path="/boot">1a39a3ee5e6b4b0d3255bfef95601890afd80709</Dir>
 *     <File Path="/boot/grub/stage1">2a39a3ee5e6b4b0d3255bfef95601890afd80709</File>
 *     <File Path="/boot/grub/e2fs_stage1_5">3a39a3ee5e6b4b0d3255bfef95601890afd80709</File>
 *     <File Path="/boot/grub/stage2">4a39a3ee5e6b4b0d3255bfef95601890afd80709</File>
 *     <File Path="/boot/grub/menu.lst">5a39a3ee5e6b4b0d3255bfef95601890afd80709</File>
 *     <File Path="/boot/initrd.img-3.0.0-12-virtual">6a39a3ee5e6b4b0d3255bfef95601890afd80709</File>
 *     <File Path="/boot/config-3.0.0-12-virtual">7a39a3ee5e6b4b0d3255bfef95601890afd80709</File>
 *     <File Path="/boot/vmlinuz-3.0.0-12-virtual">8a39a3ee5e6b4b0d3255bfef95601890afd80709</File>
 *     <Dir Path="/path/to/directory" Include="^include.regex.here$" Exclude="^exclude.regex.here$">9a39a3ee5e6b4b0d3255bfef95601890afd80709</Dir>
 * </Measurements>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class XmlMeasurementLogEquals extends BaseRule {
    private Logger log = LoggerFactory.getLogger(getClass());
    private XmlMeasurementLog expected;
    private PcrIndex pcrIndex; 

    protected XmlMeasurementLogEquals() {
        this.expected = new XmlMeasurementLog(PcrIndex.PCR19);
    } // for desearializing jackson
    
    public XmlMeasurementLogEquals(XmlMeasurementLog expected) {
        this.expected = expected;
        this.pcrIndex = expected.getPcrIndex();
    }
    
    public PcrIndex getPcrIndex() {
        return this.pcrIndex;
    }
 
    public XmlMeasurementLog getXmlMeasurementLog() { return expected; }
    
    @Override
    public RuleResult apply(HostReport hostReport) {
        log.debug("XmlMeasurementLogEquals: About to apply the XmlMeasurementLogEquals policy");
        RuleResult report = new RuleResult(this);
        if( hostReport.pcrManifest.getMeasurementXml() == null || hostReport.pcrManifest.getMeasurementXml().isEmpty()) {
            
            log.debug("XmlMeasurementLogEquals: XmlMeasurementLog missing fault is being raised.");
            report.fault(new XmlMeasurementLogMissing());
            
        } else {
            // Retrieve the list of modules as measurements from the XML log provided by the host
            List<Measurement> actualModules = new XmlMeasurementLog(expected.getPcrIndex(), hostReport.pcrManifest.getMeasurementXml()).getMeasurements();
            log.debug("XmlMeasurementLogEquals: About to apply the XmlMeasurementLogEquals policy for {} entries.", actualModules.size());
            if( actualModules.isEmpty() ) {
                report.fault(new XmlMeasurementLogMissing());
            }
            else {                    
                // We will first check if the host provided any additional modules as part of the log
                // hostActualUnexpected = actual modules - expected modules = only extra modules that shouldn't be there;  
                // comparison is done BY HASH VALUE,  not by name or any "other info"
                ArrayList<Measurement> hostActualUnexpected = new ArrayList<>(actualModules);
                hostActualUnexpected.removeAll(expected.getMeasurements()); 

                ArrayList<Measurement> hostActualMissing = new ArrayList<>(expected.getMeasurements());

                log.debug("XmlMeasurementLogEquals: About to check host entries {} against the whitelist which has {} entries.", 
                        actualModules.size(), hostActualMissing.size());
                log.debug("XmlMeasurementLogEquals: Verifying {} against {}", expected.toString(), actualModules.toString());

                hostActualMissing.removeAll(actualModules); // hostActualMissing = expected modules - actual modules = only modules that should be there but aren't 

                raiseFaultForModifiedEntries(hostActualUnexpected, hostActualMissing, report);
                
                if( !hostActualUnexpected.isEmpty() ) {
                    log.debug("XmlMeasurementLogEquals : Host is having #{} additional modules compared to the white list.", hostActualUnexpected.size());
                    report.fault(new XmlMeasurementLogContainsUnexpectedEntries(expected.getPcrIndex(), hostActualUnexpected));
                } else {
                    log.debug("XmlMeasurementLogEquals: Host is not having any additional modules compared to the white list");
                }
                
                if( !hostActualMissing.isEmpty() ) {
                    log.debug("XmlMeasurementLogEquals : Host is missing #{} modules compared to the white list.", hostActualMissing.size());
                    report.fault(new XmlMeasurementLogMissingExpectedEntries(expected.getPcrIndex(), new HashSet<>(hostActualMissing)));
                } else {                       
                    log.debug("XmlMeasurementLogEquals: Host is not missing any modules compared to the white list");
                }                    
            }
        }
        return report;
    }
    
    private void raiseFaultForModifiedEntries(ArrayList<Measurement> hostActualUnexpected, ArrayList<Measurement> hostActualMissing, RuleResult report) {
        ArrayList<Measurement> hostModifiedModules = new ArrayList<>();
        ArrayList<Measurement> tempHostActualUnexpected = new ArrayList<>(hostActualUnexpected);
        ArrayList<Measurement> tempHostActualMissing = new ArrayList<>(hostActualMissing);
        
        try {
            for (Measurement tempUnexpected : tempHostActualUnexpected) {
                for (Measurement tempMissing : tempHostActualMissing) {
                    log.debug("RaiseFaultForModifiedEntries: Comparing module {} with hash {} to module {} with hash {}.", tempUnexpected.getLabel(), 
                            tempUnexpected.getValue().toString(), tempMissing.getLabel(), tempMissing.getValue().toString());
                    if (tempUnexpected.getLabel().equalsIgnoreCase(tempMissing.getLabel())) {
                        log.debug("Adding the entry to the list of modified modules and deleting from the other 2 lists.");
                        
                        // We are storing the whitelist value and the actual value so that we do not need to compare again when generating the reports.
                        HashMap<String, String> tempHashMapToAdd = new HashMap<>();
                        tempHashMapToAdd.put("Actual_Value", tempUnexpected.getValue().toString());
                        Measurement toMeasurementToAdd = new MeasurementSha1((Sha1Digest)tempMissing.getValue(), tempMissing.getLabel(), tempHashMapToAdd);
                        
                        hostModifiedModules.add(toMeasurementToAdd);
                        hostActualUnexpected.remove(tempUnexpected);
                        hostActualMissing.remove(tempMissing);
                    }
                }
            }                        
            
            if (!hostModifiedModules.isEmpty()) {
                log.debug("XmlMeasurementLogEquals : Host has updated #{} modules compared to the white list.", hostModifiedModules.size());
                report.fault(new XmlMeasurementLogValueMismatchEntries(expected.getPcrIndex(), new HashSet<>(hostModifiedModules)));                
            } else {
                log.debug("RaiseFaultForModifiedEntries: No updated modules found.");
            }
            
        } catch (Exception ex) {
            log.error("RaiseFaultForModifiedEntries: Error during verification of changed modules.", ex);            
        }
    }    
}
