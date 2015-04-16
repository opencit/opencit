/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.provider;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.codehaus.jackson.map.SerializationConfig.Feature;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * A hypothetical example JSON output using the default ObjectMapper settings 
 * might look like this (notice camelCase on connectionUrl and biosMLE):
 * 
{"hosts":[{"id":"093b4663-ca45-4d3e-8a3a-f1f584996a6b","name":"hostabc","connectionUrl":"http://1.2.3.4","description":"test host","biosMLE":"bios-4.3.2"}]}
 * 
 * But the same example object when this provider is on the classpath will
 * look like this (notice the underscores connection_url and bios_mle):
 * 
{"hosts":[{"id":"093b4663-ca45-4d3e-8a3a-f1f584996a6b","name":"hostabc","connection_url":"http://1.2.3.4","description":"test host","bios_mle":"bios-4.3.2"}]}
 * 
 * 
 * @author jbuhacoff
 */
@Provider
@Produces({MediaType.APPLICATION_JSON})
public class V1JacksonObjectMapperProvider implements ContextResolver<ObjectMapper> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(V1JacksonObjectMapperProvider.class);

    private final ObjectMapper defaultObjectMapper;
 
    public V1JacksonObjectMapperProvider() {
        log.debug("V1JacksonObjectMapperProvider constructor");
        defaultObjectMapper = createDefaultMapper();
    }
 
    @Override
    public ObjectMapper getContext(Class<?> type) {
        log.debug("V1JacksonObjectMapperProvider getContext");
        return defaultObjectMapper;
    }
 
    private ObjectMapper createDefaultMapper() {
        log.debug("V1JacksonObjectMapperProvider createDefaultMapper");
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        ObjectMapper mapper = new ObjectMapper(jsonFactory);
//        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy()); // v1 apis dont' use this because it overrides @JsonProperty annotations 
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return mapper;
    }
 
}    
