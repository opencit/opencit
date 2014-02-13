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
public class AssetTagCertAssociateRequest {
    
    private byte[]  sha1OfAssetCert;
    private int     hostID;

    public AssetTagCertAssociateRequest() {
        this.sha1OfAssetCert = null;
        this.hostID = 0;
    }
            
    public AssetTagCertAssociateRequest(byte[] sha1OfAssetCert, int hostID) {
        this.sha1OfAssetCert = sha1OfAssetCert;
        this.hostID = hostID;
    }

    
    @JsonProperty("SHA1Hash")
    public byte[] getSha1OfAssetCert() {
        return sha1OfAssetCert;
    }

    @JsonProperty("SHA1Hash")
    public void setSha1OfAssetCert(byte[] sha1OfAssetCert) {
        this.sha1OfAssetCert = sha1OfAssetCert;
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
