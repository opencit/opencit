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

    private byte[] sha256OfAssetCert;
    private byte[] sha1OfAssetCert;


    @JsonProperty("SHA256Hash")
    public byte[] getSha256OfAssetCert() {
        return sha256OfAssetCert;
    }

    @JsonProperty("SHA256Hash")
    public void setSha256OfAssetCert(byte[] sha256OfAssetCert) {
        this.sha256OfAssetCert = sha256OfAssetCert;
    }
    
    @JsonProperty("SHA1Hash")
    public byte[] getSha1OfAssetCert() {
        return sha1OfAssetCert;
    }

    @JsonProperty("SHA1Hash")
    public void setSha1OfAssetCert(byte[] sha1OfAssetCert) {
        this.sha1OfAssetCert = sha1OfAssetCert;
    }
}
