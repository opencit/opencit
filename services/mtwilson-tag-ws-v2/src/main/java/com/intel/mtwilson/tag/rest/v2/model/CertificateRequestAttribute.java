/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jersey.Document;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="certificate_request_attribute")
public class CertificateRequestAttribute extends Document{
        
    private UUID certificateRequestId;
    private UUID attributeId;
    private UUID attributeValueId;

    public UUID getCertificateRequestId() {
        return certificateRequestId;
    }

    public void setCertificateRequestId(UUID certificateRequestId) {
        this.certificateRequestId = certificateRequestId;
    }

    public UUID getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(UUID attributeId) {
        this.attributeId = attributeId;
    }

    public UUID getAttributeValueId() {
        return attributeValueId;
    }

    public void setAttributeValueId(UUID attributeValueId) {
        this.attributeValueId = attributeValueId;
    }
    
}
