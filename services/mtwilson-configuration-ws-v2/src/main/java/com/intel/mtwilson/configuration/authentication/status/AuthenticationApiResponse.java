/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.configuration.authentication.status;

/**
 *
 * @author hmgowda
 */
public class AuthenticationApiResponse {
    
    boolean hostBasedAuthenticationBypassEnabled;

    public boolean isHostBasedAuthenticationBypassEnabled() {
        return hostBasedAuthenticationBypassEnabled;
    }

    public void setHostBasedAuthenticationBypassEnabled(boolean hostBasedAuthenticationBypassEnabled) {
        this.hostBasedAuthenticationBypassEnabled = hostBasedAuthenticationBypassEnabled;
    }

    
    
}
