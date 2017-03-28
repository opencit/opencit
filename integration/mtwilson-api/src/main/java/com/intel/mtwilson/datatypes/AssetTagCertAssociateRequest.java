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
    
    /**
     * A tag is the 32-byte sha256 digest of the asset certificate
     */
    private byte[]  tag;
    private int     hostId;

    public AssetTagCertAssociateRequest() {
        this.tag = null;
        this.hostId = 0;
    }
            
    public AssetTagCertAssociateRequest(byte[] sha256OfAssetCert, int hostID) {
        this.tag = sha256OfAssetCert;
        this.hostId = hostID;
    }

       
    @JsonProperty("SHA256Hash")
    public byte[] getSha256OfAssetCert() {
        return tag;
    }

    @JsonProperty("SHA256Hash")
    public void setSha256OfAssetCert(byte[] sha256OfAssetCert) {
        this.tag = sha256OfAssetCert;
    }
    
    @JsonProperty("SHA1Hash")
    public byte[] getSha1OfAssetCert() {
        return tag;
    }

    @JsonProperty("SHA1Hash")
    public void setSha1OfAssetCert(byte[] sha1OfAssetCert) {
        this.tag = sha1OfAssetCert;
    }

    @JsonProperty("Host_ID")
    public int getHostID() {
        return hostId;
    }

    @JsonProperty("Host_ID")
    public void setHostID(int hostID) {
        this.hostId = hostID;
    }    
    
}
