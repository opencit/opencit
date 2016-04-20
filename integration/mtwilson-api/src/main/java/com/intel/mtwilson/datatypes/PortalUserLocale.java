/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 *
 * @author rksavinx
 */
public class PortalUserLocale {
    private String user = null;
    private String locale = null;

    public PortalUserLocale(){ }
 
    public PortalUserLocale(String _user, String _locale) {
        setUser(_user);
        setLocale(_locale);
    }
    
    
    @JsonGetter("User")
    public String getUser() {
        return this.user;
    }

    @JsonSetter("User")
    public void setUser(String value) {
        this.user = value;
    }
    
    @JsonGetter("Locale")
    public String getLocale() {
        return this.locale;
    }

    @JsonSetter("Locale")
    public void setLocale(String value) {
        this.locale = value;
    }
}
