/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson.v2api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelReport;

/**
 *
 * @author jbuhacoff
 */
public abstract class PollHostsOutputMixIn {

    @JsonProperty("poll_hosts") public  OpenStackHostTrustLevelReport hostTrusts = null;
    
}
