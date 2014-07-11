/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.provider;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.codehaus.jackson.map.SerializationConfig.Feature;
//import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
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
@Produces({DataMediaType.APPLICATION_YAML,DataMediaType.TEXT_YAML,MediaType.TEXT_PLAIN})
public class JacksonYamlObjectMapperProvider implements ContextResolver<ObjectMapper> {
 
    private final ObjectMapper defaultObjectMapper;
 
    public JacksonYamlObjectMapperProvider() {
        defaultObjectMapper = createDefaultMapper();
    }
 
    @Override
    public ObjectMapper getContext(Class<?> type) {
        return defaultObjectMapper;
    }
 
    private ObjectMapper createDefaultMapper() {
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        yamlFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        ObjectMapper mapper = new ObjectMapper(yamlFactory);
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
 
}    
