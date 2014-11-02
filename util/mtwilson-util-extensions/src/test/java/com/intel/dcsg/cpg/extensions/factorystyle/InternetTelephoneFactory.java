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
public class InternetTelephoneFactory implements TelephoneFactory,Filter<String> {
    private VoipFactory voipFactory = new VoipFactory();
    
    @Override
    public Telephone create() {
        return voipFactory.create(); 
    }
    
    @Override
    public boolean accept(String item) {
        return "voip".equals(item);
    }
    
}
