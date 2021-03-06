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
public abstract class AssetTagCertRevokeRequestMixIn {

    @JsonProperty("sha1_hash")
    public abstract byte[] getSha1OfAssetCert();

    @JsonProperty("sha1_hash")
    public abstract void setSha1OfAssetCert(byte[] sha1OfAssetCert);
    
}
