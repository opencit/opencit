/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.file;

import com.intel.dcsg.cpg.io.pem.Pem;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * The KeyEnvelope class models a key wrapped (encrypted) with another key.
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public class KeyEnvelope {
    private byte[] content;                 // the encrypted AES key
    private String contentAlgorithm;        // the algorithm for which the encrypted key is intended; for example "AES"
    private String envelopeKeyId;           // arbitrary key id so recipient knows which key to use to unwrap; for example an email address or hex representation of MD5, SHA1, or SHA256 fingerprint of the public key
    private String envelopeAlgorithm;       // defined by RsaKeyEnvelopeFactory; for example RSA/ECB/OAEPWithSHA-256AndMGF1Padding
    private HashMap<String,String> otherHeaders = new HashMap<String,String>(); // optional;  users can set other headers to send along with the envelope (for writing additional information on it, such as the identifier of the enveloped key, which we don't care about here)
    
    public void setContent(byte[] content) { this.content = content; }
    public void setContentAlgorithm(String contentAlgorithm) { this.contentAlgorithm = contentAlgorithm; this.otherHeaders.put("ContentAlgorithm", contentAlgorithm); }
    public void setEnvelopeKeyId(String envelopeKeyId) { this.envelopeKeyId = envelopeKeyId;  this.otherHeaders.put("EnvelopeKeyId", envelopeKeyId); }
    public void setEnvelopeAlgorithm(String envelopeAlgorithm) { this.envelopeAlgorithm = envelopeAlgorithm; this.otherHeaders.put("EnvelopeAlgorithm", envelopeAlgorithm); }
    public void setHeader(String name, String value) { this.otherHeaders.put(name, value); } // optional
    public void removeHeader(String name) { this.otherHeaders.remove(name); } // optional
    
    public byte[] getContent() { return content; }
    public String getContentAlgorithm() { return contentAlgorithm; }
    public String getEnvelopeKeyId() { return envelopeKeyId; }
    public String getEnvelopeAlgorithm() { return envelopeAlgorithm; }
    public String getHeader(String name) { return this.otherHeaders.get(name); } // optional
    public Set<String> listHeaders() { return Collections.unmodifiableSet(this.otherHeaders.keySet()); } // optional
    
    /*
     * This is an example of a PEM format file of an RSA private key.  Notice it has http/smtp-like headers, an empty line, and then base64-encoded-chunked content.
-----BEGIN RSA PRIVATE KEY-----
Proc-Type: 4,ENCRYPTED
DEK-Info: DES-EDE3-CBC,F2D4E6438DBD4EA8
   
LjKQ2r1Yt9foxbHdLKZeClqZuzN7PoEmy+b+dKq9qibaH4pRcwATuWt4/Jzl6y85
NHM6CM4bOV1MHkyD01tFsT4kJ0GwRPg4tKAiTNjE4Yrz9V3rESiQKridtXMOToEp
Mj2nSvVKRSNEeG33GNIYUeMfSSc3oTmZVOlHNp9f8LEYWNmIjfzlHExvgJaPrixX
QiPGJ6K05kV5FJWRPET9vI+kyouAm6DBcyAhmR80NYRvaBbXGM/MxBgQ7koFVaI5
zoJ/NBdEIMdHNUh0h11GQCXAQXOSL6Fx2hRdcicm6j1CPd3AFrTt9EATmd4Hj+D4
91jDYXElALfdSbiO0A9Mz6USUepTXwlfVV/cbBpLRz5Rqnyg2EwI2tZRU+E+Cusb
/b6hcuWyzva895YMUCSyDaLgSsIqRWmXxQV1W2bAgRbs8jD8VF+G9w=  =
-----END RSA PRIVATE KEY-----
     * 
     * See also: http://etutorials.org/Programming/secure+programming/Chapter+7.+Public+Key+Cryptography/7.17+Representing+Keys+and+Certificates+in+Plaintext+PEM+Encoding/
     * 
     */
    
    /**
     * Generates a PEM-like format that represents this envelope instance.
     * The three built-in headers EnvelopeKeyId, EnvelopeAlgorithm, and ContentAlgorithm override
     * any headers of the same name that may have been set as "Other Headers".
     * 
     * XXX the output of this is not inter-operable with other software ; we may want to provide another serialization method that produces a standard format
     * @param pemContentType is the text that goes in both the BEGIN and END tags of the PEM format, for example "SECRET KEY" in "-----BEGIN SECRET KEY-----"
     * @return A PEM-like format for serializing the key envelope
     */
    protected String toPem(String pemContentType) {
        HashMap<String,String> headers = new HashMap<>(this.otherHeaders); // start with the optional "other headers" set and override using built-in values
        headers.put("EnvelopeKeyId", getEnvelopeKeyId()); // salted hash
        headers.put("EnvelopeAlgorithm", getEnvelopeAlgorithm()); // whatever algorithm was used for password-based-encryption
        headers.put("ContentAlgorithm", getContentAlgorithm()); // for example "AES" if what we've encrypted is an AES key
        Pem pem = new Pem(pemContentType, getContent(), headers);
        return pem.toString();
    }
    
    /**
     * @param pem a PEM-like format for serializing the key envelope, generated with toPem() 
     * @param pemContentType is the text that goes in both the BEGIN and END tags of the PEM format, for example "SECRET KEY" in "-----BEGIN SECRET KEY-----"
     * @return 
     */
    protected static KeyEnvelope fromPem(String input, String pemContentType) {
        Pem pem = Pem.valueOf(input);
        if( !pemContentType.equals(pem.getContentType()) ) {
            throw new IllegalArgumentException("Content type of input PEM must be '"+pemContentType+"'");
        }
        KeyEnvelope envelope = new KeyEnvelope();
        for(String header : pem.getHeaders().keySet()) {
            envelope.setHeader(header, pem.getHeaders().get(header));
        }
        // ensure the built-in values are set last, to override any "Other header" values
        envelope.setContent(pem.getContent());
        envelope.setContentAlgorithm(pem.getHeaders().get("ContentAlgorithm"));
        envelope.setEnvelopeKeyId(pem.getHeaders().get("EnvelopeKeyId"));
        envelope.setEnvelopeAlgorithm(pem.getHeaders().get("EnvelopeAlgorithm"));
        return envelope;
    } 
    
}
