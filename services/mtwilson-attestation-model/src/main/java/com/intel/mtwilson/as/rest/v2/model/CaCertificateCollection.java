/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.DocumentCollection;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="ca_certificate_collection")
public class CaCertificateCollection extends DocumentCollection<CaCertificate> {

    private final ArrayList<CaCertificate> caCerts = new ArrayList<>();
    
    @JsonSerialize(include=JsonSerialize.Inclusion.ALWAYS) // jackson 1.9
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="ca_certificates")
    @JacksonXmlProperty(localName="ca_certificate")    
    public List<CaCertificate> getCaCertificates() { return caCerts; }
    
    @Override
    public List<CaCertificate> getDocuments() {
        return getCaCertificates();
    }
    
}
