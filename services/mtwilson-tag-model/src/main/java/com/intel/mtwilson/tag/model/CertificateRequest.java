/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jersey.Document;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="certificate_request")
public class CertificateRequest extends Document{
        
    private String subject;
    private String status;
    private String authorityName;
    private UUID certificateId;
    private UUID selectionId;
    private String selectionName; // tags to include in the certificate

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAuthorityName() {
        return authorityName;
    }

    public void setAuthorityName(String authorityName) {
        this.authorityName = authorityName;
    }

    public UUID getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(UUID certificateId) {
        this.certificateId = certificateId;
    }

    public UUID getSelectionId() {
        return selectionId;
    }

    public void setSelectionId(UUID selectionId) {
        this.selectionId = selectionId;
    }

    
    public String getSelectionName() {
        return selectionName;
    }

    public void setSelectionName(String selectionName) {
        this.selectionName = selectionName;
    }
    
}
