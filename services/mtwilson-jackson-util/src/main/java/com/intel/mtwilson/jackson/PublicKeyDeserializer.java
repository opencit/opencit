/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import java.io.IOException;
import java.security.PublicKey;

/**
 *
 * @author jbuhacoff
 */
public class PublicKeyDeserializer extends JsonDeserializer<PublicKey> {

    @Override
    public PublicKey deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
        byte[] bytes = jp.getBinaryValue();
        try {
            PublicKey publicKey = RsaUtil.decodeDerPublicKey(bytes);
            return publicKey;
        }
        catch(Exception e) {
//            throw new JsonProcessingException("Cannot read public key", jp.getCurrentLocation(), e);
            throw new IOException("Cannot read public key", e);
        }
    }
    
}
