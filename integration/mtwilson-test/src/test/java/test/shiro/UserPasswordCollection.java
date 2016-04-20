/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.shiro;

import com.intel.mtwilson.jaxrs2.DocumentCollection;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * 
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="document")
public class UserPasswordCollection extends DocumentCollection<UserPassword> {
    private final ArrayList<UserPassword> userPasswords = new ArrayList<UserPassword>();
    
    // using the xml annotations we get output like <hosts><host>...</host><host>...</host></hosts> , without them we would have <hosts><hosts>...</hosts><hosts>...</hosts></hosts> and it looks strange
    @JsonSerialize(include=JsonSerialize.Inclusion.ALWAYS) // jackson 1.9
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="user-passwords")
    @JacksonXmlProperty(localName="user-password")    
    public List<UserPassword> getUserPasswords() { return userPasswords; }

    @Override
    public List<UserPassword> getDocuments() {
        return getUserPasswords();
    }
    
}
