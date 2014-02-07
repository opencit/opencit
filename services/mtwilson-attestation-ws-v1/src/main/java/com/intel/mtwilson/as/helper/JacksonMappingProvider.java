/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.helper;

import java.net.URL;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
//import org.codehaus.jackson.map.DeserializationConfig;
//import org.codehaus.jackson.map.ObjectMapper;

/**
 * Allows the OpenStack JSON format where a single-element array is serialized
 * as a string instead of as a single-element array.
 * 
 * Glassfish comes with its own copy of jackson-jaxrs in
 * glassfish/modules/jackson-mapper-asl.jar.
 * 
 * Glassfish 3.1.1 includes an older version of jackson mapper that does not
 * support the feature ACCEPT_SINGLE_VALUE_AS_ARRAY. 
 * 
 * Glassfish 3.1.2.2 includes a newer version jackson that does support this
 * feature. 
 * 
 * @author jbuhacoff
 * @since 1.1 
 */
@Provider
public class JacksonMappingProvider implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper = new ObjectMapper();
    
    public JacksonMappingProvider() {
        /*
        DeserializationConfig.Feature[] values = DeserializationConfig.Feature.values();
        for(DeserializationConfig.Feature value : values) {
            System.out.println("[JACKSON DeserializationConfig.Feature] "+value.name());
        }
        URL jackson = getClass().getResource("org/codehaus/jackson/map/ObjectMapper.class");
        if( jackson != null ) {
            System.out.println("[JACKSON JAR] "+jackson.toExternalForm());
        }
        */
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }
    
    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
    
}
