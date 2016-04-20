/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author dsmagadX
 */
public class BulkHostTrustResponse {
    private List<HostTrust> hosts = new ArrayList<HostTrust>();

    @JsonProperty("Hosts")
    public List<HostTrust> getHosts() {
        return hosts;
    }

    @JsonProperty("Hosts")
    public void setHosts(List<HostTrust> hosts) {
        this.hosts = hosts;
    }

    
}
