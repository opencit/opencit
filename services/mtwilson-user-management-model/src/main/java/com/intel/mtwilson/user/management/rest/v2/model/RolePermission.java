/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.user.management.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.Document;
import com.intel.dcsg.cpg.validation.Regex;
import com.intel.dcsg.cpg.validation.RegexPatterns;

/**
  role_id uuid NOT NULL,
  permit_domain character varying(200) DEFAULT NULL,
  permit_action character varying(200) DEFAULT NULL,
  permit_selection character varying(200) DEFAULT NULL,
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="role_permission")
public class RolePermission extends Document {
    private UUID roleId;
    private String permitDomain;
    private String permitAction;
    private String permitSelection;

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    @Regex(RegexPatterns.ANY_VALUE)
    public String getPermitDomain() {
        return permitDomain;
    }

    public void setPermitDomain(String permitDomain) {
        this.permitDomain = permitDomain;
    }

    @Regex(RegexPatterns.ANY_VALUE)
    public String getPermitAction() {
        return permitAction;
    }

    public void setPermitAction(String permitAction) {
        this.permitAction = permitAction;
    }

    @Regex(RegexPatterns.ANY_VALUE)
    public String getPermitSelection() {
        return permitSelection;
    }

    public void setPermitSelection(String permitSelection) {
        this.permitSelection = permitSelection;
    }
    
    
}
