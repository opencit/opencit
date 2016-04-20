/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.user.management.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.Set;

/**
 * Encapsulates structured data in the comment field of a user record.
 * It intentionally does not extend Document because it is not an 
 * independent entity.
 * 
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="user_comment")
@JsonInclude(JsonInclude.Include.NON_EMPTY) // jackson 2.0
@JsonIgnoreProperties(ignoreUnknown=true)
public class UserComment {
    public Set<String> roles;
}
