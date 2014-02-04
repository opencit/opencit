/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelQuery;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelReport;
import com.intel.mtwilson.jersey.Document;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="poll_host")
public class PollHost extends Document{
    
    @JsonInclude(JsonInclude.Include.ALWAYS)                // jackson 2.0
    @JacksonXmlElementWrapper(localName="hosts")
    @JacksonXmlProperty(localName="host")            
    private String[] hosts;
    private OpenStackHostTrustLevelReport result;

    public String[] getHosts() {
        return hosts;
    }

    public void setHosts(String[] hosts) {
        this.hosts = hosts;
    }

    public OpenStackHostTrustLevelReport getResult() {
        return result;
    }

    public void setResult(OpenStackHostTrustLevelReport result) {
        this.result = result;
    }
    
    
    
}
