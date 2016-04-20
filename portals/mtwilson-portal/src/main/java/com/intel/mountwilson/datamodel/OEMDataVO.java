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
public class OEMDataVO {
	
	@JsonProperty("Name")
	private String oemName;
	
	@JsonProperty("Description")
	private String oemDescription;

	/**
	 * @return the oemName
	 */
	public String getOemName() {
		return oemName;
	}

	/**
	 * @return the oemDescription
	 */
	public String getOemDescription() {
		return oemDescription;
	}

	/**
	 * @param oemName the oemName to set
	 */
	public void setOemName(String oemName) {
		this.oemName = oemName;
	}

	/**
	 * @param oemDescription the oemDescription to set
	 */
	public void setOemDescription(String oemDescription) {
		this.oemDescription = oemDescription;
	}

	
	@Override
	public String toString() {
		return "OEMDataVO [oemName=" + oemName + ", oemDescription="
				+ oemDescription + "]";
	}

}
