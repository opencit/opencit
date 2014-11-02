/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions.factorystyle;

/**
 *
 * @author jbuhacoff
 */
public class Landline implements Telephone {

    @Override
    public String call(String number) {
        return String.format("Dialing %s using landline", number);
    }
    
}
