/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.user.management.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jersey.Document;
import java.util.Locale;

/**
  id uuid NOT NULL,
  username character varying(255) NOT NULL,
  locale character varying(8) NOT NULL,
  enabled boolean NOT NULL DEFAULT '0',
  status varchar(128) NOT NULL DEFAULT 'Pending',
  comment text DEFAULT NULL,
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="register_user_with_certificate")
public class RegisterUserWithCertificate {

    private User user;
    private UserLoginCertificate userCertificate;
    private Boolean result;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserLoginCertificate getUserCertificate() {
        return userCertificate;
    }

    public void setUserCertificate(UserLoginCertificate userCertificate) {
        this.userCertificate = userCertificate;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }
    
    
}
