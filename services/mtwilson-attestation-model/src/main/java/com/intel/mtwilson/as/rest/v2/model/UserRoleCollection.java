/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jersey.DocumentCollection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="user_role_collection")
public class UserRoleCollection extends DocumentCollection<UserRole> {
    private final ArrayList<UserRole> userRoles = new ArrayList<UserRole>();

    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="user_roles")
    @JacksonXmlProperty(localName="user_role")    
    public List<UserRole> getUserRoles() { return userRoles; }
    
    @Override
    public List<UserRole> getDocuments() {
        return getUserRoles();
    }
    
}
