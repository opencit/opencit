package com.intel.mtwilson.datatypes;

import org.codehaus.jackson.annotate.JsonProperty;

public class HostLocationResponse extends AuthResponse{
	
	public HostLocationResponse(AuthResponse authResponse) {
		super(authResponse);
	}

	@JsonProperty public String location = null;

}
