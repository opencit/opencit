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
public abstract class PCRWhiteListMixIn {

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

    @JsonProperty("pcr_digest")
    public abstract String getPcrDigest();

    @JsonProperty("pcr_digest")
    public abstract void setPcrDigest(String pcrDigest);

    @JsonProperty("pcr_name")
    public abstract String getPcrName();

    @JsonProperty("pcr_name")
    public abstract void setPcrName(String pcrName);
}
