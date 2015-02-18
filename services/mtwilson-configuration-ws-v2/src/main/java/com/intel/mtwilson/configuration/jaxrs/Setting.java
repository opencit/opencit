/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration.jaxrs;

import com.intel.dcsg.cpg.io.Attributes;

/**
 * Represents a single configuration setting which is a (name,value) pair.
 * It extends Attributes so that metadata can be added later, for example
 * validation information, without breaking the API, but can be confirmed
 * into fields at a later time.
 * @author jbuhacoff
 */
public class Setting extends Attributes {
    private String name;
    private String value;

    public Setting() {
    }

    public Setting(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    
    
}
