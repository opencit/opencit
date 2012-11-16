package com.intel.mtwilson.datatypes;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;

/**
 * Currently the pcrMask field is ignored by PollHosts and HostTrustBO.
 *
 * @author dsmagadx
 */
public class HostTrustInput {

    /**
     * This field is currently ignored by the PollHosts API.
     */
    @JsonProperty("count")
    public int count;

    /**
     * This field is currently ignored by the PollHosts API.
     */
    @JsonProperty("pcrmask")
    public String pcrMask;
    
    /**
     * A list of host names. 
     */
    @JsonProperty("hosts")
    public Hostname[] hosts; // datatype.Hostname
    
    @JsonSetter
    public void setHosts(String[] hosts) {
        this.hosts = new Hostname[hosts.length];
        for(int i=0; i<hosts.length; i++) {
            this.hosts[i] = new Hostname(hosts[i]);
        }
    }
}
