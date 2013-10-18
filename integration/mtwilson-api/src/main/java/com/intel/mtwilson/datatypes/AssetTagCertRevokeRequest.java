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

    private byte[] sha1OfAssetCert;

    @JsonProperty("SHA1Hash")
    public byte[] getSha1fAssetCert() {
        return sha1OfAssetCert;
    }

    @JsonProperty("SHA1Hash")
    public void setSha256OfAssetCert(byte[] sha1OfAssetCert) {
        this.sha1OfAssetCert = sha1OfAssetCert;
    }
}
