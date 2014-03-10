/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import org.codehaus.jackson.map.annotate.JsonSerialize;
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
@JacksonXmlRootElement(localName="certificate_request_attribute_collection")
public class CertificateRequestAttributeCollection extends DocumentCollection<CertificateRequestAttribute>{

    private final ArrayList<CertificateRequestAttribute> certificateRequests = new ArrayList<CertificateRequestAttribute>();
    
    @JsonSerialize(include=JsonSerialize.Inclusion.ALWAYS) // jackson 1.9
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="certificate_request_attributes")
    @JacksonXmlProperty(localName="certificate_request_attribute")    
    public List<CertificateRequestAttribute> getCertificateRequestAttributes() { return certificateRequests; }

    @Override
    public List<CertificateRequestAttribute> getDocuments() {
        return getCertificateRequestAttributes();
    }
    
}
