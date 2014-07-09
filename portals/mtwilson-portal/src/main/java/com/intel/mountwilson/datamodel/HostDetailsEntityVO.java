package com.intel.mountwilson.datamodel;

import java.io.Serializable;
import java.util.Date;


/**
*
* @author Yuvraj Singh
*/

public class HostDetailsEntityVO implements Serializable {
	
	
	private static final long serialVersionUID = 1L;

	private String hostId;
    private String hostName;
	private String hostIPAddress;
	private String hostPort;
	private String hostDescription;
	private String biosName;
	private String biosBuildNo;
	private String vmmName;
	private String vmmBuildNo;
	private Date updatedOn;
	private String emailAddress;
	private String location;
	private String oemName;
	private String vCenterDetails;
    /**
     *  tlsPolicyId indicates a shared policy
     */
	private String tlsPolicyId;
    /**
     * tlsPolicyType and tlsPolicyData indicate a private policy
     */
    private String tlsPolicyType;
    private String tlsPolicyData;
    
	/**
	 * @return the vCenterDetails
	 */
	public String getvCenterDetails() {
		return vCenterDetails;
	}

	/**
	 * @param vCenterDetails the vCenterDetails to set
	 */
	public void setvCenterDetails(String vCenterDetails) {
		this.vCenterDetails = vCenterDetails;
	}

	/**
	 * @return the hostId
	 */
	public String getHostId() {
		return hostId;
	}

	/**
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * @return the hostIPAddress
	 */
	public String getHostIPAddress() {
		return hostIPAddress;
	}

	/**
	 * @return the hostPort
	 */
	public String getHostPort() {
		return hostPort;
	}

	/**
	 * @return the hostDescription
	 */
	public String getHostDescription() {
		return hostDescription;
	}

	/**
	 * @return the biosName
	 */
	public String getBiosName() {
		return biosName;
	}

	/**
	 * @return the biosBuildNo
	 */
	public String getBiosBuildNo() {
		return biosBuildNo;
	}

	/**
	 * @return the vmmName
	 */
	public String getVmmName() {
		return vmmName;
	}

	/**
	 * @return the vmmBuildNo
	 */
	public String getVmmBuildNo() {
		return vmmBuildNo;
	}


	/**
	 * @return the updatedOn
	 */
	public Date getUpdatedOn() {
		return updatedOn;
	}

	/**
	 * @return the emailAddress
	 */
	public String getEmailAddress() {
		return emailAddress;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @return the oemName
	 */
	public String getOemName() {
		return oemName;
	}

    /**
     * 
     * @return the UUID of the record in the mw_tls_policy table
     */
    public String getTlsPolicyId() {
        return tlsPolicyId;
    }

    /**
     * null if tls policy id is set,  or a policy type like "certificate" or "INSECURE"
     * @return 
     */
    public String getTlsPolicyType() {
        return tlsPolicyType;
    }

    /**
     * only set if tlsPolicyType is set to a type that requires additional data (certificate, public key, or digest)
     * @return 
     */
    public String getTlsPolicyData() {
        return tlsPolicyData;
    }
    
    

	/**
	 * @param string the hostId to set
	 */
	public void setHostId(String string) {
		this.hostId = string;
	}

	/**
	 * @param hostName the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * @param hostIPAddress the hostIPAddress to set
	 */
	public void setHostIPAddress(String hostIPAddress) {
		this.hostIPAddress = hostIPAddress;
	}

	/**
	 * @param hostPort the hostPort to set
	 */
	public void setHostPort(String hostPort) {
		this.hostPort = hostPort;
	}

	/**
	 * @param hostDescription the hostDescription to set
	 */
	public void setHostDescription(String hostDescription) {
		this.hostDescription = hostDescription;
	}

	/**
	 * @param biosName the biosName to set
	 */
	public void setBiosName(String biosName) {
		this.biosName = biosName;
	}

	/**
	 * @param biosBuildNo the biosBuildNo to set
	 */
	public void setBiosBuildNo(String biosBuildNo) {
		this.biosBuildNo = biosBuildNo;
	}

	/**
	 * @param vmmName the vmmName to set
	 */
	public void setVmmName(String vmmName) {
		this.vmmName = vmmName;
	}

	/**
	 * @param vmmBuildNo the vmmBuildNo to set
	 */
	public void setVmmBuildNo(String vmmBuildNo) {
		this.vmmBuildNo = vmmBuildNo;
	}


	/**
	 * @param updatedOn the updatedOn to set
	 */
	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	/**
	 * @param emailAddress the emailAddress to set
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @param oemName the oemName to set
	 */
	public void setOemName(String oemName) {
		this.oemName = oemName;
	}

    public void setTlsPolicyId(String tlsPolicyId) {
        this.tlsPolicyId = tlsPolicyId;
    }

    public void setTlsPolicyType(String tlsPolicyType) {
        this.tlsPolicyType = tlsPolicyType;
    }

    public void setTlsPolicyData(String tlsPolicyData) {
        this.tlsPolicyData = tlsPolicyData;
    }

    
    
	@Override
	public String toString() {
		return "HostDetailsEntityVO [hostId=" + hostId + ", hostName="
				+ hostName + ", hostIPAddress=" + hostIPAddress + ", hostPort="
				+ hostPort + ", hostDescription=" + hostDescription
				+ ", biosName=" + biosName + ", biosBuildNo=" + biosBuildNo
				+ ", vmmName=" + vmmName + ", vmmBuildNo=" + vmmBuildNo
				+ ", updatedOn=" + updatedOn + ", emailAddress=" + emailAddress
				+ ", location=" + location + ", oemName=" + oemName + ", tlsPolicyId="+tlsPolicyId+"]";
				// remove bad logs + ", vCenterDetails=" + vCenterDetails + 
	}

}
