/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson.v2api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public abstract class TxtHostRecordListMixIn {

     @JsonProperty("host_records")
    public abstract List<TxtHostRecord> getHostRecords();

     @JsonProperty("host_records")
    public abstract void setHostRecords(List<TxtHostRecord> hostRecords);
    
}
