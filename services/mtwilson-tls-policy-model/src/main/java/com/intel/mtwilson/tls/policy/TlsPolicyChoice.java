/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;


/**
 * A choice of tls policy can be an id of a private or shared policy, 
 * a name of a special policy type, or a complete tls policy descriptor.
 * Only one of these can be set - the others are automatically cleared
 * when one of them is set. 
 * Therefore, it doesn't matter in what order the fields are checked.
 * However, the order in which they are set matters - only the last one
 * to be set will be kept (as long as it is non-null).
 * If none of them are set then no choice is made - the application can
 * interpret this as a signal to use a default choice or fail if no other
 * choice of tls policy is available.
 * 
 * In Mt Wilson 1.2 the tls policy names were INSECURE, TRUST_FIRST_CERTIFICATE,
 * TRUST_KNOWN_CERTIFICATE, and TRUST_CA_VERIFY_HOSTNAME
 * 
 * In Mt Wilson 2.0 the TRUST_KNOWN_CERTIFICATE and the TRUST_FIRST_CERTIFICATE
 * policies are public key policies, and the TRUST_CA_VERIFY_HOSTNAME policy
 * is the certificate policy.
 * 
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="tlsPolicyChoice")
public class TlsPolicyChoice {
    @JsonProperty
    private String tlsPolicyId; // key into the mw_tls_policy table
    @JsonProperty
    private TlsPolicyDescriptor tlsPolicyDescriptor;

    public String getTlsPolicyId() {
        return tlsPolicyId;
    }
    public TlsPolicyDescriptor getTlsPolicyDescriptor() {
        return tlsPolicyDescriptor;
    }

    public void setTlsPolicyId(String tlsPolicyId) {
        this.tlsPolicyId = tlsPolicyId;
        if( tlsPolicyId != null ) {
        this.tlsPolicyDescriptor = null;
        }
    }

    public void setTlsPolicyDescriptor(TlsPolicyDescriptor tlsPolicyDescriptor) {
        this.tlsPolicyDescriptor = tlsPolicyDescriptor;
        if( tlsPolicyDescriptor != null && tlsPolicyDescriptor.getPolicyType() != null && !tlsPolicyDescriptor.getPolicyType().isEmpty()) {
        this.tlsPolicyId = null;
        }
    }
    
    
    
    
}
