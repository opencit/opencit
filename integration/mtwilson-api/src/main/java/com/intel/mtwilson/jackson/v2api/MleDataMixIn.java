/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson.v2api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.mtwilson.datatypes.ManifestData;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public abstract class MleDataMixIn {

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

    @JsonProperty("name")
    public abstract void setName(String value);

    @JsonProperty("name")
    public abstract String getName();

    @JsonProperty("version")
    public abstract void setVersion(String value);

    @JsonProperty("version")
    public abstract String getVersion();
    
    @JsonProperty("attestation_type")
    public abstract void setAttestationType(String value);

    @JsonProperty("attestation_type")
    public abstract String getAttestationType();

    @JsonProperty("mle_type")
    public abstract void setMleType(String value);

    @JsonProperty("mle_type")
    public abstract String getMleType();
    
    @JsonProperty("description")
    public abstract void setDescription(String value);

    @JsonProperty("description")
    public abstract String getDescription();
    
    @JsonProperty("mle_manifests")
    public abstract void setManifestList(List<ManifestData> list);

    @JsonProperty("mle_manifests")
    public abstract List<ManifestData> getManifestList();
    
}
