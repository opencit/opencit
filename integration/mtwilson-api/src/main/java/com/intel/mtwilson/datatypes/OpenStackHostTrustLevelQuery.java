package com.intel.mtwilson.datatypes;

import com.intel.mtwilson.model.Hostname;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
//import org.codehaus.jackson.annotate.JsonProperty;
//import org.codehaus.jackson.annotate.JsonSetter;

/**
 * Currently the pcrMask field is ignored by PollHosts and HostTrustBO.
 *
 * @author dsmagadx
 */
public class OpenStackHostTrustLevelQuery {

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
     * A list of host names.  OpenStack uses a string value (non-array) when
     * there is only one host to query instead of using a single-element array.
     * So when you deserialize this field you must allow for a single value:
     * 
     * "hosts":"single-host"
     * 
     * and an array of one or more hosts:
     * 
     * "hosts":["single-host"]
     * 
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
