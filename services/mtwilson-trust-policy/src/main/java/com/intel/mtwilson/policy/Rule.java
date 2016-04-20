/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A TrustPolicy defines which PCRs must have what values. It is different
 * than simply defining a PcrManifest because, in addition to allowing for
 * a variety of expected values, it produces a report that details the compliance
 * or non-compliance of a given PcrManifest with the policy.
 * 
 * The contract of any implementation of TrustPolicy is that it be reusable.
 * That is, it should be possible to call apply() repeatedly on different
 * HostReport instances and get a correct report for each one. The TrustPolicy
 * instance itself should NOT maintain any state regarding the last instance 
 * checked - the entire result set of apply() must be returned via the TrustReport.
 * 
 * @since 1.2
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="rule_name")
public interface Rule {
    RuleResult apply(HostReport hostReport); // applies the trust policy to the given host report and returns the resulting trust report
    String[] getMarkers(); // indicate the purpose of this policy; read like this "policy indicates that [markers] are trusted" where markers can be "bios", "vmm", "location", or user-defined;  BUT the trust is actually defined by "AND" of all policies that declare a marker... so "vmm" only trusted if all policies that include "vmm" in their markers list reported "trusted"
}
