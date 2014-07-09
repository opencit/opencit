/**
 * 
 */
package com.intel.mountwilson.datamodel;

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
        private String biosWLTarget;
        private String vmmWLtarget;
        //private String selectionTarget;
        private boolean registered;
	
        private String tlsPolicyId; // optional
        private String tlsPolicyType; // optional, typically used instead of tlsPolicyId
        private String tlsPolicyData; // optional extra data to accompany tlsPolicyType, for example hex-encoded certificate fingerprint or a complete base64-encoded certificate or public key
        
        // Modified the BIOS and VMM target variables to be of String instead of HostWhiteListTarget for easier reverse mapping when it would be used in the ManagementConsoleServiceImpl file.
        public String getVmmWLtarget() {
                return vmmWLtarget;
        }

        public void setVmmWLtarget(String vmmWLtarget) {
                this.vmmWLtarget = vmmWLtarget;
        }

        public boolean isRegistered() {
                return registered;
        }

        // Modified the BIOS and VMM target variables to be of String instead of HostWhiteListTarget for easier reverse mapping when it would be used in the ManagementConsoleServiceImpl file.
        public String getBiosWLTarget() {
                return biosWLTarget;
        }

        public void setBiosWLTarget(String biosWLTarget) {
                this.biosWLTarget = biosWLTarget;
        }

        public void setRegistered(boolean registered) {
                this.registered = registered;
        }
	
        public String getHostType() {
                return hostType;
        }

        public void setHostType(String hostType) {
                this.hostType = hostType;
        }

        public String getHostName() {
                return hostName;
        }

        public String getHostPortNo() {
                return hostPortNo;
        }

        public String getvCenterString() {
                return vCenterString;
        }

        public boolean isVmWareType() {
                return vmWareType;
        }

        public String getStatus() {
                return status;
        }

        public void setHostName(String hostName) {
                this.hostName = hostName;
        }

        public void setHostPortNo(String hostPortNo) {
                this.hostPortNo = hostPortNo;
        }

        public void setvCenterString(String vCenterString) {
                this.vCenterString = vCenterString;
        }

        public void setVmWareType(boolean vmWareType) {
                this.vmWareType = vmWareType;
        }

        public void setStatus(String status) {
                this.status = status;
        }

    //public void setSelectionTarget(String selectionTarget) {
    //    this.selectionTarget = selectionTarget;
    //}
    //public String getSelectionTarget() {
    //    return this.selectionTarget;
    //}
    public String getTlsPolicyId() {
        return tlsPolicyId;
    }

    public String getTlsPolicyType() {
        return tlsPolicyType;
    }

    public String getTlsPolicyData() {
        return tlsPolicyData;
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
                return "HostDetails [hostType=" + hostType + ", hostName=" + hostName
                                + ", hostPortNo=" + hostPortNo + ", vCenterString="
                                + vCenterString + ", vmWareType=" + vmWareType + ", status="
                                + status + ", config=" + vmmWLtarget + ", registered=" + registered //+ ", selectionTarget=" + selectionTarget
                                + "]";
        }
	
}
