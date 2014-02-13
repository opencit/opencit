/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import org.apache.commons.lang3.Validate;
import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author dsmagadx
 */
public class RegisterHostRequest /*extends AuthRequest*/ {

    private String hostName;
    private String ipAddress;
    private String bios;
    private String biosBuildNo;
    private String biosBuildOem;
    private String vmm;
    private String vmmBuildNo;
    private String vmmOsName;
    private String vmmOsVersion;
    private String emailAddress;
    private int port;
    private int cacheValidityMins;
    private String addonConnectionString;
    private String description;

    public RegisterHostRequest(String clientId, String userName, String password, String hostName) {
     //   super(clientId, userName, password);
        setHostName(hostName);
    }

    public RegisterHostRequest() {
    }

    @JsonProperty("addon_connection_string")
    public String getAddonConnectionString() {
        return addonConnectionString;
    }

    @JsonProperty("addon_connection_string")
    public final void setAddonConnectionString(String addonConnectionString) {
        this.addonConnectionString = addonConnectionString;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public final void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("vmm")
    public String getVMM() {
        return vmm;
    }

    @JsonProperty("vmm")
    public final void setVMM(String vmm) {
        this.vmm = vmm;
    }

    @JsonProperty("vmm_build_no")
    public String getVMMBuildNo() {
        return vmmBuildNo;
    }

    @JsonProperty("vmm_build_no")
    public final void setVMMBuildNo(String osBuildNo) {
        this.vmmBuildNo = osBuildNo;
    }

    @JsonProperty("bios_build_no")
    public String getBiosBuildNo() {
        return biosBuildNo;
    }

    @JsonProperty("bios_build_no")
    public final void setBiosBuildNo(String biosBuildNo) {
        this.biosBuildNo = biosBuildNo;
    }

    @JsonProperty("cache_validity_mins")
    public int getCacheValidityMins() {
        return cacheValidityMins;
    }

    @JsonProperty("cache_validity_mins")
    public final void setCacheValidityMins(int cacheValidityMins) {
        this.cacheValidityMins = cacheValidityMins;
    }

    @JsonProperty("email_address")
    public String getEmailAddress() {
        return emailAddress;
    }

    @JsonProperty("email_address")
    public final void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @JsonProperty("host_name")
    public String getHostName() {
    	Validate.notNull(hostName);
        return hostName;
    }

    @JsonProperty("host_name")
    public final void setHostName(String hostName) {
    	
    	Validate.notNull(hostName);
    	
    	if(hostName != null )
    		this.hostName = hostName;
    }

    @JsonProperty("ip_address")
    public String getIpAddress() {
        return ipAddress;
    }

    @JsonProperty("ip_address")
    public final void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @JsonProperty("bios")
    public String getBios() {
        return bios;
    }

    @JsonProperty("bios")
    public final void setBios(String bios) {
        this.bios = bios;
    }

    @JsonProperty("port")
    public int getPort() {
        return port;
    }

    @JsonProperty("port")
    public final void setPort(int port) {
        this.port = port;
    }

    @JsonProperty("bios_build_oem")
    public String getBiosBuildOem() {
        return biosBuildOem;
    }

    @JsonProperty("bios_build_oem")
    public void setBiosBuildOem(String biosBuildOem) {
        this.biosBuildOem = biosBuildOem;
    }

    @JsonProperty("vmm_os_name")
    public String getVmmOsName() {
        return vmmOsName;
    }

    @JsonProperty("vmm_os_name")
    public void setVmmOsName(String vmmOsName) {
        this.vmmOsName = vmmOsName;
    }

    @JsonProperty("vmm_os_version")
    public String getVmmOsVersion() {
        return vmmOsVersion;
    }

    @JsonProperty("vmm_os_version")
    public void setVmmOsVersion(String vmmOsVersion) {
        this.vmmOsVersion = vmmOsVersion;
    }
}
