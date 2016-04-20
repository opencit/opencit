package com.intel.mtwilson.datatypes;

import com.intel.mtwilson.model.Hostname;
import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @since 0.5.2
 * @author jbuhacoff
 */
public class HostTrustResponse
{
    @JsonProperty("hostname") public Hostname hostname;
    @JsonProperty("trust") public HostTrustStatus trust;

    public HostTrustResponse() {
        
    }
    
    public HostTrustResponse(Hostname hostname, HostTrustStatus trust) {
        this.hostname = hostname;
        this.trust = trust;
    }    
}
