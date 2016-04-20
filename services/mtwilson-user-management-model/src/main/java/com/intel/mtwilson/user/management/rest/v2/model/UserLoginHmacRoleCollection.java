/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.model;

import com.intel.mtwilson.jaxrs2.DocumentCollection;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import org.codehaus.jackson.map.annotate.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="user_login_hmac_role_collection")
public class UserLoginHmacRoleCollection extends DocumentCollection<UserLoginHmacRole> {
    private final ArrayList<UserLoginHmacRole> userLoginHmacRoles = new ArrayList<UserLoginHmacRole>();

    // using the xml annotations we get output like <users><user>...</user><user>...</user></users> , without them we would have <users><users>...</users><users>...</users></users> and it looks strange
    @JsonSerialize(include=JsonSerialize.Inclusion.ALWAYS) // jackson 1.9
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="user_login_hmac_roles")
    @JacksonXmlProperty(localName="user_login_hmac_role")    
    public List<UserLoginHmacRole> getUserLoginHmacRoles() { return userLoginHmacRoles; }
    
    @Override
    public List<UserLoginHmacRole> getDocuments() {
        return getUserLoginHmacRoles();
    }
    
}
