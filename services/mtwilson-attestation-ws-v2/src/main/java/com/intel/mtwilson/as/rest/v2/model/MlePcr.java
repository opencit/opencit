/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jersey.Document;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="mle_pcr")
public class MlePcr extends Document{
    
    private String mleUuid;
    private String pcrName;
    private String pcrDigest;
    private String description;

    public String getMleUuid() {
        return mleUuid;
    }

    public void setMleUuid(String mleUuid) {
        this.mleUuid = mleUuid;
    }

    public String getPcrName() {
        return pcrName;
    }

    public void setPcrName(String pcrName) {
        this.pcrName = pcrName;
    }

    public String getPcrDigest() {
        return pcrDigest;
    }

    public void setPcrDigest(String pcrDigest) {
        this.pcrDigest = pcrDigest;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    
    
}
