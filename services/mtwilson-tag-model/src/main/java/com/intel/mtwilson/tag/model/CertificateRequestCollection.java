/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import org.codehaus.jackson.map.annotate.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.DocumentCollection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="certificate_request_collection")
public class CertificateRequestCollection extends DocumentCollection<CertificateRequest>{

    private final ArrayList<CertificateRequest> certificateRequests = new ArrayList<CertificateRequest>();
    
    @JsonSerialize(include=JsonSerialize.Inclusion.ALWAYS) // jackson 1.9
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="certificate_requests")
    @JacksonXmlProperty(localName="certificate_request")    
    public List<CertificateRequest> getCertificateRequests() { return certificateRequests; }

    @Override
    public List<CertificateRequest> getDocuments() {
        return getCertificateRequests();
    }
    
}
