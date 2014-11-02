/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions.factorystyle;

/**
 *
 * @author jbuhacoff
 */
public class BrandedCellphone implements Telephone {
    private String carrier;
    public BrandedCellphone(String carrier) {
        this.carrier = carrier;
    }
    
    @Override
    public String call(String number) {
        return String.format("Dialing %s wirelessly on %s network", number, carrier);
    }
    
}
