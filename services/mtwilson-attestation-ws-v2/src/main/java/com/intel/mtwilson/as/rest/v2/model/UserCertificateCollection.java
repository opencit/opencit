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
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="user_certificate_collection")
public class UserCertificateCollection extends DocumentCollection<UserCertificate> {

    private final ArrayList<UserCertificate> userCertificates = new ArrayList<UserCertificate>();
    
    @JsonSerialize(include=JsonSerialize.Inclusion.ALWAYS) // jackson 1.9
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="user_certificates")
    @JacksonXmlProperty(localName="user_certificate")    
    public List<UserCertificate> getUserCertificates() { return userCertificates; }
    
    @Override
    public List<UserCertificate> getDocuments() {
        return getUserCertificates();
    }
    
}
