/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.model.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.intel.mtwilson.tag.model.X509AttributeCertificate;
import java.io.IOException;

/**
 *
 * @author rksavino
 */
public class X509AttributeCertificateDeserializer extends JsonDeserializer<X509AttributeCertificate> {

    @Override
    public X509AttributeCertificate deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        byte[] encodedBytes = node.get("encoded").binaryValue();
        return X509AttributeCertificate.valueOf(encodedBytes);
    }
    
}
