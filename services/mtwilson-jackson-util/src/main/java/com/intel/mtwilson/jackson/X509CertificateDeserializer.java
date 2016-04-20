/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.intel.dcsg.cpg.x509.X509Util;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 *
 * @author jbuhacoff
 */
public class X509CertificateDeserializer extends JsonDeserializer<X509Certificate> {

    @Override
    public X509Certificate deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
        byte[] bytes = jp.getBinaryValue();
        try {
            X509Certificate certificate = X509Util.decodeDerCertificate(bytes);
            return certificate;
        }
        catch(CertificateException e) {
//            throw new JsonProcessingException("Cannot read certificate", jp.getCurrentLocation(), e);
            throw new IOException("Cannot read certificate", e);
        }
    }
    
}
