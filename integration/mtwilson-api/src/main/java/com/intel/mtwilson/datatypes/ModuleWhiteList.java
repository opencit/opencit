/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author ssbangal
 */
public class ModuleWhiteList {
   
    private String componentName;
    private String digestValue;
    private String eventName;
    private String extendedToPCR;
    private String packageName;
    private String packageVendor;
    private String packageVersion;
    private Boolean useHostSpecificDigest;
    private String description;
    private String mleName;
    private String mleVersion;
    private String osName; 
    private String osVersion; 
    private String oemName;

    /**
     * Constructor for the ModuleWhiteList object. Note that based on the MLE type only OS or OEM information
     * need to populated.
     */
    public ModuleWhiteList() {
    }

    /**
     * Constructor for the ModuleWhiteList object. Note that based on the MLE type only OS or OEM information
     * need to populated.
     * 
     * @param componentName
     * @param digestValue
     * @param eventName
     * @param extendedToPCR
     * @param packageName
     * @param packageVendor
     * @param packageVersion
     * @param useHostSpecificDigest
     * @param description
     * @param mleName
     * @param mleVersion
     * @param osName
     * @param osVersion
     * @param oemName 
     */
    public ModuleWhiteList(String componentName, String digestValue, String eventName, String extendedToPCR, String packageName, String packageVendor, String packageVersion, Boolean useHostSpecificDigest, String description, String mleName, String mleVersion, String osName, String osVersion, String oemName) {
        this.componentName = componentName;
        this.digestValue = digestValue;
        this.eventName = eventName;
        this.extendedToPCR = extendedToPCR;
        this.packageName = packageName;
        this.packageVendor = packageVendor;
        this.packageVersion = packageVersion;
        this.useHostSpecificDigest = useHostSpecificDigest;
        this.description = description;
        this.mleName = mleName;
        this.mleVersion = mleVersion;
        this.osName = osName;
        this.osVersion = osVersion;
        this.oemName = oemName;
    }
    
    @JsonProperty("ComponentName")
    public String getComponentName() {
        return componentName;
    }

    @JsonProperty("ComponentName")
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    @JsonProperty("Description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("Description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("DigestValue")
    public String getDigestValue() {
        return digestValue;
    }

    @JsonProperty("DigestValue")
    public void setDigestValue(String digestValue) {
        this.digestValue = digestValue;
    }

    @JsonProperty("EventName")
    public String getEventName() {
        return eventName;
    }

    @JsonProperty("EventName")
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    @JsonProperty("ExtendedToPCR")
    public String getExtendedToPCR() {
        return extendedToPCR;
    }

    @JsonProperty("ExtendedToPCR")
    public void setExtendedToPCR(String extendedToPCR) {
        this.extendedToPCR = extendedToPCR;
    }

    @JsonProperty("MLEName")
    public String getMleName() {
        return mleName;
    }

    @JsonProperty("MLEName")
    public void setMleName(String mleName) {
        this.mleName = mleName;
    }

    @JsonProperty("MLEVersion")
    public String getMleVersion() {
        return mleVersion;
    }

    @JsonProperty("MLEVersion")
    public void setMleVersion(String mleVersion) {
        this.mleVersion = mleVersion;
    }

    @JsonProperty("OEMName")
    public String getOemName() {
        return oemName;
    }

    @JsonProperty("OEMName")
    public void setOemName(String oemName) {
        this.oemName = oemName;
    }

    @JsonProperty("OSName")
    public String getOsName() {
        return osName;
    }

    @JsonProperty("OSName")
    public void setOsName(String osName) {
        this.osName = osName;
    }

    @JsonProperty("OSVersion")
    public String getOsVersion() {
        return osVersion;
    }

    @JsonProperty("OSVersion")
    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    @JsonProperty("PackageName")
    public String getPackageName() {
        return packageName;
    }

    @JsonProperty("PackageName")
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @JsonProperty("PackageVendor")
    public String getPackageVendor() {
        return packageVendor;
    }

    @JsonProperty("PackageVendor")
    public void setPackageVendor(String packageVendor) {
        this.packageVendor = packageVendor;
    }

    @JsonProperty("PackageVersion")
    public String getPackageVersion() {
        return packageVersion;
    }

    @JsonProperty("PackageVersion")
    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    @JsonProperty("UseHostSpecificDigest")
    public Boolean getUseHostSpecificDigest() {
        return useHostSpecificDigest;
    }

    @JsonProperty("UseHostSpecificDigest")
    public void setUseHostSpecificDigest(Boolean useHostSpecificDigest) {
        this.useHostSpecificDigest = useHostSpecificDigest;
    }

}
