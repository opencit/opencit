/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy;

import com.intel.mtwilson.model.Pcr;
import com.intel.mtwilson.policy.fault.Cite;
import com.intel.mtwilson.policy.fault.PcrManifestMissing;
import com.intel.mtwilson.policy.fault.PcrValueMismatch;
import com.intel.mtwilson.policy.fault.PcrValueMissing;
import com.intel.mtwilson.policy.fault.RequireAllEmptySet;
import java.util.ArrayList;
import java.util.List;

/**
 * A policy container that requires a host to pass all contained policies.
 * 
 * Generally it's only useful to apply this policy at the highest level
 * (contains all the policies that apply to the host) or as an item of
 * a RequiresAny policy in order to group a set of policies that together
 * forms one option.
 * 
 * @author jbuhacoff
 */
public class RequireAll implements TrustPolicy {
    private final List<TrustPolicy> required;
    public RequireAll(List<TrustPolicy> required) {
        this.required = required;
    }
    
    public List<Check> getChecks() { 
        ArrayList<Check> list = new ArrayList<Check>();
        for(TrustPolicy policy : required) {
            list.add(new Check(policy));
        }
        return list; 
    }
    
    @Override
    public TrustReport apply(HostReport hostReport) {
        TrustReport report = new TrustReport(this);
        for(TrustPolicy policy : required) {
            TrustReport subreport = policy.apply(hostReport);
            if( subreport.isTrusted() ) {
                report.ok(subreport);
            }
            else { // if( !subreport.isTrusted() ) {
                report.fault(new Cite(subreport));
            }
        }
        if( required.isEmpty() ) {
            report.fault(new RequireAllEmptySet());
        }
        return report;
    }
    
    @Override
    public String toString() {
        return String.format("Require all of %d policies", required.size());
    }
}
