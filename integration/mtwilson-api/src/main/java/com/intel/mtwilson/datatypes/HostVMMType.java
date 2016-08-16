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

    CITRIX_XENSERVER("Citrix Xen","0,17,18,19"),
    // HOST_NO_VMM("Linux", "0,17,18,19"), // CIT 2.1
    // KVM("Linux KVM","0,17,18,19"), // CIT 2.1
    HOST_NO_VMM("Linux", "0,17,18"), // CIT 2.2 temporary
    KVM("Linux KVM","0,17,18"), // CIT 2.2 temporary
    XEN("Linux Xen","0,17,18"),
    VMWARE("VMware ESXi","0,17,18,19,20"),
    WINDOWS("Windows", "0,13,14");


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
