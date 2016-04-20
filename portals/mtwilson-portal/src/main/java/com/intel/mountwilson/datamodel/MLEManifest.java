/**
 * 
 */
package com.intel.mountwilson.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author yuvrajsx
 *
 */
public class MLEManifest {

	@JsonProperty("Name")
	private String name;
	
	@JsonProperty("Value")
    private String value;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	
	@Override
	public String toString() {
		return "MLEManifest [name=" + name + ", value=" + value + "]";
	}
	
	
}
