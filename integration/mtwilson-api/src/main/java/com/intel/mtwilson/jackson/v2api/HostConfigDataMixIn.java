/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson.v2api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.mtwilson.datatypes.HostVMMType;
import com.intel.mtwilson.datatypes.HostWhiteListTarget;
import com.intel.mtwilson.datatypes.TxtHostRecord;

/**
 *
 * @author jbuhacoff
 */
public abstract class HostConfigDataMixIn {

    @JsonProperty("host_vmm_type")
    public abstract HostVMMType getHostVmmType();

    @JsonProperty("host_vmm_type")    
    public abstract void setHostVmmType(HostVMMType hostVmmType);

    @JsonProperty("txt_host_record")
    public abstract TxtHostRecord getTxtHostRecord();

    @JsonProperty("txt_host_record")
    public abstract void setTxtHostRecord(TxtHostRecord txtHostRecord);
	
    @JsonProperty("add_bios_white_List")
    public abstract boolean addBiosWhiteList();
    
    @JsonProperty("add_bios_white_List")
    public abstract void setBiosWhiteList(boolean biosWhiteList);
    
    @JsonProperty("add_vmm_white_List")
    public abstract boolean addVmmWhiteList();
    
    @JsonProperty("add_vmm_white_List")
    public abstract void setVmmWhiteList(boolean vmmWhiteList);
    
    @JsonProperty("bios_white_list_target")
    public abstract HostWhiteListTarget getBiosWLTarget();
    
    @JsonProperty("bios_white_list_target")
    public abstract void setBiosWLTarget(HostWhiteListTarget biosWLTarget);
    
    @JsonProperty("vmm_white_list_target")
    public abstract HostWhiteListTarget getVmmWLTarget();
    
    @JsonProperty("vmm_white_list_target")
    public abstract void setVmmWLTarget(HostWhiteListTarget vmmWLTarget);
    
    @JsonProperty("bios_pcrs")
    public abstract String getBiosPCRs();
    
    @JsonProperty("bios_pcrs")
    public abstract void setBiosPCRs(String biosPCRs);
    
    @JsonProperty("vmm_pcrs")
    public abstract String getVmmPCRs();
    
    @JsonProperty("vmm_pcrs")
    public abstract void setVmmPCRs(String vmmPCRs);
    
    @JsonProperty("host_location")
    public abstract String getHostLocation();
    
    @JsonProperty("host_location")
    public abstract void setHostLocation(String hostLocation);
    
    @JsonProperty("register_host")
    public abstract boolean isRegisterHost();
    
    @JsonProperty("register_host")
    public abstract void setRegisterHost(boolean registerHost);
    
    @JsonProperty("overwrite_whitelist")
    public abstract boolean getOverWriteWhiteList();

    @JsonProperty("overwrite_whitelist")
    public abstract void setOverWriteWhiteList(boolean overWriteWhiteList);
    
}
