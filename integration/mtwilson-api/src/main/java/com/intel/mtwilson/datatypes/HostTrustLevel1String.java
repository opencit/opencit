package com.intel.mtwilson.datatypes;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author dsmagadx
 */
public class HostTrustLevel1String
{
    @JsonProperty("host_name") public String hostname ;
    @JsonProperty("trust_lvl") public String trustLevel ;
    @JsonProperty("timestamp") public String timestamp ;
    
}

