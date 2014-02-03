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
@JacksonXmlRootElement(localName="tls_policy_collection")
public class TlsPolicyCollection extends DocumentCollection<TlsPolicy> {
    private final ArrayList<TlsPolicy> tlsPolicies = new ArrayList<TlsPolicy>();
    
    @JsonSerialize(include=JsonSerialize.Inclusion.ALWAYS) // jackson 1.9
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="tls_policies")
    @JacksonXmlProperty(localName="tls_policy")    
    public List<TlsPolicy> getTlsPolicies() { return tlsPolicies; }

    @Override
    public List<TlsPolicy> getDocuments() {
        return getTlsPolicies();
    }
    
}
