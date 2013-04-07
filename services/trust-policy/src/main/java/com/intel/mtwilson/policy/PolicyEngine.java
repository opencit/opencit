/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XXX TODO this class really isn't needed, other than to have a "something" into which
 * both a host report and any policy may be input, and a result obtained.
 * maybe move the convenience methods applyAll and applyAny
 * into a utility class?      PolicyUtil.applyAll(HostReport hostReport, TrustPolicy... policies) and
 * PolicyUtil.applyAny(HostReport hostReport, TrustPolicy... policies)  ??
 * 
 * @author jbuhacoff
 */
public class PolicyEngine {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    public TrustReport applyAll(HostReport hostReport, TrustPolicy... policies) {
        RequireAll requireAll = new RequireAll(Arrays.asList(policies));
        TrustReport report = requireAll.apply(hostReport);
        return report;
    }
    
    public TrustReport applyAny(HostReport hostReport, TrustPolicy... policies) {
        RequireAny requireAny = new RequireAny(Arrays.asList(policies));
        TrustReport report = requireAny.apply(hostReport);
        return report;
    }
    
    public TrustReport apply(HostReport hostReport, TrustPolicy policy) {
        return applyAll(hostReport, policy);
    }
    
}
