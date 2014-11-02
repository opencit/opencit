/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The PasswordKeyEnvelope class models an AES key that has been wrapped using a password-based encryption.
 * 
 * The envelope contains the hash of the password used so that recipients can verify they have the correct password
 * before attempting to decrypt the key with it -- otherwise they would just get junk when trying to use it to 
 * decrypt something.  
 * 
 * 
 * 
 * 
 * The EnvelopeAlgorithm is expected to be something like  PBEWithHmacSHA1AndDESede  (found in pkcs5 1.0)
 * 
 * See also:  
 * http://www.openssl.org/docs/apps/cms.html
 * http://etutorials.org/Programming/secure+programming/Chapter+7.+Public+Key+Cryptography/7.17+Representing+Keys+and+Certificates+in+Plaintext+PEM+Encoding/
 * http://docs.oracle.com/javase/6/docs/technotes/guides/security/StandardNames.html 
 * 
 * XXX TODO look into support for PBKDF2WithHmacSHA1 ... according to Java docs (see Standard Names ref above) it's only available as a parameter to SecretKeyFactory 
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public class PasswordKeyEnvelope extends KeyEnvelope {
    private static final Logger log = LoggerFactory.getLogger(PasswordKeyEnvelope.class);
    
    
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
    public static PasswordKeyEnvelope fromPem(String input) {
        KeyEnvelope envelope = KeyEnvelope.fromPem(input, "SECRET KEY");
        PasswordKeyEnvelope passwordKeyEnvelope = new PasswordKeyEnvelope();
        for(String headerName : envelope.listHeaders()) {
            passwordKeyEnvelope.setHeader(headerName, envelope.getHeader(headerName));
        }
        passwordKeyEnvelope.setContent(envelope.getContent());
        passwordKeyEnvelope.setContentAlgorithm(envelope.getContentAlgorithm());
        passwordKeyEnvelope.setEnvelopeKeyId(envelope.getEnvelopeKeyId());
        passwordKeyEnvelope.setEnvelopeAlgorithm(envelope.getEnvelopeAlgorithm());
        if( !passwordKeyEnvelope.getEnvelopeAlgorithm().startsWith("PBEWith")
                && !passwordKeyEnvelope.getEnvelopeAlgorithm().startsWith("PBKDF2With") ) { throw new IllegalArgumentException("Envelope algorithm is not password-based encryption"); }
        return passwordKeyEnvelope;
    } 
}
