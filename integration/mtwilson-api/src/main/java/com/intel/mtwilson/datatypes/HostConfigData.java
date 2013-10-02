package com.intel.mtwilson.datatypes;

import org.codehaus.jackson.annotate.JsonProperty;

public class HostConfigData {
   
    private boolean biosWhiteList;
    private boolean vmmWhiteList;    
    private HostWhiteListTarget biosWLTarget;
    private HostWhiteListTarget vmmWLTarget;
    private String biosPCRs;
    private String vmmPCRs;
    private String hostLocation;
    private boolean registerHost; 
    private HostVMMType hostVmmType;
    private TxtHostRecord txtHostRecord;
    private boolean overWriteWhiteList;

    // By default we will use the OEM as the white list target for both BIOS and VMM.
    public HostConfigData() {
        this.biosWhiteList = false;
        this.vmmWhiteList = false;
        this.biosWLTarget = HostWhiteListTarget.BIOS_OEM;
        this.vmmWLTarget = HostWhiteListTarget.VMM_OEM;
        this.biosPCRs = "";
        this.vmmPCRs = "";
        this.hostLocation = "";
        this.registerHost = false;
        this.hostVmmType = null;
        this.txtHostRecord = null;
        this.overWriteWhiteList = false;
    }
    
    @JsonProperty("Host_VMM_Type")
    public HostVMMType getHostVmmType() {
        return hostVmmType;
    }

    @JsonProperty("Host_VMM_Type")    
    public void setHostVmmType(HostVMMType hostVmmType) {
        this.hostVmmType = hostVmmType;
    }  

    @JsonProperty("TXT_Host_Record")
    public TxtHostRecord getTxtHostRecord() {
        return txtHostRecord;
    }

    @JsonProperty("TXT_Host_Record")
    public void setTxtHostRecord(TxtHostRecord txtHostRecord) {
        this.txtHostRecord = txtHostRecord;
    }
	
    @JsonProperty("Add_BIOS_WhiteList")
    public boolean addBiosWhiteList() {
        return biosWhiteList;
    }
    
    @JsonProperty("Add_BIOS_WhiteList")
    public void setBiosWhiteList(boolean biosWhiteList) {
        this.biosWhiteList = biosWhiteList;
    }
    
    @JsonProperty("Add_VMM_WhiteList")
    public boolean addVmmWhiteList() {
        return vmmWhiteList;
    }
    
    @JsonProperty("Add_VMM_WhiteList")
    public void setVmmWhiteList(boolean vmmWhiteList) {
        this.vmmWhiteList = vmmWhiteList;
    }
    
    @JsonProperty("BIOS_WhiteList_Target")
    public HostWhiteListTarget getBiosWLTarget() {
        return biosWLTarget;
    }
    
    @JsonProperty("BIOS_WhiteList_Target")
    public void setBiosWLTarget(HostWhiteListTarget biosWLTarget) {
        this.biosWLTarget = biosWLTarget;
    }
    
    @JsonProperty("VMM_WhiteList_Target")
    public HostWhiteListTarget getVmmWLTarget() {
        return vmmWLTarget;
    }
    
    @JsonProperty("VMM_WhiteList_Target")
    public void setVmmWLTarget(HostWhiteListTarget vmmWLTarget) {
        this.vmmWLTarget = vmmWLTarget;
    }
    
    @JsonProperty("BIOS_PCRS")
    public String getBiosPCRs() {
        return biosPCRs;
    }
    
    @JsonProperty("BIOS_PCRS")
    public void setBiosPCRs(String biosPCRs) {
        this.biosPCRs = biosPCRs;
    }
    
    @JsonProperty("VMM_PCRS")
    public String getVmmPCRs() {
        return vmmPCRs;
    }
    
    @JsonProperty("VMM_PCRS")
    public void setVmmPCRs(String vmmPCRs) {
        this.vmmPCRs = vmmPCRs;
    }
    
    @JsonProperty("Host_Location")
    public String getHostLocation() {
        return hostLocation;
    }
    
    @JsonProperty("Host_Location")
    public void setHostLocation(String hostLocation) {
        this.hostLocation = hostLocation;
    }
    
    @JsonProperty("Register_Host")
    public boolean isRegisterHost() {
        return registerHost;
    }
    
    @JsonProperty("Register_Host")
    public void setRegisterHost(boolean registerHost) {
        this.registerHost = registerHost;
    }
    
    @JsonProperty("Overwrite_Whitelist")
    public boolean getOverWriteWhiteList() {
        return overWriteWhiteList;
    }

    @JsonProperty("Overwrite_Whitelist")
    public void setOverWriteWhiteList(boolean overWriteWhiteList) {
        this.overWriteWhiteList = overWriteWhiteList;
    }

    @Override
    public String toString() {
        return "WhiteListConfig [biosWhiteList=" + biosWhiteList
                    + ", vmmWhiteList=" + vmmWhiteList + ", biosWLTarget="
                    + biosWLTarget + ", vmmWLTarget=" + vmmWLTarget + ", biosPCRs="
                    + biosPCRs + ", vmmPCRs=" + vmmPCRs + ", hostLocation="
                    + hostLocation + ", registerHost=" + registerHost + "]";
    }

	
}
