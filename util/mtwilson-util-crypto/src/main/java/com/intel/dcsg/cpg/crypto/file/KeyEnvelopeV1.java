/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.file;

import com.intel.dcsg.cpg.io.pem.Pem;

/**
 * Reads and writes the original KeyEnvelope (mtwilson-util-crypto 0.2) format
 * banner and headers.
 * 
 * @author jbuhacoff
 */
public class KeyEnvelopeV1 implements PemKeyEncryption {
    public static final String PEM_BANNER = "SECRET KEY";
    public static final String CONTENT_KEY_ID_HEADER = "ContentKeyId";
    public static final String CONTENT_ALGORITHM_HEADER = "ContentAlgorithm";
    public static final String CONTENT_KEY_LENGTH_HEADER = "ContentKeyLength";
    public static final String CONTENT_MODE_HEADER = "ContentMode";
    public static final String CONTENT_PADDING_MODE_HEADER = "ContentPaddingMode";
    public static final String ENVELOPE_KEY_ID_HEADER = "EnvelopeKeyId";
    public static final String ENVELOPE_ALGORITHM_HEADER = "EnvelopeAlgorithm";
    public static final String ENVELOPE_MODE_HEADER = "EnvelopeMode";
    public static final String ENVELOPE_PADDING_MODE_HEADER = "EnvelopePaddingMode";

    private Pem document;
    
    public KeyEnvelopeV1(Pem document) {
        this.document = document;
    }
    
    @Override
    public String getContentAlgorithm() {
        return document.getHeader(CONTENT_ALGORITHM_HEADER);
    }

//    @Override
    public void setContentAlgorithm(String contentAlgorithm) {
        document.setHeader(CONTENT_ALGORITHM_HEADER, contentAlgorithm);
    }

    @Override
    public String getEncryptionAlgorithm() {
        return document.getHeader(ENVELOPE_ALGORITHM_HEADER);
    }
    @Override
    public String getEncryptionMode() {
        return document.getHeader(ENVELOPE_MODE_HEADER);
    }
    @Override
    public String getEncryptionPaddingMode() {
        return document.getHeader(ENVELOPE_PADDING_MODE_HEADER);
    }

//    @Override
    public void setEncryptionAlgorithm(String envelopeAlgorithm) {
        document.setHeader(ENVELOPE_ALGORITHM_HEADER, envelopeAlgorithm);
    }

    @Override
    public String getEncryptionKeyId() {
        return document.getHeader(ENVELOPE_KEY_ID_HEADER);
    }

//    @Override
    public void setEncryptionKeyId(String envelopeKeyId) {
        document.setHeader(ENVELOPE_KEY_ID_HEADER, envelopeKeyId);
    }
    public void setEncryptionMode(String envelopeMode) { document.setHeader(ENVELOPE_MODE_HEADER, envelopeMode); }
    public void setEncryptionPaddingMode(String envelopePaddingMode) { document.setHeader(ENVELOPE_PADDING_MODE_HEADER, envelopePaddingMode); }
    
    
//        @Override
        public static boolean isCompatible(Pem pem) {
            return pem.getBanner().equals(PEM_BANNER) && pem.getHeaders().containsKey(CONTENT_ALGORITHM_HEADER) && pem.getHeaders().containsKey(ENVELOPE_KEY_ID_HEADER) && pem.getHeaders().containsKey(ENVELOPE_ALGORITHM_HEADER);
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
    
}
