/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class VmTrustReport {
    private VmReport vmReport;
    private ArrayList<RuleResult> results = new ArrayList<>();
    private Logger log = LoggerFactory.getLogger(getClass());
    
    public VmTrustReport() { } // for desearializing jackson
    
    public VmTrustReport(VmReport vmReport) {
        this.vmReport = vmReport;
    }
    
    public VmReport getVmReport() { return vmReport; }
    
    public void addResult(RuleResult result) {
        results.add(result);
    }
    
    public List<RuleResult> getResults() { return results; } // contains the set of rules and their parameters AND faults AND isTrusted for each one
 
    private boolean isTrustedForResults(List<RuleResult> list) {
        if( list.isEmpty() ) {
            return false; // empty policy is not trusted;  like RequireAllEmptySet fault.
        }
        boolean trusted = true;
        Iterator<RuleResult> it = list.iterator();
        while(it.hasNext()) {
            RuleResult result = it.next();
            trusted = trusted && result.isTrusted();
        }
        return trusted;        
    }
    
    public boolean isTrusted() {
        return isTrustedForResults(results);
    }
    
    // returns a list of trust reports corresponding to the specified marker
    // they are already included in the overall "getReports" but this allows
    // you to look specifically at what caused a specific marker to be trusted
    // or untrusted
    public List<RuleResult> getResultsForMarker(String marker) {
        ArrayList<RuleResult> markerReports = new ArrayList<>();
        for(RuleResult report : results) {
            String[] markers = report.getRule().getMarkers();
            if( markers != null ) {
                List<String> markerList = Arrays.asList(markers);
                if( markerList.contains(marker) ) {
                    markerReports.add(report);
                }
            }
        }
        return markerReports;
    }
    
    public boolean isTrustedForMarker(String marker) {
        return isTrustedForResults(getResultsForMarker(marker));
    }
}
