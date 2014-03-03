/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.saml;

/**
 * Typically wraps MarshallingException, ConfigurationException, 
 * UnknownHostException, GeneralSecurityException, XMLSignatureException, 
 * or MarshalException 
 * 
 * @author jbuhacoff
 */
public class SamlException extends Exception {
    public SamlException() {
        super();
    }
    public SamlException(Throwable cause) {
        super(cause);
    }
    public SamlException(String message) {
        super(message);
    }
    public SamlException(String message, Throwable cause) {
        super(message, cause);
    }    
}
