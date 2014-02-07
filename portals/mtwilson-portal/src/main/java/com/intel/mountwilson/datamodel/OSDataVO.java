/**
 * 
 */
package com.intel.mountwilson.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;
//import org.codehaus.jackson.map.annotate.JacksonInject;

/**
 * @author yuvrajsx
 *
 */
public class OSDataVO {
	
	@JsonProperty("Name")
	private String osName; 

	@JsonProperty("Version")
	private String osVersion; 
	
	@JsonProperty("Description")
	private String osDescription;
	
	
	/**
	 * @return the osName
	 */
	public String getOsName() {
		return osName;
	}
	/**
	 * @return the osVersion
	 */
	public String getOsVersion() {
		return osVersion;
	}
	/**
	 * @return the osDescription
	 */
	public String getOsDescription() {
		return osDescription;
	}
	
	
	
		/**
	 * @param osName the osName to set
	 */
	public void setOsName(String osName) {
		this.osName = osName;
	}
	/**
	 * @param osVersion the osVersion to set
	 */
	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}
	/**
	 * @param osDescription the osDescription to set
	 */
	public void setOsDescription(String osDescription) {
		this.osDescription = osDescription;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "OSDataVO [osName=" + osName + ", osVersion=" + osVersion
				+ ", osDescription=" + osDescription + "]";
	}
}
