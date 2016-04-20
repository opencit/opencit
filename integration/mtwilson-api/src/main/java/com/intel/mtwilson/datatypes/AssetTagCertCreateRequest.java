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
public class AssetTagCertCreateRequest {

    private byte[] certificate;

    @JsonProperty("X509Certificate")
    public byte[] getCertificate() {
        return certificate;
    }

    @JsonProperty("X509Certificate")
    public void setCertificate(byte[] credential) {
        this.certificate = credential;
    }
}
