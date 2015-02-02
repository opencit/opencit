/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.vm.attestation.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.Document;
import com.intel.dcsg.cpg.validation.Regex;
import com.intel.dcsg.cpg.validation.RegexPatterns;

/**
 * Inputs: VM Image ID, Hash of manifest + measurements
 * 
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="manifest_signature_input")
public class ManifestSignatureInput extends Document {
    
    public static final String BASE64 = "(?:([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==))";
    public static final String UUID = "(?:[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12})";
    @Regex(UUID)    
    public String vmImageId;
    @Regex(BASE64)    
    public String manifestHash;

    
    @Regex(UUID)    
    public String getVmImageId() {
        return vmImageId;
    }

    public void setVmImageId(String vmImageId) {
        this.vmImageId = vmImageId;
    }

    @Regex(BASE64)    
    public String getManifestHash() {
        return manifestHash;
    }

    public void setManifestHash(String manifestHash) {
        this.manifestHash = manifestHash;
    }
    
    
}
