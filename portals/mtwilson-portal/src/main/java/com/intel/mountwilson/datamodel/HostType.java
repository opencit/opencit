/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.datamodel;


public class HostType {
    
    public static enum hostOS{
    	VMWARE_OS("vmware","vmware.png", "false"),
        UBUNTU("ubuntu","ubuntu.png","true"),
        SUSE("suse","suse.png","true"),
        RHEL("rhel","rhel.png","true"),
        RED_HAT("redhat","rhel.png","true"),
        CITRIX_XENSERVER("xenserver","citrix.png","false"),
        WINDOWS("windows", "windows.png", "true");
        
        private String value;
        private String imageName;
        private String vmmImageNeeded;

        public String getVmmImageNeeded() {
            return vmmImageNeeded;
        }

        public void setVmmImageNeeded(String vmmImageNeeded) {
            this.vmmImageNeeded = vmmImageNeeded;
        }


    	private hostOS(String value, String imageName, String vmmImageNeeded) {
    		this.value = value;
    		this.imageName = imageName;
                this.vmmImageNeeded = vmmImageNeeded;
    	}

    	public String getValue() {
    		return value;
    	}

    	public void setValue(String value) {
    		this.value = value;
    	}

    	public String getImageName() {
    		return imageName;
    	}

    	public void setImageName(String imageName) {
    		this.imageName = imageName;
    	}
    }
    
    public static enum hostVMM{
    	QEMU("qemu","kvm.png"),
        KVM("kvm","kvm.png"),
        XEN("xen","xen.png"),
        HYPERV("hyper-v", "hyper-v.png");
       
        
        private String value;
        private String imageName;


    	private hostVMM(String value, String imageName) {
    		this.value = value;
    		this.imageName = imageName;
    	}


		public String getValue() {
			return value;
		}


		public void setValue(String value) {
			this.value = value;
		}


		public String getImageName() {
			return imageName;
		}


		public void setImageName(String imageName) {
			this.imageName = imageName;
		}
    	
    }
    
    
}
