/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.file;

/**
 * The RsaKeyEnvelope class models an AES key that has been wrapped using an RSA public key. 
 * @since 0.1
 * @author jbuhacoff
 */
public class RsaKeyEnvelope extends KeyEnvelope {
    
    /**
     * XXX the output of this is not inter-operable with other software ; we may want to provide another serialization method that produces a standard format
     * @return A PEM-like format for serializing the key envelope
     */
    public String toPem() {
        return toPem("SECRET KEY");
    }
    
    /**
     * @param pem a PEM-like format for serializing the key envelope, generated with toPem() 
     * @return 
     */
    public static RsaKeyEnvelope fromPem(String input) {
        KeyEnvelope envelope = KeyEnvelope.fromPem(input, "SECRET KEY");
        RsaKeyEnvelope rsaKeyEnvelope = new RsaKeyEnvelope();
        for(String headerName : envelope.listHeaders()) {
            rsaKeyEnvelope.setHeader(headerName, envelope.getHeader(headerName));
        }
        rsaKeyEnvelope.setContent(envelope.getContent());
        rsaKeyEnvelope.setContentAlgorithm(envelope.getContentAlgorithm());
        rsaKeyEnvelope.setEnvelopeKeyId(envelope.getEnvelopeKeyId());
        rsaKeyEnvelope.setEnvelopeAlgorithm(envelope.getEnvelopeAlgorithm());
        if( !rsaKeyEnvelope.getEnvelopeAlgorithm().startsWith("RSA") ) { throw new IllegalArgumentException("Envelope algorithm is not RSA"); }
        return rsaKeyEnvelope;
    } 
    
}
