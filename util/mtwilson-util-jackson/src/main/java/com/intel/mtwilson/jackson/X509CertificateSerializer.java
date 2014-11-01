/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

/**
 *
 * @author jbuhacoff
 */
public class X509CertificateSerializer extends JsonSerializer<X509Certificate> {

    @Override
    public void serialize(X509Certificate t, JsonGenerator jg, SerializerProvider sp) throws IOException, JsonProcessingException {
        try {
            jg.writeBinary(t.getEncoded());
        }
        catch(CertificateEncodingException e) {
            throw new IOException(e);
        }
    }
    
}
