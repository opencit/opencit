/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import java.security.cert.X509Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is kept for compatibility with the previous version of
 * mtwilson-client-java6. In the new version, TrustAssertion class moved 
 * to the com.intel.mtwilson.saml package.
 * 
 * @author jbuhacoff
 */
public class TrustAssertion extends com.intel.mtwilson.saml.TrustAssertion {
    private final Logger log = LoggerFactory.getLogger(getClass());
    

    public TrustAssertion(X509Certificate[] trustedSigners, String xml) {
        super(trustedSigners, xml);
    }
    
}
