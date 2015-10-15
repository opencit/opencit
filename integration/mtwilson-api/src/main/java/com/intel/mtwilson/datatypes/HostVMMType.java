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
    KVM("KVM","0,17,18"),
    VMWARE("VMware ESXi","0,17,18,19,20"),
    XEN("Xen","0,17,18"),
    WINDOWS("WINDOWS", "0,13,14");
    
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
