/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author dsmagadx
 */
public class ApiClientCreateRequest {
    private byte[] certificate;
    private String[] roles;
    
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
    

}
