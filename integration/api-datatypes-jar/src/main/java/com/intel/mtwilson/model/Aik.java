/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.model;

import java.security.PublicKey;

/**
 * XXX draft
 * @author jbuhacoff
 */
public class Aik {
    private PublicKey publicKey;

    public Aik(PublicKey aikPublicKey) {
        this.publicKey = aikPublicKey;
    }
    
    public PublicKey getPublicKey() { return publicKey; }
    
}
