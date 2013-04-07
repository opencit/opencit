/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy;

import com.intel.mtwilson.policy.fault.Cite;
import com.intel.mtwilson.policy.fault.RequireAnyEmptySet;
import java.util.ArrayList;
import java.util.List;

/**
 * A policy container that requires a host to pass any one of the contained policies.
 * 
 * Generally it's only useful to apply this policy at the highest level
 * (contains all the policy sets that are trusted for the host).
 * 
 * If the list of optional policies is EMPTY the report will indicate NOT trusted -- 
 * it is an error to say "Require any of an empty list" because doing so creates a
 * condition in which a host could be "trusted" without meeting any policy at all.
 * So in this case the report will contain exactly one fault of the type RequireAnyEmptySet.
 * 
 * If the host fails ALL the optional policies, the report will indicate NOT trusted 
 * (as expected) and will contain one fault for each failed policy.
 * 
 * If the host fails SOME of the optional policies, the report will indicate TRUSTED,
 * getFaults() will return an empty list, and a new function getOptionalFaults() will
 * return the list of faults from the optional policies.
 * 
 * 
 * @author jbuhacoff
 */
public class RequireAny implements TrustPolicy {
    private final List<TrustPolicy> optional;
    public RequireAny(List<TrustPolicy> optional) {
        this.optional = optional;
    }
    
    /**
     * The reason we wrap each of the policies in a Check class is that when this is
     * serialized, the Check class will indicate the name of the policy.
     * 
     * For example, if you were to serialize an instance of PcrMatchesConstant, you would
     * get:
      "expectedPcr" : {
        "value" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
        "index" : "1"
      }
     * But if you were to serialize the same instance wrapped with a Check, you would get:
      "policy" : {
        "expectedPcr" : {
          "value" : "aabbccddeeaabbccddeeaabbccddeeaabbccddee",
          "index" : "1"
        }
      },
      "policyName" : "com.intel.mtwilson.policy.PcrMatchesConstant"
     * 
     * @return 
     */
    public List<Check> getChecks() { 
        ArrayList<Check> list = new ArrayList<Check>();
        for(TrustPolicy policy : optional) {
            list.add(new Check(policy));
        }
        return list; 
    }
    
    @Override
    public TrustReport apply(HostReport hostReport) {
        final TrustReport report = new TrustReport(this);
        for(TrustPolicy policy : optional) {
            TrustReport subreport = policy.apply(hostReport);
            if( !subreport.isTrusted() ) {
                report.fault(new Cite(subreport));
            }
        }
        if( optional.isEmpty() ) {
            report.fault(new RequireAnyEmptySet());
            return report;
        }
        else if( report.getFaults().size() == optional.size() ) {
            return report;
        }
        else {
            // create a new report with no faults so isValid() returns true, and provide the list of optional policies that failed
            TrustReport reportFailedSome = new TrustReport(this) {
                public List<Fault> getOptionalFaults() { return report.getFaults(); }
            };
            return reportFailedSome;
        }
    }
    
    @Override
    public String toString() {
        return String.format("Require any of %d policies", optional.size());
    }
}
