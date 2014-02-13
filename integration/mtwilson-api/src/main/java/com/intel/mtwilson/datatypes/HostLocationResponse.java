package com.intel.mtwilson.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

public class HostLocationResponse extends AuthResponse{
	
	public HostLocationResponse(AuthResponse authResponse) {
		super(authResponse);
	}

	@JsonProperty public String location = null;

}
