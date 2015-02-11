/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="binding_key_endorsement_request")
public class BindingKeyEndorsementRequest{
    
    Logger log = LoggerFactory.getLogger(getClass().getName());
    
    private byte[] publicKeyModulus;
    private byte[] tpmCertifyKey;

    public byte[] getPublicKeyModulus() {
        return publicKeyModulus;
    }

    public void setPublicKeyModulus(byte[] publicKeyModulus) {
        this.publicKeyModulus = publicKeyModulus;
    }

    public byte[] getTpmCertifyKey() {
        return tpmCertifyKey;
    }

    public void setTpmCertifyKey(byte[] tpmCertifyKey) {
        this.tpmCertifyKey = tpmCertifyKey;
    }    
}
