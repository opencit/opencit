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
     * A tag is the 20-byte sha1 digest of the asset certificate
     */
    private byte[]  tag;
    private int     hostId;

    public AssetTagCertAssociateRequest() {
        this.tag = null;
        this.hostId = 0;
    }
            
    public AssetTagCertAssociateRequest(byte[] sha1OfAssetCert, int hostID) {
        this.tag = sha1OfAssetCert;
        this.hostId = hostID;
    }

    // TODO:  should be "tag"
    @JsonProperty("SHA1Hash")
    public byte[] getSha1OfAssetCert() {
        return tag;
    }

    // TODO:  should be "tag"
    @JsonProperty("SHA1Hash")
    public void setSha1OfAssetCert(byte[] sha1OfAssetCert) {
        this.tag = sha1OfAssetCert;
    }

    // TODO: should be "host_id" ; or allow the mapping framework to do it based on the default rules which would result in host_id
    @JsonProperty("Host_ID")
    public int getHostID() {
        return hostId;
    }

    // TODO: should be "host_id" ; or allow the mapping framework to do it based on the default rules which would result in host_id
    @JsonProperty("Host_ID")
    public void setHostID(int hostID) {
        this.hostId = hostID;
    }    
    
}
