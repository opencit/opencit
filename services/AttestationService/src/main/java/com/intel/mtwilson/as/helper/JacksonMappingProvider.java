/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.helper;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Allows the OpenStack JSON format where a single-element array is serialized
 * as a string instead of as a single-element array.
 * @author jbuhacoff
 * @since 1.1 
 */
@Provider
public class JacksonMappingProvider implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper = new ObjectMapper();
    
    public JacksonMappingProvider() {
        DeserializationConfig.Feature[] values = DeserializationConfig.Feature.values();
        for(DeserializationConfig.Feature value : values) {
            System.out.println("[JACKSON DeserializationConfig.Feature] "+value.name());
        }
//        mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }
    
    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
    
}
