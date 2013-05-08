/**
 * 
 */
package com.intel.mountwilson.datamodel;


/**
 * @author yuvrajsx
 *
 */


public class HostVmMappingVO {
	
    private String hostId;
	private String vmName;
	private int vmStatus;
	private int trustedHostPolicy;
	private int locationPolicy;

    public String getHostId() {
        return hostId;
    }

   public int getLocationPolicy() {
        return locationPolicy;
    }

    public int getTrustedHostPolicy() {
        return trustedHostPolicy;
    }

    public String getVmName() {
        return vmName;
    }

    public int getVmStatus() {
        return vmStatus;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public void setLocationPolicy(int locationPolicy) {
        this.locationPolicy = locationPolicy;
    }


    public void setTrustedHostPolicy(int trustedHostPolicy) {
        this.trustedHostPolicy = trustedHostPolicy;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public void setVmStatus(int vmStatus) {
        this.vmStatus = vmStatus;
    }

	@Override
	public String toString() {
		return "HostVmMappingVO [hostId=" + hostId + ", vmName=" + vmName
				+ ", vmStatus=" + vmStatus + ", trustedHostPolicy="
				+ trustedHostPolicy + ", locationPolicy=" + locationPolicy
				+ "]";
	}

	
}
