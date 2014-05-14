/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.Document;
import javax.ws.rs.PathParam;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="mle_source")
public class MleSource extends Document{
    
    private String name;
    private String mleUuid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMleUuid() {
        return mleUuid;
    }

    public void setMleUuid(String mleUuid) {
        this.mleUuid = mleUuid;
    }
    
    
}
