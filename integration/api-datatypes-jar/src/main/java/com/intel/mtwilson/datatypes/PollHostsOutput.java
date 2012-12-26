package com.intel.mtwilson.datatypes;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author dsmagadx
 */
public class PollHostsOutput {
    
    @JsonProperty("PollHosts") public  OpenStackHostTrustLevelReport hostTrusts = null;
    
}
