/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.vm.attestation.resource;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.mtwilson.v2.vm.attestation.model.ManifestSignatureInput;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.v2.vm.attestation.model.ManifestSignature;
import javax.ws.rs.POST;
//import javax.ejb.Stateless;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.intel.mtwilson.My;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
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
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,DataMediaType.APPLICATION_YAML,DataMediaType.TEXT_YAML})
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,DataMediaType.APPLICATION_YAML,DataMediaType.TEXT_YAML})
    public ManifestSignature signImageManifest(ManifestSignatureInput input) throws KeyManagementException, FileNotFoundException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, CertificateEncodingException, CryptographyException, InvalidKeyException, SignatureException {
        
        String vmblobXml = "<vm_manifest><customer_id>1234</customer_id><image_id>1235289</image_id><manifest_hash>aaaaaa</manifest_hash></vm_manifest>";
        
        SimpleKeystore keystore = new SimpleKeystore(My.configuration().getSamlKeystoreFile(), My.configuration().getSamlKeystorePassword());
        RsaCredentialX509 credential = keystore.getRsaCredentialX509("saml", My.configuration().getSamlKeystorePassword());
        byte[] signature = credential.signature(vmblobXml.getBytes(Charset.forName("UTF-8")));
        
        log.debug("retrieve version");
        ManifestSignature output = new ManifestSignature();
        output.setCustomerId("1234");
        output.setVmImageId("1235289");
        output.setManifestHash("aaaaaa");
        output.setDocument(vmblobXml);
        output.setSignature(Base64.encodeBase64String(signature));
        return output;
    }
    
}
