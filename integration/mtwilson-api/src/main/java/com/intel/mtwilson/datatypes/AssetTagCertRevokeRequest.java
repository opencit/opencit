/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author ssbangal
 */
public class AssetTagCertRevokeRequest {

    private byte[] sha1OfAssetCert;

    @JsonProperty("SHA1Hash")
    public byte[] getSha1OfAssetCert() {
        return sha1OfAssetCert;
    }

    @JsonProperty("SHA1Hash")
    public void setSha1OfAssetCert(byte[] sha1OfAssetCert) {
        this.sha1OfAssetCert = sha1OfAssetCert;
    }
}
