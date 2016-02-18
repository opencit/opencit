/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.rule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.mtwilson.model.VmMeasurement;
import com.intel.mtwilson.policy.BaseRule;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.RuleResult;
import com.intel.mtwilson.policy.fault.VMMeasurementLogValueMismatchEntries;
import com.intel.mtwilson.policy.fault.VmMeasurementLogContainsUnexpectedEntries;
import com.intel.mtwilson.policy.fault.VmMeasurementLogMissingExpectedEntries;
import java.util.ArrayList;
import java.util.HashSet;
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

    public VmMeasurementLogEquals()
    { 
        this.setMarkers("VM");
    } // for desearializing jackson
    
    /**
     * This function would verify the measured modules against the whitelist modules and appropriately raises any required
     * faults in case of differences.
     * @param actualModules
     * @param whitelistModules
     * @return 
     */
    public RuleResult apply2(List<VmMeasurement> actualModules, List<VmMeasurement> whitelistModules) {
        log.debug("VmMeasurementLogEquals: About to apply the VmMeasurementLogEquals policy");
        RuleResult report = new RuleResult(this);

        ArrayList<VmMeasurement> vmActualUnexpected = new ArrayList<>(actualModules);
        vmActualUnexpected.removeAll(whitelistModules); 

        ArrayList<VmMeasurement> vmActualMissing = new ArrayList<>(whitelistModules);

        log.debug("VmMeasurementLogEquals: About to check VM entries {} against the whitelist which has {} entries.", 
                actualModules.size(), vmActualMissing.size());
        log.debug("VmMeasurementLogEquals: Verifying {} against {}", whitelistModules.toString(), actualModules.toString());

        vmActualMissing.removeAll(actualModules);

        raiseFaultForModifiedEntries(vmActualUnexpected, vmActualMissing, report);        
                
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
    
    /**
     * This function raises the faults for the modules that were updated. The faults for missing and new modules would be raised
     * by the calling function.
     * @param vmActualUnexpected
     * @param vmActualMissing
     * @param report 
     */
    private void raiseFaultForModifiedEntries(ArrayList<VmMeasurement> vmActualUnexpected, ArrayList<VmMeasurement> vmActualMissing, RuleResult report) {
        ArrayList<VmMeasurement> vmModifiedModules = new ArrayList<>();
        ArrayList<VmMeasurement> tempVMActualUnexpected = new ArrayList<>(vmActualUnexpected);
        ArrayList<VmMeasurement> tempVMActualMissing = new ArrayList<>(vmActualMissing);
        
        try {
            for (VmMeasurement tempUnexpected : tempVMActualUnexpected) {
                for (VmMeasurement tempMissing : tempVMActualMissing) {
                    log.debug("RaiseFaultForModifiedEntries: Comparing module {} with hash {} to module {} with hash {}.", tempUnexpected.getLabel(), 
                            tempUnexpected.getValue().toString(), tempMissing.getLabel(), tempMissing.getValue().toString());
                    if (tempUnexpected.getLabel().equalsIgnoreCase(tempMissing.getLabel())) {
                        log.debug("Adding the entry to the list of modified modules and deleting from the other 2 lists.");
                        vmModifiedModules.add(tempUnexpected);
                        vmActualMissing.remove(tempMissing);
                        vmActualUnexpected.remove(tempUnexpected);
                    }
                }
            }                        
                       
            if (!vmModifiedModules.isEmpty()) {
                log.debug("RaiseFaultForModifiedEntries : Host has updated #{} modules compared to the white list.", vmModifiedModules.size());
                report.fault(new VMMeasurementLogValueMismatchEntries(new HashSet<>(vmModifiedModules)));                
            } else {
                log.debug("RaiseFaultForModifiedEntries: No updated modules found.");
            }
            
        } catch (Exception ex) {
            log.error("RaiseFaultForModifiedEntries: Error during verification of changed modules.", ex);
            log.error(ex.getMessage());
        }
    }    

    @Override
    public RuleResult apply(HostReport hostReport) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
