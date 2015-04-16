/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.fault;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.mtwilson.policy.Fault;
import com.intel.mtwilson.policy.RuleResult;

/**
 * This fault indicates that some other policy was evaluated and the result was not
 * what was expected. The relevant report is provided -- reports include both the policy
 * and the outcome.
 * 
 * In comparison to the Model interface, the Cite class provides functionality similar
 * to the getMore() method of the Fault class in the Model class hierarchy, because it
 * allows a policy to evaluate sub-policies and then if they produce negative results
 * to add those results to its own report via the Cite.
 * 
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class Cite extends Fault {
    private RuleResult report;
    
    public Cite() { } // for desearializing jackson
    
    public Cite(RuleResult report) {
//        super(report.getPolicyName()); 
        super("%s: %s with %d faults", report.getRuleName(), report.isTrusted() ? "trusted" : "not trusted", report.getFaults().size());
        this.report = report;
    }
    public RuleResult getReport() { return report; }
}
