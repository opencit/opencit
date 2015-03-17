/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
/**
 *
 * @author jbuhacoff
 */
public class JacksonCodec implements ObjectCodec {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JacksonCodec.class);

    private ObjectMapper jackson;

    public JacksonCodec() {
        jackson = new ObjectMapper();
        jackson.enableDefaultTyping();
        jackson.addMixInAnnotations(Object.class, JacksonObjectMixIn.class);
    }
    
    public JacksonCodec(ObjectMapper mapper) {
        jackson = mapper;
    }
    
    

    @Override
    public byte[] encode(Object input) {
        log.debug("encode object to byte[]");
        try {
            return jackson.writeValueAsBytes(input);
        }
        catch(JsonProcessingException e) {
            log.error("Cannot encode input", e);
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Object decode(byte[] encoded) {
        log.debug("decode byte[] to object");
        try {   
            return jackson.readValue(encoded, Object.class); // throws IOException and subclasses of IOException: JsonParseException, JsonMappingException
        }
        catch(IOException e) {
            log.error("Cannot decode input", e);
            throw new IllegalArgumentException();
        }
    }

}
