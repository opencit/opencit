/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.rpc.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.mtwilson.jaxrs2.Document;
import com.intel.dcsg.cpg.performance.Progress;

/**
 * A remote procedure call has an id, href, name of the procedure, input, 
 * status, progress, output, faults, and links.
 * 
 * This document captures the input and the output of the RPC, and is 
 * treated similarly to other resources. 
 * 
 * If the input or the output are large, or are in a format that is not
 * convenient to serialize in JSON, XML, or YAML,
 * they may be provided as links to files instead of inline. It would be like
 * this:
 * links.input = http://server.com/files/1/content 
 * links.output = http://server.com/files/2/content 
 * Unlike the JSONAPI, the linked documents would NOT appear in the "linked"
 * section because of their size or format. To maintain compatibility, we might
 * put the input/output links in meta.links = [ {rel:input,href:...}, {rel:output,href:...} ]
 * instead
 * of the normal document links section.
 * 
 * The faults will only be populated if meta.error is set to true. Otherwise
 * it will be either null or an empty list (TBD).
 * The status and progress are attributes in the meta section.
 * The meta.status value will be one of 
 * "QUEUE" (indicates the task is in the queue waiting for processing), 
 * "PROGRESS" (indicates processing has started and to check 
 * meta.progress for progress information), 
 * "OUTPUT" (indicates its done and output is available), 
 * "ERROR" (indicates an error and faults may be available).
 * 
 * 
 * Summary of information available in the meta section:
 * status - QUEUE|PROGRESS|OUTPUT|ERROR
 * progress - an object with "completed" and "total" (TBD)
 * createdOn (TBD - this one might be available on all documents)
 * updatedOn (TBD - whether for an error, progress, or final output, and might be available on all documents)
 * authorizationToken - an encrypted token the server creates which contains the user id who submitted the request and a reference to the user's validated authorization credential (password, digest, or rsa authorization used)  and an HMAC over the RPC name and input, to detect any tampering between when the request was submitted/authorized by the user and when it is executed by the server with the user's permissions (associated to user's id and credential used -  that means you can't send an RPC with HTTP BASIC authorization for something that would require X509 non-repudiation authorization)  in addition to the normal nonce and timestamp that are included in the authorization tokens. 
 *      the authorizationToken is normally not included in the meta section,
 *      only someone with administrative/audit permissions may view the token.
 * 
 * 
 * The web service would look like POST /rpc/{name}   (request body -> response body)
 * Client can send additional headers with the request.
 * 
 * 
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="rpc")
public class Rpc extends Document implements Progress {
    private String name;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JacksonXmlElementWrapper(localName="faults")
    @JacksonXmlProperty(localName="fault")        
    private Fault[] faults;
    private Status status;
    private Long current;
    private Long max;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Fault[] getFaults() {
        return faults;
    }

    public void setFaults(Fault[] faults) {
        this.faults = faults;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public static enum Status {
        QUEUE, PROGRESS, OUTPUT, ERROR;
    }
    
    @Override
    public Long getCurrent() {
        return current;
    }

    @Override
    public Long getMax() {
        return max;
    }

    public void setCurrent(Long current) {
        this.current = current;
    }

    public void setMax(Long max) {
        this.max = max;
    }
        
    public void copyFrom(Rpc rpc) {
        setId(rpc.getId());
        setName(rpc.getName());
        setFaults(rpc.getFaults());
        setStatus(rpc.getStatus());
        setCurrent(rpc.getCurrent());
        setMax(rpc.getMax());
    }
    
}
