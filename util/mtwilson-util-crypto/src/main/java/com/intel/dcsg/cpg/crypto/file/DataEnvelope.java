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
 * The DataEnvelope class models arbitrary data encrypted with a key - it's 
 * implied that the key will be a symmetric key.  If you need to then send
 * the symmetric key to a recipient securely, use RsaKeyEnvelope to wrap it
 * with an RSA key. Or if you want to password-protect the file, you can 
 * use a PasswordKeyEnvelope to protect the AES key.
 * 
 * The EnvelopeAlgorithm is the password-based key-derivation algorithm, together
 * with the cipher mode for the derived key and the padding. For example,
 * PBEWithMD5AndDES/CBC/PKCS5Padding.
 * 
 * 
 * The EnvelopeKeyId is a salted hash of the password that was used to 
 * generate the key. It's used in order to detect whether the password
 * is correct or not w/o needing to know anything about the envelope
 * contents. Upon a successful decryption, implementations may re-salt
 * the hash when saving the file again - and possibly with a different
 * password!
 * 
 * <pre>
-----BEGIN ENCRYPTED DATA-----
EnvelopeKeyId: bkaz4lbv/y0=:etpZPPlmwEilwbNxzBHRCd+FyWQMw1akygtgIPent6w=
EnvelopeAlgorithm: PBEWithMD5AndDES/CBC/PKCS5Padding
   
LjKQ2r1Yt9foxbHdLKZeClqZuzN7PoEmy+b+dKq9qibaH4pRcwATuWt4/Jzl6y85
NHM6CM4bOV1MHkyD01tFsT4kJ0GwRPg4tKAiTNjE4Yrz9V3rESiQKridtXMOToEp
Mj2nSvVKRSNEeG33GNIYUeMfSSc3oTmZVOlHNp9f8LEYWNmIjfzlHExvgJaPrixX
QiPGJ6K05kV5FJWRPET9vI+kyouAm6DBcyAhmR80NYRvaBbXGM/MxBgQ7koFVaI5
zoJ/NBdEIMdHNUh0h11GQCXAQXOSL6Fx2hRdcicm6j1CPd3AFrTt9EATmd4Hj+D4
91jDYXElALfdSbiO0A9Mz6USUepTXwlfVV/cbBpLRz5Rqnyg2EwI2tZRU+E+Cusb
/b6hcuWyzva895YMUCSyDaLgSsIqRWmXxQV1W2bAgRbs8jD8VF+G9w==
-----END ENCRYPTED DATA-----
 * </pre>
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public class DataEnvelope {
    private byte[] content;                 // the encrypted data
    private final String ENVELOPE_KEY_ID_HEADER = "EnvelopeKeyId";           // arbitrary key id so recipient knows which key to use to unwrap; for example an email address or hex representation of MD5, SHA1, or SHA256 fingerprint of the public key
    private final String ENVELOPE_ALGORITHM_HEADER = "EnvelopeAlgorithm";       // defined by RsaKeyEnvelopeFactory; for example RSA/ECB/OAEPWithSHA-256AndMGF1Padding
    private HashMap<String,String> headers = new HashMap<String,String>(); // optional;  users can set other headers to send along with the envelope (for writing additional information on it, such as the identifier of the enveloped key, which we don't care about here)
    
    public void setContent(byte[] content) { this.content = content; }
    public void setEnvelopeKeyId(String envelopeKeyId) { this.headers.put(ENVELOPE_KEY_ID_HEADER, envelopeKeyId); }
    public void setEnvelopeAlgorithm(String envelopeAlgorithm) { this.headers.put(ENVELOPE_ALGORITHM_HEADER, envelopeAlgorithm); }
    public void setHeader(String name, String value) { this.headers.put(name, value); } // optional
    public void removeHeader(String name) { this.headers.remove(name); } // optional
    
    public byte[] getContent() { return content; }
    public String getEnvelopeKeyId() { return this.headers.get(ENVELOPE_KEY_ID_HEADER); }
    public String getEnvelopeAlgorithm() { return this.headers.get(ENVELOPE_ALGORITHM_HEADER); }
    public String getHeader(String name) { return this.headers.get(name); } // optional
    public Set<String> listHeaders() { return Collections.unmodifiableSet(this.headers.keySet()); } // optional
    
    protected String getPemContentType() { return "ENCRYPTED DATA"; } // goes next to BEGIN and END tags in PEM format
    
    public String toPem() {
        Pem pem = new Pem(getPemContentType(), getContent(), headers);
        return pem.toString();        
    }
    
    public static DataEnvelope fromPem(String input) {
        Pem pem = Pem.valueOf(input); // throws IllegalArgumentException if the input is not in PEM format (has a matching begin/end pair like -----BEGIN DATA----- and -----END DATA-----)
        // XXX not checking the PEM content type (what is next to BEGIN and END tags) 
        DataEnvelope envelope = new DataEnvelope();
        for(String header : pem.getHeaders().keySet()) {
            envelope.setHeader(header, pem.getHeaders().get(header));
        }
        envelope.setContent(pem.getContent());
        return envelope;        
    }
    
}
