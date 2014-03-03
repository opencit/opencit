/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intel.mtwilson.atag.model;

/**
 *
 * @author stdalex
 */
public class TpmPassword {
    private long id;
    private String uuid;
    private String password;
    
    public TpmPassword(){}
    
    public TpmPassword(long id, String uuid, String password) {
        this.id = id;
        this.uuid = uuid;
        this.password = password;
    }
    
    public long getId() {
        return this.id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getUuid() {
        return this.uuid;
    }
    
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public String getPassword() {
        return this.password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
