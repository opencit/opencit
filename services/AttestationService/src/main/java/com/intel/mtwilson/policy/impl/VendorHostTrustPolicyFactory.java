/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.impl;

import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.Rule;
import java.util.List;

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
    
    /**
     * The purpose of this method is to create a list of policies specific to the host report.
     * This is used to create a "whitelist" policy out of a "golden master host", and
     * also to create a "host-specific policy" out of any host  (which is the same as making
     * a "whitelist" that applies just to that host). 
     * 
     * Two important notes:
     * 
     * First, the list returned from this method need not be complete -- it is only necessary
     * to return policies that are specific to this vendor's hosts and would not necessarily
     * apply to another vendor's hosts.
     * 
     * Second, the result will be used as a whitelist so it may contain some constant values
     * (all hosts assigned to this whitelist need to have the SAME value for some attribute)
     * and also some variable values (all hosts assigned to this whitelist each have a unique
     * UUID that gets extended to PCR 0, for example, but the sequence of extensions to PCR 0
     * is the same for all of them so the whitelist will contain a variable value for the UUID
     * and it will get filled in when each host's trust status is evaluated by the trust policy
     * engine)
     * 
     * @return 
     */
    List<Rule> createTrustPolicyWhitelistFromHost(TblHosts host, HostReport hostReport); // XXX TODO need a way to accep tthe pcr list from the UI , right? 17,18,19,20,  or 0, or 22, or whatever. ??? for CUSTOMIZATIONS,  because defaults would be encoded by the vendors.
    
    
}
