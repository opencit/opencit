/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.user.management.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

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
    private UserLoginCertificate userLoginCertificate;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserLoginCertificate getUserLoginCertificate() {
        return userLoginCertificate;
    }

    public void setUserLoginCertificate(UserLoginCertificate userLoginCertificate) {
        this.userLoginCertificate = userLoginCertificate;
    }

}
