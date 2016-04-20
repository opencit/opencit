/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.factory;

import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;

/**
 *
 * @author jbuhacoff
 */
public class TlsPolicyDescriptorInvalidException extends IllegalArgumentException {
    private TlsPolicyDescriptor descriptor;
    
    public TlsPolicyDescriptorInvalidException(TlsPolicyDescriptor descriptor) {
        super();
        this.descriptor = descriptor;
    }

    public TlsPolicyDescriptorInvalidException(String message) {
        super(message);
    }

    public TlsPolicyDescriptorInvalidException(String message, TlsPolicyDescriptor descriptor) {
        super(message);
        this.descriptor = descriptor;
    }
    
    public TlsPolicyDescriptorInvalidException(Throwable cause, TlsPolicyDescriptor descriptor) {
        super(cause);
        this.descriptor = descriptor;
    }

    public TlsPolicyDescriptorInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public TlsPolicyDescriptorInvalidException(String message, Throwable cause, TlsPolicyDescriptor descriptor) {
        super(message, cause);
        this.descriptor = descriptor;
    }

    public TlsPolicyDescriptor getDescriptor() {
        return descriptor;
    }
    
    
}
