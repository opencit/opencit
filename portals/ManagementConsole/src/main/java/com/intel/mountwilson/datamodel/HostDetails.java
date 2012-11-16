/**
 * 
 */
package com.intel.mountwilson.datamodel;

import com.intel.mtwilson.datatypes.HostWhiteListTarget;

/**
 * @author yuvrajsx
 *
 */
public class HostDetails {
	
	private String hostType;
	private String hostName;
	private String hostPortNo;
	private String vCenterString;
	private boolean vmWareType;
	private String status;
	private HostWhiteListTarget biosWLTarget;
	private HostWhiteListTarget vmmWLtarget;
	private boolean registered;
	
	
	
	
	public HostWhiteListTarget getVmmWLtarget() {
		return vmmWLtarget;
	}
	public void setVmmWLtarget(HostWhiteListTarget vmmWLtarget) {
		this.vmmWLtarget = vmmWLtarget;
	}
	public boolean isRegistered() {
		return registered;
	}
	public HostWhiteListTarget getBiosWLTarget() {
		return biosWLTarget;
	}
	public void setBiosWLTarget(HostWhiteListTarget biosWLTarget) {
		this.biosWLTarget = biosWLTarget;
	}
	public void setRegistered(boolean registered) {
		this.registered = registered;
	}
	
	/**
	 * @return the hostType
	 */
	public String getHostType() {
		return hostType;
	}
	/**
	 * @param hostType the hostType to set
	 */
	public void setHostType(String hostType) {
		this.hostType = hostType;
	}
	
	/**
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}
	/**
	 * @return the hostPortNo
	 */
	public String getHostPortNo() {
		return hostPortNo;
	}
	/**
	 * @return the vCenterString
	 */
	public String getvCenterString() {
		return vCenterString;
	}
	/**
	 * @return the vmWareType
	 */
	public boolean isVmWareType() {
		return vmWareType;
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param hostName the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	/**
	 * @param hostPortNo the hostPortNo to set
	 */
	public void setHostPortNo(String hostPortNo) {
		this.hostPortNo = hostPortNo;
	}
	/**
	 * @param vCenterString the vCenterString to set
	 */
	public void setvCenterString(String vCenterString) {
		this.vCenterString = vCenterString;
	}
	/**
	 * @param vmWareType the vmWareType to set
	 */
	public void setVmWareType(boolean vmWareType) {
		this.vmWareType = vmWareType;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	@Override
	public String toString() {
		return "HostDetails [hostType=" + hostType + ", hostName=" + hostName
				+ ", hostPortNo=" + hostPortNo + ", vCenterString="
				+ vCenterString + ", vmWareType=" + vmWareType + ", status="
				+ status + ", config=" + vmmWLtarget + ", registered=" + registered
				+ "]";
	}
	
}
