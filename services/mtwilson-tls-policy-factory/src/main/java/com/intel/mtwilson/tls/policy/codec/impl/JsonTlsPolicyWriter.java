/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.codec.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 *
 * @author jbuhacoff
 */
public class JsonTlsPolicyWriter {

    public byte[] write(TlsPolicyDescriptor tlsPolicyDescriptor) {
        try {
            ObjectMapper json = JacksonObjectMapperProvider.createDefaultMapper();
            return json.writeValueAsString(tlsPolicyDescriptor).getBytes(Charset.forName("UTF-8"));
        }
        catch(IOException e) {
            throw new IllegalArgumentException(e); // it's not TlsPolicyDescriptorInvalidException because we weren't able to read the content... there is no descriptor yet
        }
    }
    
}
