/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.i18n;

/**
 * XXX this class should be deprecated because it doesn't work with a plugin
 * architecture
 * 
 * @author jbuhacoff
 */
public enum BundleName {
    MTWILSON_STRINGS("MtWilsonStrings"); // changed from mtwilson-strings to MtWilsonStrings because java convention is that resource bundles are equivalent to classes and should have the same naming convention
    
    private String bundleName;
    BundleName(String bundleName) {
        this.bundleName = bundleName;
    }
    
    public String bundle() {
        return bundleName;
    }
}
