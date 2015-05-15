/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson.v2api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.mtwilson.datatypes.HostConfigData;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public abstract class HostConfigDataListMixIn {

   @JsonProperty("host_records")
    public abstract List<HostConfigData> getHostRecords();

   @JsonProperty("host_records")
    public abstract void setHostRecords(List<HostConfigData> hostRecords);
    
}
