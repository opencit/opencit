/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.vm.attestation.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.Document;

/**
 * Inputs: VM Image ID, Hash of manifest + measurements
 * 
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="manifest_signature_input")
public class ManifestSignatureInput extends Document {
    private String vmImageId;
    private String manifestHash;

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
    
    
}
