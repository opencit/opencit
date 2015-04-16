/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.token;

import java.util.Date;

/**
 * The token value is used to lookup the user and permission information
 * associated with the token. The token date is typically the date the
 * request containing the token was received by the server (determined by
 * the server, NOT by the date in the request itself).
 * 
 * @author jbuhacoff
 */
public class Token {
    private String value;
    private Date date;

    public Token(String value) {
        this.value = value;
        this.date = new Date();
    }

    public Token(String value, Date date) {
        this.value = value;
        this.date = date;
    }
    
    public String getValue() {
        return value;
    }

    public Date getDate() {
        return date;
    }
    
}
