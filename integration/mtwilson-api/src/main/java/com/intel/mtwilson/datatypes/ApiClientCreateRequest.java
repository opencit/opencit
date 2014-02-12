/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author dsmagadx
 */
public class ApiClientCreateRequest {
    private byte[] certificate;
    private String[] roles;
    private byte[] keyStore; // We are using this as a workaround for creating the user first and then the api client x509 entry 
    
    @JsonProperty("X509Certificate")
    public byte[] getCertificate() {
        return certificate;
    }
    @JsonProperty("X509Certificate")
    public void setCertificate(byte[] credential) {
        this.certificate = credential;
    }

    @JsonProperty("Roles")
    public String[] getRoles() {
        return roles;
    }
    
    @JsonProperty("Roles")
    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    @JsonProperty("Keystore")
    public byte[] getKeyStore() {
        return keyStore;
    }

    @JsonProperty("Keystore")
    public void setKeyStore(byte[] keyStore) {
        this.keyStore = keyStore;
    }

    

}
