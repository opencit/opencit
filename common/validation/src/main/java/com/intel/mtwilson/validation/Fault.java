/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.validation;

/**
 *
 * @author jbuhacoff
 */
public class Fault {
    private String description;
    public Fault(String description) {
        this.description = description;
    }
    @Override
    public String toString() {
        return description;
    }
}
