package com.intel.mountwilson.datamodel;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

public class HostEntryServer {
	
	@JsonProperty("hosts")
	private List<Map<String, Object>> hosts;
	
	@JsonProperty("error_code")
	private long error_code;
	
	@JsonProperty("error_message")
	private String error_message;

	/**
	 * @return the hosts
	 */
	public List<Map<String, Object>> getHosts() {
		return hosts;
	}

	/**
	 * @return the error_code
	 */
	public long getError_code() {
		return error_code;
	}

	/**
	 * @return the error_message
	 */
	public String getError_message() {
		return error_message;
	}

	/**
	 * @param hosts the hosts to set
	 */
	public void setHosts(List<Map<String, Object>> hosts) {
		this.hosts = hosts;
	}

	/**
	 * @param error_code the error_code to set
	 */
	public void setError_code(long error_code) {
		this.error_code = error_code;
	}

	/**
	 * @param error_message the error_message to set
	 */
	public void setError_message(String error_message) {
		this.error_message = error_message;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "HostEntryServer [hosts=" + hosts + ", error_code=" + error_code
				+ ", error_message=" + error_message + "]";
	}

}
