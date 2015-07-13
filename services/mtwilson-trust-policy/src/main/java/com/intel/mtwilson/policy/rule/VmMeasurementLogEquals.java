/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.rule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.policy.BaseRule;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.RuleResult;
import com.intel.mtwilson.policy.fault.VMMeasurementLogValueMismatchEntries;
import com.intel.mtwilson.policy.fault.VmMeasurementLogContainsUnexpectedEntries;
import com.intel.mtwilson.policy.fault.VmMeasurementLogMissingExpectedEntries;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The functionality of this policy is to verify the whitelist measurement log in the trust policy against what is provided by the VM during attestation.
 * Need to ensure that there are no additional modules or any modules missing. Also the digest value of all the modules are matching.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class VmMeasurementLogEquals extends BaseRule {
    private Logger log = LoggerFactory.getLogger(getClass());

    public VmMeasurementLogEquals() { } // for desearializing jackson
    
    public RuleResult apply2(List<Measurement> actualModules, List<Measurement> whitelistModules) {
        log.debug("VmMeasurementLogEquals: About to apply the VmMeasurementLogEquals policy");
        RuleResult report = new RuleResult(this);

        ArrayList<Measurement> vmActualUnexpected = new ArrayList<>(actualModules);
        vmActualUnexpected.removeAll(whitelistModules); 

        ArrayList<Measurement> vmActualMissing = new ArrayList<>(whitelistModules);

        log.debug("VmMeasurementLogEquals: About to check VM entries {} against the whitelist which has {} entries.", 
                actualModules.size(), vmActualMissing.size());
        log.debug("VmMeasurementLogEquals: Verifying {} against {}", whitelistModules.toString(), actualModules.toString());

        vmActualMissing.removeAll(actualModules);

        RaiseFaultForModifiedEntries(vmActualUnexpected, vmActualMissing, report);        
                
        if( !vmActualUnexpected.isEmpty() ) {
            log.debug("VmMeasurementLogEquals : VM is having #{} additional modules compared to the white list.", vmActualUnexpected.size());
            report.fault(new VmMeasurementLogContainsUnexpectedEntries(vmActualUnexpected));
        } else {
            log.debug("VmMeasurementLogEquals: VM is not having any additional modules compared to the white list");
        }

        if( !vmActualMissing.isEmpty() ) {
            log.debug("VmMeasurementLogEquals : Host is missing #{} modules compared to the white list.", vmActualMissing.size());
            report.fault(new VmMeasurementLogMissingExpectedEntries(new HashSet<>(vmActualMissing)));
        } else {                       
            log.debug("VmMeasurementLogEquals: Host is not missing any modules compared to the white list");
        }                    
        return report;
    }
    
    private void RaiseFaultForModifiedEntries(ArrayList<Measurement> vmActualUnexpected, ArrayList<Measurement> vmActualMissing, RuleResult report) {
        ArrayList<Measurement> vmModifiedModules = new ArrayList<>();
        
        try {
            Iterator unexpectedModules = vmActualUnexpected.iterator();
            while (unexpectedModules.hasNext()) {
                Measurement tempUnexpected = (Measurement) unexpectedModules.next();
                Iterator missingModules = vmActualMissing.iterator();
                while (missingModules.hasNext()) {
                    Measurement tempMissing = (Measurement) missingModules.next();
                    log.debug("RaiseFaultForModifiedEntries: Comparing module {} with hash {} to module {} with hash {}.", tempUnexpected.getLabel(), 
                            tempUnexpected.getValue().toString(), tempMissing.getLabel(), tempMissing.getValue().toString());
                    if (tempUnexpected.getLabel().equalsIgnoreCase(tempMissing.getLabel())) {
                        log.debug("Adding the entry to the list of modified modules and deleting from the other 2 lists.");
                        vmModifiedModules.add(tempUnexpected);
                        vmActualUnexpected.remove(tempUnexpected);
                        vmActualMissing.remove(tempMissing);
                    }
                }
            }                        
            
            if (!vmModifiedModules.isEmpty()) {
                log.debug("XmlMeasurementLogEquals : Host has updated #{} modules compared to the white list.", vmModifiedModules.size());
                report.fault(new VMMeasurementLogValueMismatchEntries(new HashSet<>(vmModifiedModules)));                
            }
            
        } catch (Exception ex) {
            
        }
    }    

    @Override
    public RuleResult apply(HostReport hostReport) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
