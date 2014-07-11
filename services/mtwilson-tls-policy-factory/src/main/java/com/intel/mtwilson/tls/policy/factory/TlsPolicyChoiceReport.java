/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.factory;

import com.intel.mtwilson.tls.policy.TlsPolicyChoice;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;

/**
 *
 * @author jbuhacoff
 */
public class TlsPolicyChoiceReport {
    private TlsPolicyChoice choice;
    private TlsPolicyDescriptor descriptor;
//    private String policyType;
    private String providerClassName;

    public TlsPolicyChoice getChoice() {
        return choice;
    }

    public TlsPolicyDescriptor getDescriptor() {
        return descriptor;
    }
/*
    public String getPolicyType() {
        return policyType;
    }*/

    public String getProviderClassName() {
        return providerClassName;
    }

    public void setChoice(TlsPolicyChoice choice) {
        this.choice = choice;
    }

    public void setDescriptor(TlsPolicyDescriptor descriptor) {
        this.descriptor = descriptor;
    }
/*
    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }*/

    public void setProviderClassName(String providerClassName) {
        this.providerClassName = providerClassName;
    }
    
    
}
