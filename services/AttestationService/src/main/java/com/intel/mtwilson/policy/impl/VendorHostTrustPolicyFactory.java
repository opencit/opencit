/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.impl;

import com.intel.mtwilson.as.data.MwAssetTagCertificate;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.model.Bios;
import com.intel.mtwilson.model.Vmm;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.Policy;
import com.intel.mtwilson.policy.Rule;
import java.util.List;
import java.util.Set;

/**
 * The job of a VendorHostTrustPolicyFactory is, given a complete host report from that
 * same vendor's HostAgent, to instantiate a list of TrustPolicy objects that are appropriate
 * for that host. For example, an Intel host needs a policy to verify the AIK.  
 * For a VMWare host we might need a policy to check the "is event log trusted" flag from vcenter,
 * or some other vmware-specific data.
 * 
 * Implementations of this interface DO NOT load policy information from or save it to a database. 
 * 
 * @author jbuhacoff
 */
public interface VendorHostTrustPolicyFactory {
    
//    Set<Rule> createWhitelistFromHost(HostReport hostReport); // without host-specific rules
//    Set<Rule> createHostSpecificRules(HostReport hostReport); // my conceptual problem is ... these could be BIOS or VMM .... does it really matter ?????????
    Set<Rule> loadTrustRulesForBios(Bios bios, TblHosts host);
//    Set<Rule> loadComparisonRulesForBios(Bios vmm, TblHosts host); // not needed currently
    Set<Rule> loadTrustRulesForVmm(Vmm vmm, TblHosts host);
    Set<Rule> loadComparisonRulesForVmm(Vmm vmm, TblHosts host); // similar to loadTrustRulesForVmm  but excluding the dynamic modules and excluding the host specific modules
    Set<Rule> loadTrustRulesForLocation(String location, TblHosts host);
     Set<Rule> loadTrustRulesForAssetTag(MwAssetTagCertificate atagCerts, TblHosts host);
}
