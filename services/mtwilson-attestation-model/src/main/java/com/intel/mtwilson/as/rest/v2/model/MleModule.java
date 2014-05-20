/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.Document;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="mle_module")
public class MleModule extends Document{
    
    private String mleUuid;
    private String moduleName;
    private String moduleValue;
    private String eventName;
    private String extendedToPCR;
    private String packageName;
    private String packageVendor;
    private String packageVersion;
    private Boolean useHostSpecificDigest;
    private String description;

    public String getMleUuid() {
        return mleUuid;
    }

    public void setMleUuid(String mleUuid) {
        this.mleUuid = mleUuid;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleValue() {
        return moduleValue;
    }

    public void setModuleValue(String moduleValue) {
        this.moduleValue = moduleValue;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getExtendedToPCR() {
        return extendedToPCR;
    }

    public void setExtendedToPCR(String extendedToPCR) {
        this.extendedToPCR = extendedToPCR;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageVendor() {
        return packageVendor;
    }

    public void setPackageVendor(String packageVendor) {
        this.packageVendor = packageVendor;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public Boolean getUseHostSpecificDigest() {
        return useHostSpecificDigest;
    }

    public void setUseHostSpecificDigest(Boolean useHostSpecificDigest) {
        this.useHostSpecificDigest = useHostSpecificDigest;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    
}
