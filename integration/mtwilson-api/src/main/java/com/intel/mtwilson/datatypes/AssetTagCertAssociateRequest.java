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
public class AssetTagCertAssociateRequest {
    
    private byte[]  sha256OfAssetCert;
    private int     hostID;

    public AssetTagCertAssociateRequest() {
        this.sha256OfAssetCert = null;
        this.hostID = 0;
    }
            
    public AssetTagCertAssociateRequest(byte[] sha256OfAssetCert, int hostID) {
        this.sha256OfAssetCert = sha256OfAssetCert;
        this.hostID = hostID;
    }

    
    @JsonProperty("SHA256Hash")
    public byte[] getSha256OfAssetCert() {
        return sha256OfAssetCert;
    }

    @JsonProperty("SHA256Hash")
    public void setSha256OfAssetCert(byte[] sha256OfAssetCert) {
        this.sha256OfAssetCert = sha256OfAssetCert;
    }

    @JsonProperty("Host_ID")
    public int getHostID() {
        return hostID;
    }

    @JsonProperty("Host_ID")
    public void setHostID(int hostID) {
        this.hostID = hostID;
    }    
    
}
