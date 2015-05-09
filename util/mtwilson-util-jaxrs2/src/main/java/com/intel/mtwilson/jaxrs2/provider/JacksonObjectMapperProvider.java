/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.provider;

import com.fasterxml.jackson.annotation.JsonInclude;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.codehaus.jackson.map.SerializationConfig.Feature;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.intel.mtwilson.jackson.bouncycastle.BouncyCastleModule;
import com.intel.mtwilson.jackson.validation.ValidationModule;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
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
@Produces({MediaType.APPLICATION_JSON,DataMediaType.APPLICATION_RELATIONAL_PATCH_JSON,DataMediaType.APPLICATION_VND_API_JSON,DataMediaType.APPLICATION_JSON_PATCH})
public class JacksonObjectMapperProvider implements ContextResolver<ObjectMapper> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JacksonObjectMapperProvider.class);

    private final ObjectMapper defaultObjectMapper;
 
    public JacksonObjectMapperProvider() {
        log.debug("JacksonObjectMapperProvider constructor");
        defaultObjectMapper = createDefaultMapper();
    }
 
    @Override
    public ObjectMapper getContext(Class<?> type) {
        log.debug("JacksonObjectMapperProvider getContext");
        return defaultObjectMapper;
    }
 
    public static ObjectMapper createDefaultMapper() {
        log.debug("JacksonObjectMapperProvider createDefaultMapper");
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        mapper.registerModule(new BouncyCastleModule());  // this is a good spot for an extension point
        mapper.registerModule(new ValidationModule());  // this is a good spot for an extension point
        return mapper;
    }
 
}    
