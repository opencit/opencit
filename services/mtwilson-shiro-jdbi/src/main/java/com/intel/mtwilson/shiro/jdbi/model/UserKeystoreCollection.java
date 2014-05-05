/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.shiro.jdbi.model;

import com.intel.mtwilson.jersey.DocumentCollection;
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
@JacksonXmlRootElement(localName="user_keystore_collection")
public class UserKeystoreCollection extends DocumentCollection<UserKeystore> {
    private final ArrayList<UserKeystore> userKeystores = new ArrayList<UserKeystore>();

    // using the xml annotations we get output like <users><user>...</user><user>...</user></users> , without them we would have <users><users>...</users><users>...</users></users> and it looks strange
    @JsonSerialize(include=JsonSerialize.Inclusion.ALWAYS) // jackson 1.9
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="user_keystores")
    @JacksonXmlProperty(localName="user_keystore")    
    public List<UserKeystore> getUserKeystores() { return userKeystores; }
    
    @Override
    public List<UserKeystore> getDocuments() {
        return getUserKeystores();
    }
    
}
