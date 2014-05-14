/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.vm.attestation.resource;

import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.mtwilson.v2.vm.attestation.model.ManifestSignatureInput;
import com.intel.mtwilson.jaxrs2.OtherMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.v2.vm.attestation.model.ManifestSignature;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
//import javax.ejb.Stateless;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import com.intel.mtwilson.My;
import java.io.File;
import java.io.IOException;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import javax.ws.rs.Consumes;
import org.apache.commons.codec.binary.Base64;

/**
 * POST /manifest-signature
 * Content-Type: application/xml
 * Request body:
 * <manifest_signature_input>
 *  <vm_image_id>123456</vm_image_id>
 *  <manifest_hash>abcdef01234567890</manifest_hash>
 * </manifest_signature_input>
 * 
 * Response body:
 * <manifest_signature>
 *  <vm_image_id>123456</vm_image_id>
 *  <manifest_hash>abcdef01234567890</manifest_hash>
 *  <customer_id>982734</customer_id>
 *  <signature>abcdef01234567890abcdef01234567890</signature>
 * </manifest_signature>
 * 
 * @author jbuhacoff
 */
@V2
//@Stateless
@Path("/manifest-signature")
public class ManifestSignatureRpc {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ManifestSignatureRpc.class);
    
    @POST
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,OtherMediaType.APPLICATION_YAML,OtherMediaType.TEXT_YAML})
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,OtherMediaType.APPLICATION_YAML,OtherMediaType.TEXT_YAML})
    public ManifestSignature signImageManifest(ManifestSignatureInput input) throws Exception {
        
        // TODO: validate the input
        // TODO: create the vmblob xml
        String vmblobXml = "<vm_manifest><customer_id>1234</customer_id><image_id>1235289</image_id><manifest_hash>aaaaaa</manifest_hash></vm_manifest>";
        
        // TODO:  load the private key for signing  -  please check with jonathan later for better convenience function in SImpleKeystore to get private key out
        SimpleKeystore keystore = new SimpleKeystore(My.configuration().getSamlKeystoreFile(), My.configuration().getSamlKeystorePassword());
        RsaCredentialX509 credential = keystore.getRsaCredentialX509("saml", My.configuration().getSamlKeystorePassword());
        byte[] signature = credential.signature(vmblobXml.getBytes(Charset.forName("UTF-8")));
        
        log.debug("retrieve version");
        ManifestSignature output = new ManifestSignature();
        // TODO sign the input ,  put signature in output.setSignature(...)
        output.setCustomerId("1234");
        output.setVmImageId("1235289");
        output.setManifestHash("aaaaaa");
        output.setDocument(vmblobXml);
        output.setSignature(Base64.encodeBase64String(signature));
        return output;
    }
    
}
