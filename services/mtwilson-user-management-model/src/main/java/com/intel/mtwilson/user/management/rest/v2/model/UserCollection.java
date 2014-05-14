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
@JacksonXmlRootElement(localName="user_collection")
public class UserCollection extends DocumentCollection<User> {
    private final ArrayList<User> users = new ArrayList<User>();

    // using the xml annotations we get output like <users><user>...</user><user>...</user></users> , without them we would have <users><users>...</users><users>...</users></users> and it looks strange
    @JsonSerialize(include=JsonSerialize.Inclusion.ALWAYS) // jackson 1.9
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="users")
    @JacksonXmlProperty(localName="user")    
    public List<User> getUsers() { return users; }
    
    @Override
    public List<User> getDocuments() {
        return getUsers();
    }
    
}
