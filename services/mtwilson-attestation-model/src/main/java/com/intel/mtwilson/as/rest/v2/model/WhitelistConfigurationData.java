package com.intel.mtwilson.as.rest.v2.model;

import com.intel.mtwilson.datatypes.HostConfigData;

public class WhitelistConfigurationData extends HostConfigData {
   
    private String biosMleName;
    private String vmmMleName;

    public WhitelistConfigurationData() {
        super();
        biosMleName = "";
        vmmMleName = "";
    }
    
    public WhitelistConfigurationData(HostConfigData hostConfigObj) {
        super(hostConfigObj);
        biosMleName = "";
        vmmMleName = "";
    }

    public String getBiosMleName() {
        return biosMleName;
    }

    public void setBiosMleName(String biosMleName) {
        this.biosMleName = biosMleName;
    }

    public String getVmmMleName() {
        return vmmMleName;
    }

    public void setVmmMleName(String vmmMleName) {
        this.vmmMleName = vmmMleName;
    }
    	
}
