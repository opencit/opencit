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
@JacksonXmlRootElement(localName="host_tls_certificate_collection")
public class HostTlsCertificateCollection extends DocumentCollection<HostTlsCertificate> {
    private final ArrayList<HostTlsCertificate> tlsCerts = new ArrayList<HostTlsCertificate>();
    
    @JsonSerialize(include=JsonSerialize.Inclusion.ALWAYS) // jackson 1.9
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="host_tls_certificates")
    @JacksonXmlProperty(localName="host_tls_certificate")    
    public List<HostTlsCertificate> getTlsCertificates() { return tlsCerts; }
    
    @Override
    public List<HostTlsCertificate> getDocuments() {
        return getTlsCertificates();
    }
    
}
