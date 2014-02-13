package com.intel.mtwilson.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author dsmagadx
 */
public class HostTrustLevel1String
{
    @JsonProperty("host_name") public String hostname ;
    @JsonProperty("trust_lvl") public String trustLevel ;
    @JsonProperty("vtime") public String timestamp ;
    
}

