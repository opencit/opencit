/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.Document;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="mle_pcr")
public class MlePcr extends Document{
    
    private String mleUuid;
    private String pcrIndex;
    private String pcrValue;
    private String description;

    public String getMleUuid() {
        return mleUuid;
    }

    public void setMleUuid(String mleUuid) {
        this.mleUuid = mleUuid;
    }

    public String getPcrIndex() {
        return pcrIndex;
    }

    public void setPcrIndex(String pcrIndex) {
        this.pcrIndex = pcrIndex;
    }

    public String getPcrValue() {
        return pcrValue;
    }

    public void setPcrValue(String pcrValue) {
        this.pcrValue = pcrValue;
    }

    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    
    
}
