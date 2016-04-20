/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 
package com.intel.mtwilson.datatypes;

import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
//import org.codehaus.jackson.annotate.JsonIgnore;
//import org.codehaus.jackson.annotate.JsonProperty;
//import org.codehaus.jackson.annotate.JsonSetter;

/**
 *
 * @author dsmagadx
 */
public class HostTrustStatusRequest /*extends AuthRequest*/ {

    @JsonProperty
    private String[] hostAddresses;
    
    @JsonProperty("force_verify")
    private Boolean forceVerify = false;

    
    public HostTrustStatusRequest(String clientId, String userName, String password, String hosts, Boolean forceVerify) {
       // super(clientId, userName, password);
        if(hosts != null)
        	setHostAddressList(hosts);
        setForceVerify(forceVerify);
    }

    public HostTrustStatusRequest() {
        super();
    }

    @JsonIgnore
    public Collection<String> getHostAddresses() {
        return Arrays.asList(hostAddresses);
    }
    /*
    @JsonGetter("hosts")
    public String[] getHostAddresses() {
        return hostAddresses;
    }
    */

    public final void setHostAddressList(String hosts) {
        
    	Validate.notNull(hosts);
    	 // for klocwork review tool
    		hostAddresses = StringUtils.split(hosts,",");
    }

    @JsonIgnore
    public Boolean getForceVerify() {
        return forceVerify;
    }

    @JsonSetter("force_verify")
    public final void setForceVerify(Boolean forceVerify) {
        if( forceVerify == null ) {
            this.forceVerify = Boolean.FALSE;
        }
        else {
            this.forceVerify = forceVerify;        
        }
    }

}
