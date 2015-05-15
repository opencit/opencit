/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson.v2api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author jbuhacoff
 */
public abstract class ModuleWhiteListMixIn {

    @JsonProperty("component_name")
    public abstract String getComponentName();

    @JsonProperty("component_name")
    public abstract void setComponentName(String componentName);

    @JsonProperty("description")
    public abstract String getDescription();

    @JsonProperty("description")
    public abstract void setDescription(String description);

    @JsonProperty("digest_value")
    public abstract String getDigestValue();

    @JsonProperty("digest_value")
    public abstract void setDigestValue(String digestValue);

    @JsonProperty("event_name")
    public abstract String getEventName();

    @JsonProperty("event_name")
    public abstract void setEventName(String eventName);

    @JsonProperty("extended_to_pcr")
    public abstract String getExtendedToPCR();

    @JsonProperty("extended_to_pcr")
    public abstract void setExtendedToPCR(String extendedToPCR);

    @JsonProperty("mle_name")
    public abstract String getMleName();

    @JsonProperty("mle_name")
    public abstract void setMleName(String mleName);

    @JsonProperty("mle_version")
    public abstract String getMleVersion();

    @JsonProperty("mle_version")
    public abstract void setMleVersion(String mleVersion);

    @JsonProperty("oem_name")
    public abstract String getOemName();

    @JsonProperty("oem_name")
    public abstract void setOemName(String oemName);

    @JsonProperty("os_name")
    public abstract String getOsName();

    @JsonProperty("os_name")
    public abstract void setOsName(String osName);

    @JsonProperty("os_version")
    public abstract String getOsVersion();

    @JsonProperty("os_version")
    public abstract void setOsVersion(String osVersion);

    @JsonProperty("package_name")
    public abstract String getPackageName();

    @JsonProperty("package_name")
    public abstract void setPackageName(String packageName);

    @JsonProperty("package_vendor")
    public abstract String getPackageVendor();

    @JsonProperty("package_vendor")
    public abstract void setPackageVendor(String packageVendor);

    @JsonProperty("package_version")
    public abstract String getPackageVersion();

    @JsonProperty("package_version")
    public abstract void setPackageVersion(String packageVersion);

    @JsonProperty("use_host_specific_digest")
    public abstract Boolean getUseHostSpecificDigest();

    @JsonProperty("use_host_specific_digest")
    public abstract void setUseHostSpecificDigest(Boolean useHostSpecificDigest);
    
}
