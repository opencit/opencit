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
 * @author dsmagadx
 */
public class HostTrustStatusResponse extends AuthResponse {


    
    private List<HostTrustLevel1String> hosts = new ArrayList<HostTrustLevel1String>();

    @JsonProperty("hosts")
    public List<HostTrustLevel1String> getHosts() {
        return hosts;
    }
    @JsonProperty("hosts")
    public void setHosts(List<HostTrustLevel1String> hosts) {
        this.hosts = hosts;
    }

    public HostTrustStatusResponse(AuthResponse authResponse) {
        super(authResponse);
    }

    public HostTrustStatusResponse() {
        
    }

}
