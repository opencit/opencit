/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.fault;

import com.intel.mtwilson.policy.Fault;
import com.intel.mtwilson.policy.TrustReport;

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
public class Cite extends Fault {
    private TrustReport report;
    public Cite(TrustReport report) {
//        super(report.getPolicyName()); // XXX TODO maybe use a format like "%s: %s with %d faults", report.getPolicyName(), report.isTrusted() ? "trusted" : "not trusted", report.getFaults().size()
        super("%s: %s with %d faults", report.getPolicyName(), report.isTrusted() ? "trusted" : "not trusted", report.getFaults().size());
        this.report = report;
    }
    public TrustReport getReport() { return report; }
}
