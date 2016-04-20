/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.DocumentCollection;
import java.util.ArrayList;
import java.util.List;
//import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="host_aik_collection")
public class HostAikCollection extends DocumentCollection<HostAik> {
    private final ArrayList<HostAik> aiks = new ArrayList<>();
    
    @JsonSerialize(include=JsonSerialize.Inclusion.ALWAYS) // jackson 1.9
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="host_aiks")
    @JacksonXmlProperty(localName="host_aik")    
    public List<HostAik> getAiks() { return aiks; }
    
    @Override
    public List<HostAik> getDocuments() {
        return getAiks();
    }
    
}
