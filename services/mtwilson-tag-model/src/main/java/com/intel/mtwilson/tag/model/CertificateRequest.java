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
        
    private String subject; // from the query parameter   POST /certificate-requests?subject={my-hardware-uuid}   and request body is the selection xml or json 
    private String status;
    private String authorityName; // TODO: remove,  instead of associating old requests to current certs, just mark requests complete.  certs can be searched on the subject to correlate.
    private UUID certificateId; // TODO: remove,  instead of associating old requests to current certs, just mark requests complete. certs can be searched on the subject to correlate.
    private UUID selectionId; // TODO: remove, and instead of having a field here we should generate the selection xml and store it.  <selections><selection id="..."/></selections>  so the approver can have a consistent input.
    private String selectionName; //remove, and instead of having a field here we should generate the selection xml and store it.  <selections><selection name="..."/></selections>  so the approver can have a consistent input.
    private byte[] content;// the selections xml format;  may be encrypted
    private String contentType; //   application/xml for plain xml,  message/rfc822 for the encrypted xml with headers,  application/json  for the json request from the UI
    
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

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    
}
