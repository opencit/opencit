/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.file;

import com.intel.dcsg.cpg.io.pem.Pem;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * The KeyEnvelope class models a key wrapped (encrypted) with another key.
 * 
 * Compatibility note: see KeyEnvelopeV1 for the compatibility class which
 * can read and write the earlier format (banner name and header names).
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public class KeyEnvelope implements PemKeyEncryption, PemIntegrity {
       public static final String PEM_BANNER = "ENCRYPTED KEY";
       public static final String CONTENT_KEY_ID_HEADER = "Content-Key-Id";
       public static final String CONTENT_ALGORITHM_HEADER = "Content-Algorithm";
       public static final String CONTENT_KEY_LENGTH_HEADER = "Content-Key-Length";
       public static final String CONTENT_MODE_HEADER = "Content-Mode";
       public static final String CONTENT_PADDING_MODE_HEADER = "Content-Padding-Mode";
       public static final String ENCRYPTION_KEY_ID_HEADER = "Encryption-Key-Id";
       public static final String ENCRYPTION_ALGORITHM_HEADER = "Encryption-Algorithm";
       public static final String ENCRYPTION_KEY_LENGTH_HEADER = "Encryption-Key-Length";
       public static final String ENCRYPTION_MODE_HEADER = "Encryption-Mode";
       public static final String ENCRYPTION_PADDING_MODE_HEADER = "Encryption-Padding-Mode";
       public static final String INTEGRITY_KEY_ID_HEADER = "Integrity-Key-Id";
       public static final String INTEGRITY_ALGORITHM_HEADER = "Integrity-Algorithm";
       public static final String INTEGRITY_KEY_LENGTH_HEADER = "Integrity-Key-Length";
       public static final String INTEGRITY_MANIFEST_HEADER = "Integrity-Manifest";

       private Pem document;

    public KeyEnvelope(Pem document) {
        this.document = document;
        
    }
       
    public KeyEnvelope(KeyEnvelopeV1 envelope) {
        this.document = new Pem(envelope.getDocument()); // makes a copy 
        /*
        // automatically upgrade v1 format to v2 format; if you don't want to auto-upgrade use the EncryptedKeyEnvelopeV1 class instead which will keep the v1 format
        if( document.getHeader(EncryptedKeyEnvelopeV1.CONTENT_ALGORITHM_HEADER) != null && document.getHeader(EncryptedKeyEnvelopeV1.ENVELOPE_KEY_ID_HEADER) != null && document.getHeader(EncryptedKeyEnvelopeV1.ENVELOPE_ALGORITHM_HEADER) != null ) {
            document.setHeader(CONTENT_ALGORITHM_HEADER, document.getHeader(EncryptedKeyEnvelopeV1.CONTENT_ALGORITHM_HEADER));
            document.setHeader(ENCRYPTION_KEY_ID_HEADER, document.getHeader(EncryptedKeyEnvelopeV1.ENVELOPE_KEY_ID_HEADER));
            document.setHeader(ENCRYPTION_ALGORITHM_HEADER, document.getHeader(EncryptedKeyEnvelopeV1.ENVELOPE_ALGORITHM_HEADER));
            document.removeHeader(EncryptedKeyEnvelopeV1.CONTENT_ALGORITHM_HEADER);
            document.removeHeader(EncryptedKeyEnvelopeV1.ENVELOPE_KEY_ID_HEADER);
            document.removeHeader(EncryptedKeyEnvelopeV1.ENVELOPE_ALGORITHM_HEADER);
        }
        */
    }
       
    public void setContentAlgorithm(String contentAlgorithm) { document.setHeader(CONTENT_ALGORITHM_HEADER, contentAlgorithm); }
    public void setEncryptionKeyId(String envelopeKeyId) { document.setHeader(ENCRYPTION_KEY_ID_HEADER, envelopeKeyId); }
    public void setEncryptionAlgorithm(String envelopeAlgorithm) { document.setHeader(ENCRYPTION_ALGORITHM_HEADER, envelopeAlgorithm); }
    public void setEncryptionMode(String envelopeMode) { document.setHeader(ENCRYPTION_MODE_HEADER, envelopeMode); }
    public void setEncryptionPaddingMode(String envelopePaddingMode) { document.setHeader(ENCRYPTION_PADDING_MODE_HEADER, envelopePaddingMode); }
    
    
    /**
     * the algorithm for which the encrypted key is intended; for example "AES"
     * @return 
     */
        @Override
    public String getContentAlgorithm() { return document.getHeader(CONTENT_ALGORITHM_HEADER); }
    
    /**
     * identifies the encryption key so recipient knows which key to use to unwrap; for example an email address or hex representation of MD5, SHA1, or SHA256 fingerprint of the public key
     * @return 
     */
        @Override
    public String getEncryptionKeyId() { return document.getHeader(ENCRYPTION_KEY_ID_HEADER); }
    
    /**
     * for example RSA/ECB/OAEPWithSHA-256AndMGF1Padding
     * @return 
     */
        @Override
    public String getEncryptionAlgorithm() { return document.getHeader(ENCRYPTION_ALGORITHM_HEADER); }
    
//    @Override
    public Integer getEncryptionKeyLength() {
        if( document.getHeader(ENCRYPTION_KEY_LENGTH_HEADER) == null ) { return null; }
        return Integer.valueOf(document.getHeader(ENCRYPTION_KEY_LENGTH_HEADER));
    }
        
        @Override
    public String getEncryptionMode() { return document.getHeader(ENCRYPTION_MODE_HEADER); }
        @Override
    public String getEncryptionPaddingMode() { return document.getHeader(ENCRYPTION_PADDING_MODE_HEADER); }
        
//    @Override
    public static boolean isCompatible(Pem pem) {
        return pem.getBanner().equals(PEM_BANNER) && pem.getHeaders().containsKey(CONTENT_ALGORITHM_HEADER) && pem.getHeaders().containsKey(ENCRYPTION_KEY_ID_HEADER) && pem.getHeaders().containsKey(ENCRYPTION_ALGORITHM_HEADER);
    }

    
    @Override
    public String getContentKeyId() {
        return document.getHeader(CONTENT_KEY_ID_HEADER);
    }

    @Override
    public Integer getContentKeyLength() {
        if( document.getHeader(CONTENT_KEY_LENGTH_HEADER) == null ) { return null; }
        return Integer.valueOf(document.getHeader(CONTENT_KEY_LENGTH_HEADER));
    }

    @Override
    public String getContentMode() {
        return document.getHeader(CONTENT_MODE_HEADER);
    }

    @Override
    public String getContentPaddingMode() {
        return document.getHeader(CONTENT_PADDING_MODE_HEADER);
    }

    @Override
    public boolean isEncrypted() {
        return getEncryptionAlgorithm() != null;
    }

    @Override
    public Pem getDocument() {
        return document;
    }

    @Override
    public boolean isIntegrated() {
        return getIntegrityAlgorithm() != null;
    }

    @Override
    public String getIntegrityKeyId() {
        return document.getHeader(INTEGRITY_KEY_ID_HEADER);
    }

    @Override
    public String getIntegrityAlgorithm() {
        return document.getHeader(INTEGRITY_ALGORITHM_HEADER);
    }

    @Override
    public Integer getIntegrityKeyLength() {
        if( document.getHeader(INTEGRITY_KEY_LENGTH_HEADER) == null ) { return null; }
        return Integer.valueOf(document.getHeader(INTEGRITY_KEY_LENGTH_HEADER));
    }
    
    @Override
    public List<String> getIntegrityManifest() {
        return Arrays.asList(StringUtils.split(document.getHeader(INTEGRITY_MANIFEST_HEADER), ", "));
    }
    
}
