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

/**
 * Note: passing policy name instead of policy itself because do not want to just "return policy", 
 * since that will have all the same information that you already get in the reports, and anything
 * that serializes this class would then show redundant info.  Every rule result already includes
 * the rule itself, and since there is a result for every rule in the policy,  the entire policy
 * is represented by the set of rule results.
 * 
 * The host report is also included, which is somewhat redundant but not completely, because it
 * has information that may not be present in the rules, which the UI may want to show. For example,
 * for vmware PCR 19 which has a different value for every host due to a host-specific UUID
 * being extended into it,  the UI may want to show the actual value of PCR 19. however, if the
 * rules only check for PCR integrity and for some modules to be included, then it doesn't really
 * say what is the PCR value (and if it did, it would show the expected PCR, not the actual PCR).
 * Also if module names mismatch even though their digests are the same, that wouldn't normally
 * be reflected in the results.
 * 
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class TrustReport {
    private HostReport hostReport;
    private String policyName;
    private ArrayList<RuleResult> results = new ArrayList<RuleResult>();
    private Logger log = LoggerFactory.getLogger(getClass());
    
    public TrustReport() { } // for desearializing jackson
    
    public TrustReport(HostReport hostReport, String policyName) {
        this.hostReport = hostReport;
        this.policyName = policyName;
    }
    
    public HostReport getHostReport() { return hostReport; }
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
