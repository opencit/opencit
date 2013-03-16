package com.intel.mountwilson.manifest.data;

import java.util.logging.Logger;

/**
 * XXX this interface includes "eventName" which currently is vmware-specific,
 * with possible values:
 *   Vim25Api.HostTpmSoftwareComponentEventDetails
 *   Vim25Api.HostTpmOptionEventDetails
 *   Vim25Api.HostTpmBootSecurityOptionEventDetails
 *   Vim25Api.HostTpmCommandEventDetails
 * and these seem to have little or no bearing on our attestation work.
 * Sudhir might be able to explain it.
 */
public class ModuleManifest implements IManifest {

    Logger log = Logger.getLogger(getClass().getName());
    private String componentName;
    private String digestValue;
    private String packageName;
    private String packageVendor;
    private String packageVersion;
    private String eventName;
    private String fieldName;
    private String packageNamespace;
    private String vendorName;
    private String description;
    /*
     * Set Only if Untrusted
     */
    private String whiteListValue;

    public String getWhiteListValue() {
        return whiteListValue;
    }

    public void setWhiteListValue(String whiteListValue) {
        this.whiteListValue = whiteListValue;
    }

    @Override
    public boolean verify(IManifest goodKnownValue) {

        log.info(String.format("%s GKV [%s] Manifest [%s] ", String.format("%s-%s-%s-%s", eventName, componentName, packageName, packageVendor, packageVersion),
                ((ModuleManifest) goodKnownValue).getDigestValue(), getDigestValue()));
        if (((ModuleManifest) goodKnownValue).getDigestValue() == null) {
            return true;
        }

        if (getDigestValue() == null) {
            return false;
        }

        if (digestValue.equals(((ModuleManifest) goodKnownValue).getDigestValue())) {
            return true;
        }
        return false;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getDigestValue() {
        return digestValue;
    }

    public void setDigestValue(String digestValue) {
        this.digestValue = digestValue;
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

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getPackageNamespace() {
        return packageNamespace;
    }

    public void setPackageNamespace(String packageNamespace) {
        this.packageNamespace = packageNamespace;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // return the has of the key elements
    public String getMFKey() {
        //Integer key = String.format("%s-%s-%s-%s", eventName,componentName,packageName,packageVendor,packageVersion).hashCode();
        //log.info("returning key - " + componentName);

        if (componentName != null) {
            return componentName.trim();
        } else {
            return "";
        }
    }
}
