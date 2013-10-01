/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.model;

/**
 *
 * @author jbuhacoff
 */
public enum YesNo {
    YES(true),
    NO(false);
    
    private boolean value;
    
    YesNo(boolean value) {
        this.value = value;
    }
    
    public boolean booleanValue() { return value; }
    
    public static YesNo valueOf(char c) {
        if( c == 'y' || c == 'Y' ) { return YES; }
        if( c == 'n' || c == 'N' ) { return NO; }
        throw new IllegalArgumentException("'Yes' or 'No' is required");
    }
}
