/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.Document;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="certificate_request")
public class CertificateRequest extends Document{
        
    private String subject; // from the query parameter   POST /certificate-requests?subject={my-hardware-uuid}   and request body is the selection xml or json 
    private String status;
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
