/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Note: passing policy name instead of policy itself because do not want to just "return policy", 
 * since that will have all the same information that you already get in the reports, and anything
 * that serializes this class would then show redundant info.
 * 
 * @author jbuhacoff
 */
public class TrustReport {
    private String policyName;
    private ArrayList<RuleResult> results = new ArrayList<RuleResult>();
    
    public TrustReport(String policyName) {
        this.policyName = policyName;
    }
    
    public String getPolicyName() { return policyName; }
    
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
        ArrayList<RuleResult> markerReports = new ArrayList<RuleResult>();
        for(RuleResult report : results) {
            String[] markers = report.getRule().getMarkers();
            List<String> markerList = Arrays.asList(markers);
            if( markerList.contains(marker) ) {
                markerReports.add(report);
            }
        }
        return markerReports;
    }
    
    public boolean isTrustedForMarker(String marker) {
        return isTrustedForResults(getResultsForMarker(marker));
    }
}
