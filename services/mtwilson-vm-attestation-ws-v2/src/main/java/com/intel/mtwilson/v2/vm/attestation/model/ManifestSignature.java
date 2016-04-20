/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.vm.attestation.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.Document;

/**
 * Inputs: VM Image ID, Hash of manifest + measurements
 * Manifest signature:  Customer ID, {Customer ID, Image ID, Hash}MTW
 * 
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="manifest_signature")
public class ManifestSignature extends Document {
    private String vmImageId;
    private String manifestHash;
    private String customerId;
    private String signature; // this is the signature over the document
    private String document; // this is the vmblob that gets signed

    public String getVmImageId() {
        return vmImageId;
    }

    public void setVmImageId(String vmImageId) {
        this.vmImageId = vmImageId;
    }

    public String getManifestHash() {
        return manifestHash;
    }

    public void setManifestHash(String manifestHash) {
        this.manifestHash = manifestHash;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }
    
    
    
}
