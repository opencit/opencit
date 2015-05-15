/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson.v2api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.mtwilson.datatypes.PcrLogReport;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public abstract class AttestationReportMixIn {

    @JsonProperty("pcr_log_report")
    public abstract List<PcrLogReport> getPcrLogs();
    
}
