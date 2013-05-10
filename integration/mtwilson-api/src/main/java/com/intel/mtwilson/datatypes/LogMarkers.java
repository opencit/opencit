/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

/**
 *
 * @author ssbangal
 */
public enum LogMarkers {
    
    WHITELIST_CONFIGURATION("WHITELIST_CONFIGURATION"), // For White list configuration/updates
    HOST_CONFIGURATION("HOST_CONFIGURATION"), // For Host configuration / updates
    HOST_ATTESTATION("HOST_ATTESTATION"), // for host attestation status
    USER_CONFIGURATION("USER_CONFIGURATION"); // For API Client configuration / updates
    
    private String value;
    
    private LogMarkers(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
}
