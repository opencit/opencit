/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2;

import com.intel.mtwilson.repository.Locator;

/**
 *
 * @author jbuhacoff
 */
public class NoLocator<T> implements Locator<T> {

    @Override
    public void copyTo(T item) {
       
    }
    
}
