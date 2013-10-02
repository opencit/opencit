/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author ssbangal
 */
public class AssetTagCertRevokeRequest {

    private byte[] sha256OfAssetCert;

    @JsonProperty("SHA256Hash")
    public byte[] getSha256OfAssetCert() {
        return sha256OfAssetCert;
    }

    @JsonProperty("SHA256Hash")
    public void setSha256OfAssetCert(byte[] sha256OfAssetCert) {
        this.sha256OfAssetCert = sha256OfAssetCert;
    }
}
