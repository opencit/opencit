/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson.v2api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author jbuhacoff
 */
public abstract class ManifestDataMixIn {

    @JsonProperty("name")
    public abstract void setName(String name);

    @JsonProperty("name")
    public abstract String getName();

    @JsonProperty("value")
    public abstract void setValue(String value);

    @JsonProperty("value")
    public abstract String getValue();
}
