/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson.v2api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.mtwilson.datatypes.HostTrust;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public abstract class BulkHostTrustResponseMixIn {

    @JsonProperty("hosts")
    public abstract List<HostTrust> getHosts();

    @JsonProperty("hosts")
    public abstract void setHosts(List<HostTrust> hosts);
    
}
