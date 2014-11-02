/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions.factorystyle;

import com.intel.mtwilson.pipe.Filter;

/**
 *
 * @author jbuhacoff
 */
public class AcmeTelephoneFactory implements TelephoneFactory,Filter<String> {
    private String carrier = "Acme, Inc."; // pretend configuration value from somewhere
    
    @Override
    public Telephone create() {
        return new BrandedCellphone(carrier);
    }

    @Override
    public boolean accept(String item) {
        return "cell".equals(item);
    }
}
