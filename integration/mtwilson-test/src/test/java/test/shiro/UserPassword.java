/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.shiro;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.Document;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 * 
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="user-password")
public class UserPassword extends Document {
    private String name;
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    
}
