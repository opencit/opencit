/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey.provider;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.codehaus.jackson.map.SerializationConfig.Feature;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
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
public class JacksonObjectMapperProvider implements ContextResolver<ObjectMapper> {
 
    private final ObjectMapper defaultObjectMapper;
 
    public JacksonObjectMapperProvider() {
        defaultObjectMapper = createDefaultMapper();
    }
 
    @Override
    public ObjectMapper getContext(Class<?> type) {
        return defaultObjectMapper;
    }
 
    private ObjectMapper createDefaultMapper() {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        return mapper;
    }
 
}    
