/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

/**
 *
 * @author ssbangal
 */
public enum HostVMMType {
    
    CITRIX_XENSERVER("Citrix XenServer","0,17,18"),
    HOST_NO_VMM("Host without VMM", "0,17,18,19"),
    KVM("KVM","0,17,18,19"),
    VMWARE("VMware ESXi","0,17,18,19,20"),
    XEN("Xen","0,17,18");
    
    
    private String value;
    private String pcrs;

    private HostVMMType(String value, String pcrs){
        this.setValue(value);
        this.setPcrs(pcrs);
    }
    
    public String getPcrs() {
        return pcrs;
    }

    public void setPcrs(String pcrs) {
        this.pcrs = pcrs;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
