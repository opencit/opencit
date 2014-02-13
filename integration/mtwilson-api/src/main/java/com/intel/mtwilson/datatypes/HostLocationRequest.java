/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author dsmagadx
 */
public class HostLocationRequest /*extends AuthRequest*/ {

    private String hostName;
    

    @JsonProperty("host_name")
    public String getHostName() {
		return hostName;
	}

    @JsonProperty("host_name")
	public void setHostName(String hostName) {
    	if(hostName == null || hostName.isEmpty())
    		throw new IllegalArgumentException("hostName must not be null or empty"); //(ErrorCode.INVALID_PARAMETER, "Input host_name is empty.");	
		this.hostName = hostName;
	}

	public HostLocationRequest(String clientId, String userName, String password, String host) {
        //super(clientId, userName, password);
        setHostName(host);
    }

    public HostLocationRequest() {
        super();
    }




}
