/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions.factorystyle;

/**
 *
 * @author jbuhacoff
 */
public class Voip implements Telephone {
    private String provider;
    public Voip(String provider) {
        this.provider = provider;
    }
    @Override
    public String call(String number) {
        return String.format("Dialing %s using internet VOIP provider %s", number, provider);
    }
    
}
