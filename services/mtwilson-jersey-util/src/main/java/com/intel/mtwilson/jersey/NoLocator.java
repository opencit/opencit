/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey;

/**
 *
 * @author jbuhacoff
 */
public class NoLocator<T> implements Locator<T> {

    @Override
    public void copyTo(T item) {
        // TODO  delete this class after all resources have locators
    }
    
}
